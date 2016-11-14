package com.peprally.jeremy.peprally.custom;

import java.util.ArrayList;

public class FeedbackContainer {

    private ArrayList<Feedback> feedbacks;

    public FeedbackContainer(ArrayList<Feedback> feedbacks) {
        if (feedbacks == null) {
            this.feedbacks = new ArrayList<>();
        } else {
            this.feedbacks = feedbacks;
        }
    }

    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
    }

    public ArrayList<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(ArrayList<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }
}
