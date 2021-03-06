package controllers;

import models.Donation;
import models.Project;
import models.User;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Security;
import viewmodels.donation.DonationData;
import viewmodels.donation.MyDonationsData;
import viewmodels.donation.ProjectDonationData;

import java.text.SimpleDateFormat;
import java.util.List;


public class MyDonationsController {
    @Security.Authenticated(AuthenticationController.class)
    public static Result myDonations() {
        MyDonationsData data = getData();
        Form<MyDonationsData> form = Form.form(MyDonationsData.class).fill(data);

        return play.mvc.Controller.ok(views.html.user.myDonations.render(data));
    }

    private static MyDonationsData getData() {
        User user = ApplicationController.getCurrentUser();
        return getFormData(user);
    }
    protected static MyDonationsData getFormData(User user){
        List<Donation> donations = user.getDonations();
        MyDonationsData result = new MyDonationsData();

        for (Donation donation: donations) {
            Project project = donation.getDonationGoal().getProject();
            String date = new SimpleDateFormat("dd.MM.yyyy").format(donation.getDate());

            ProjectDonationData projectdata = result.getOrSetData(
                project, date, donation.getMessageToCollector()
            );
            projectdata.donations.add(new DonationData(donation));
        }

        return result;
    }


}
