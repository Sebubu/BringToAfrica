package controllers;

import exceptions.AfricaException;
import models.*;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.Html;
import services.DateConverter;
import services.DonationTypeService;
import services.ProjectService;
import viewmodels.*;
import viewmodels.donation.CreateDonationData;

import java.util.List;


public class ProjectController extends Controller {


    public static Html getProjectWidget(long id) {
        Project project = ProjectService.getProjectById(id);
        ProjectWidget projectWidget = new ProjectWidget(project);
        return views.html.project.widget.render(projectWidget);
    }

    public static Result getProject(long id) {
        Project project = ProjectService.getProjectById(id);
        ProjectDetail projectDetail = new ProjectDetail(project);
        User user = ApplicationController.getCurrentUser();
        Boolean isAuthenticated = user != null;
        Boolean isProjectOwner = isAuthenticated && user.getId().equals(project.getOwner().getId());
        return ok(views.html.project.projectdetails.render(
            project, projectDetail, createDonationForm(project),
            Form.form(NewsData.class), isAuthenticated, isProjectOwner
        ));
    }

    private static Form<CreateDonationData> createDonationForm(Project project) {
        List<DonationGoal> goals = project.getDonationGoals();
        CreateDonationData data = new CreateDonationData(goals);
        return Form.form(CreateDonationData.class).fill(data);
    }

    @Security.Authenticated(AuthenticationController.class)
    public static Result addProjectData() throws AfricaException {
        Form<ProjectData> projectDataForm = Form.form(ProjectData.class).bindFromRequest();
        if (projectDataForm.hasErrors()) {
            return badRequest(views.html.project.newProject.render(projectDataForm));
        } else {
            User user = ApplicationController.getCurrentUser();
            Address address = setAddress(projectDataForm);
            models.Project project = createProject(projectDataForm, address, user);
            addDonationGoals(project, projectDataForm);
            return redirect(routes.ApplicationController.index());
        }
    }

    private static Project createProject(
            Form<ProjectData> projectDataForm, Address address, User user) throws AfricaException {
        models.Project project = new Project();
        project.setTitle(projectDataForm.get().title);
        project.setDescription(projectDataForm.get().description);
        project.setImageURL(projectDataForm.get().imageURL);
        project.setEndsAt(DateConverter.stringToSqlDate(projectDataForm.get().endsAt));
        project.setStartsAt(DateConverter.stringToSqlDate(projectDataForm.get().startsAt));
        project.setContact(projectDataForm.get().contactInformation);
        project.setAddress(address);
        project.setOwner(user);
        project.save();
        return project;
    }

    private static void addDonationGoals(Project project, Form<ProjectData> projectDataForm) {
        for (int i = 0; i < projectDataForm.get().donations.size(); i++) {
            DonationType donationType = DonationTypeService.getOrSetDonationType(
                projectDataForm.get().donations.get(i)
            );
            DonationGoal donationGoal = new DonationGoal(project);
            donationGoal.setAmount(Integer.parseInt(projectDataForm.get().amounts.get(i)));
            donationGoal.setType(donationType);
            donationGoal.setProject(project);
            donationGoal.save();
        }
    }

    private static Address setAddress(Form<ProjectData> projectDataForm) {
        Address address = new models.Address();
        address.setCountry(projectDataForm.get().country);
        address.setCity(projectDataForm.get().city);
        address.save();
        return address;
    }

    @Security.Authenticated(AuthenticationController.class)
    public static Result createProject() {
        return ok(views.html.project.newProject.render(Form.form(ProjectData.class)));
    }

}
