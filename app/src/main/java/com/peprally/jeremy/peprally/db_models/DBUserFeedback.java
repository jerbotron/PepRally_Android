package com.peprally.jeremy.peprally.db_models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "UserFeedback")
public class DBUserFeedback {
    private String username;
    private String feedback;
    private String platform;
    private Long timestampSeconds;
    private int feedbackType;   // 0 = general feedback, 1 = account deletion

    // Hash/Range Constructor
    public DBUserFeedback(String username, Long timestampSeconds) {
        this.username = username;
        this.timestampSeconds = timestampSeconds;
    }

    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBAttribute(attributeName = "Feedback")
    public String getFeedback() { return feedback; }

    public void setFeedback(String feedback) { this.feedback = feedback; }

    @DynamoDBAttribute(attributeName = "Platform")
    public String getPlatform() { return platform; }

    public void setPlatform(String platform) { this.platform = platform; }

    @DynamoDBRangeKey(attributeName = "Timestamp")
    public Long getTimestampSeconds() {
        return timestampSeconds;
    }

    public void setTimestampSeconds(Long timestampSeconds) {
        this.timestampSeconds = timestampSeconds;
    }

    @DynamoDBAttribute(attributeName = "FeedbackType")
    public int getFeedbackType() { return feedbackType; }

    public void setFeedbackType(int feedbackType) { this.feedbackType = feedbackType; }
}
