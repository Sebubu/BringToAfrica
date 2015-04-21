package viewmodels.donation;


import models.DonationGoal;

import java.util.ArrayList;
import java.util.List;

public class CreateDonationData {

    public CreateDonationData() {
    }

    public CreateDonationData(List<DonationGoal> goals) {
        for (DonationGoal goal : goals) {
            donations.add(goal.getType().getName());
            amounts.add("0");
        }
    }

    public long projectId;
    public List<String> amounts = new ArrayList<>();
    public List<String> donations = new ArrayList<>();
    public String remarks;
}