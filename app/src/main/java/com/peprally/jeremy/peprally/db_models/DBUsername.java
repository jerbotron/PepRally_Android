package com.peprally.jeremy.peprally.db_models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "Usernames")
public class DBUsername {
    private String username;
    private String cognitoId;
    private String facebookId;

    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBAttribute(attributeName = "CognitoId")
    public String getCognitoId() {
        return cognitoId;
    }

    public void setCognitoId(String cognitoId) {
        this.cognitoId = cognitoId;
    }

    @DynamoDBAttribute(attributeName = "FacebookId")
    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }
}
