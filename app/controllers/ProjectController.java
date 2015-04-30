package controllers;

import com.avaje.ebean.Page;
import exceptions.AfricaException;
import models.*;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import services.DonationTypeService;
import viewmodels.*;
import play.mvc.Security;
import viewmodels.NewsData;
import viewmodels.donation.CreateDonationData;
import viewmodels.ProjectData;
import viewmodels.ProjectWidget;
import viewmodels.donation.DonationData;
import viewmodels.donation.ProjectDonationData;
import views.html.newProject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import services.ProjectService;


public class ProjectController extends Controller {

    private static final int PAGE_SIZE = 10;

    public static Result getProjectWidgets(int page) {
        Page<Project> projectPage = ProjectService.getProjectPage(PAGE_SIZE, page);
        List<ProjectWidget> widgets = new ArrayList<>();
        for (Project p :projectPage.getList()) {
            widgets.add(new ProjectWidget(p));
        }
        return ok(views.html.index.render(widgets, projectPage.getTotalPageCount(), page));
    }

    public static Html getProjectWidget(long id) {
        Project project = ProjectService.getProjectById(id);
        ProjectWidget projectWidget = new ProjectWidget(project);
        return views.html.project.widget.render(projectWidget);
    }

    public static Result getProject(long id) {
        Project project = ProjectService.getProjectById(id);
        ProjectDetail projectDetail = new ProjectDetail(project);
        return ok(views.html.project.detail.render(project, projectDetail, createDonationForm(project), Form.form(NewsData.class)));
    }

    public static Result donate() {
        Form<CreateDonationData> form = Form.form(CreateDonationData.class).bindFromRequest();
        Project project = ProjectService.getProjectById(form.get().projectId);

        if (form.hasErrors()) {
            return badRequest(views.html.project.donate.render(project, form));
        } else {
            List<DonationGoal> goals = project.getDonationGoals();
            ArrayList<DonationData> donations = createDonations(form, goals);

            ProjectDonationData donation = new ProjectDonationData();
            donation.project = project;
            donation.donations = donations;
            return ok(views.html.project.donateSuccess.render(donation));
        }
    }

    private static ArrayList<DonationData> createDonations(Form<CreateDonationData> form, List<DonationGoal> goals) {
        ArrayList<DonationData> donations = new ArrayList<>();

        for (int i = 0; i < form.get().amounts.size(); i++) {
            String typeName = form.get().donations.get(i);
            int amount = Integer.parseInt(form.get().amounts.get(i));

            User user = ApplicationController.getCurrentUser();
            DonationGoal goal = getGoalByType(goals, typeName);

            Donation donation = new Donation(user, goal);
            donation.setAmount(amount);
            donation.save();

            donations.add(new DonationData(donation));
        }
        return donations;
    }

    private static DonationGoal getGoalByType(List<DonationGoal> goals, String typeName) {
        DonationType type = DonationTypeService.getDonationTypeByName(typeName);
        return goals.stream()
                .filter(g -> g.getType().getName().equals(type.getName()))
                .limit(2)
                .collect(Collectors.toList()).get(0);
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
            return badRequest(newProject.render(projectDataForm));
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
        for (int i = 0; i < projectDataForm.get().amounts.size(); i++) {
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
        return ok(newProject.render(Form.form(ProjectData.class)));
    }

}
