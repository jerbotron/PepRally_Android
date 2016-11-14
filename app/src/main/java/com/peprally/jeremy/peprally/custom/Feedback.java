package com.peprally.jeremy.peprally.custom;

import com.peprally.jeremy.peprally.enums.FeedbackEnum;
import com.peprally.jeremy.peprally.enums.PlatformEnum;

import org.json.JSONException;
import org.json.JSONObject;

public class Feedback {
    private String username;
    private String feedback;
    private Long timestamp;
    private PlatformEnum platform;
    private FeedbackEnum feedbackType;

    public Feedback(String username,
                    String feedback,
                    Long timestamp,
                    PlatformEnum platform,
                    FeedbackEnum feedbackType) {
        this.username = username;
        this.feedback = feedback;
        this.timestamp = timestamp;
        this.platform = platform;
        this.feedbackType = feedbackType;
    }

    public Feedback (JSONObject json) {
        try {
            username = json.getString("username");
            feedback = json.getString("feedback");
            timestamp = json.getLong("timestamp");
            platform = PlatformEnum.fromInt(json.getInt("platform"));
            feedbackType = FeedbackEnum.fromInt(json.getInt("feedback_type"));
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public PlatformEnum getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformEnum platform) {
        this.platform = platform;
    }

    public FeedbackEnum getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(FeedbackEnum feedbackType) {
        this.feedbackType = feedbackType;
    }
}
