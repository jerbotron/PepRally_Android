package com.peprally.jeremy.peprally;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "UserProfile")
public class UserProfile {
    private String cognitoId;
    private boolean newUser;
    private String firstName;
    private String lastName;
    private int age;
    private int followers;
    private int following;
    private int fistbumps;
    private String motto;
    private String favoriteTeam;
    private String favoritePlayer;
    private String pepTalk;
    private String trashTalk;
    private String dateJoined;

    @DynamoDBHashKey(attributeName = "CognitoId")
    public String getCognitoId() {
        return cognitoId;
    }

    public void setCognitoId(String cognitoId) {
        this.cognitoId = cognitoId;
    }

    @DynamoDBAttribute(attributeName = "NewUser")
    public boolean getNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    @DynamoDBRangeKey(attributeName = "FirstName")
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

    @DynamoDBAttribute(attributeName = "Age")
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @DynamoDBAttribute(attributeName = "Followers")
    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    @DynamoDBAttribute(attributeName = "Following")
    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    @DynamoDBAttribute(attributeName = "FistBumps")
    public int getFistbumps() {
        return fistbumps;
    }

    public void setFistbumps(int fistbumps) {
        this.fistbumps = fistbumps;
    }

    @DynamoDBAttribute(attributeName = "Motto")
    public String getMotto() {
        return motto;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }

    @DynamoDBAttribute(attributeName = "FavoriteTeam")
    public String getFavoriteTeam() {
        return favoriteTeam;
    }

    public void setFavoriteTeam(String favoriteTeam) {
        this.favoriteTeam = favoriteTeam;
    }

    @DynamoDBAttribute(attributeName = "FavoritePlayer")
    public String getFavoritePlayer() {
        return favoritePlayer;
    }

    public void setFavoritePlayer(String favoritePlayer) {
        this.favoritePlayer = favoritePlayer;
    }

    @DynamoDBAttribute(attributeName = "PepTalk")
    public String getPepTalk() {
        return pepTalk;
    }

    public void setPepTalk(String pepTalk) {
        this.pepTalk = pepTalk;
    }

    @DynamoDBAttribute(attributeName = "TrashTalk")
    public String getTrashTalk() {
        return trashTalk;
    }

    public void setTrashTalk(String trashTalk) {
        this.trashTalk = trashTalk;
    }

    @DynamoDBAttribute(attributeName = "DateJoined")
    public String getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }
}
