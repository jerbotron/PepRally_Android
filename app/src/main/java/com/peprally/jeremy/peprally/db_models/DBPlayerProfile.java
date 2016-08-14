package com.peprally.jeremy.peprally.db_models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "PlayerProfiles")
public class DBPlayerProfile {
    private String team;
    private String firstName;
    private String lastName;
    private String imageURL;
    private String year;
    private String height;
    private String weight;
    private String position;
    private String hometown;
    private String gender;
    private String username;
    private int index;
    private int number;
    private boolean hasUserProfile;

    @DynamoDBHashKey(attributeName = "Team")
    public String getTeam() {
        return team;
    }
    public void setTeam(String team) {
        this.team = team;
    }

    @DynamoDBRangeKey(attributeName = "Index")
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    @DynamoDBAttribute(attributeName = "Number")
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    @DynamoDBAttribute(attributeName = "FirstName")
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @DynamoDBAttribute(attributeName = "LastName")
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @DynamoDBAttribute(attributeName = "ImageURL")
    public String getImageURL() {
        return imageURL;
    }
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @DynamoDBAttribute(attributeName = "Year")
    public String getYear() {
        return year;
    }
    public void setYear(String year) {
        this.year = year;
    }

    @DynamoDBAttribute(attributeName = "Height")
    public String getHeight() {
        return height;
    }
    public void setHeight(String height) {
        this.height = height;
    }

    @DynamoDBAttribute(attributeName = "Weight")
    public String getWeight() {
        return weight;
    }
    public void setWeight(String weight) {
        this.weight = weight;
    }

    @DynamoDBAttribute(attributeName = "Position")
    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }

    @DynamoDBAttribute(attributeName = "Hometown")
    public String getHometown() {
        return hometown;
    }
    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "MF-index", attributeName = "MF")
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    @DynamoDBAttribute(attributeName = "Username")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBAttribute(attributeName = "HasUserProfile")
    public boolean getHasUserProfile() {
        return hasUserProfile;
    }
    public void setHasUserProfile(boolean hasUserProfile) {
        this.hasUserProfile = hasUserProfile;
    }
}
