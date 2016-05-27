package com.peprally.jeremy.peprally.db_models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "UserProfiles")
public class DBUserProfile {
    private String cognitoID;
    private String facebookID;
    private boolean newUser;
    private String firstName;
    private String lastName;
    private int age;
    private int followers;
    private int following;
    private int fistbumps;
    private String nickname;
    private String favoriteTeam;
    private String favoritePlayer;
    private String pepTalk;
    private String trashTalk;
    private String dateJoined;
    private boolean isVarsityPlayer;
    private String team;
    private int playerIndex;
    private int postsCount;

    @DynamoDBHashKey(attributeName = "CognitoID")
    public String getCognitoId() {
        return cognitoID;
    }

    public void setCognitoId(String cognitoId) {
        this.cognitoID = cognitoId;
    }

    @DynamoDBAttribute(attributeName = "FacebookID")
    public String getFacebookID() {
        return facebookID;
    }

    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
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
    public int getFollowersCount() {
        return followers;
    }

    public void setFollowersCount(int followers) {
        this.followers = followers;
    }

    @DynamoDBAttribute(attributeName = "Following")
    public int getFollowingCount() {
        return following;
    }

    public void setFollowingCount(int following) {
        this.following = following;
    }

    @DynamoDBAttribute(attributeName = "FistBumps")
    public int getFistbumpsCount() {
        return fistbumps;
    }

    public void setFistbumpsCount(int fistbumps) {
        this.fistbumps = fistbumps;
    }

    @DynamoDBAttribute(attributeName = "PostsCount")
    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    @DynamoDBAttribute(attributeName = "Nickname")
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    @DynamoDBAttribute(attributeName = "IsVarsityPlayer")
    public boolean getIsVarsityPlayer() {
        return isVarsityPlayer;
    }

    public void setIsVarsityPlayer(boolean isVarsityPlayer) {
        this.isVarsityPlayer = isVarsityPlayer;
    }

    @DynamoDBIndexHashKey(attributeName = "Team")
    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @DynamoDBAttribute(attributeName = "Index")
    public int getPlayerIndex() {
        return playerIndex;
    }

    public void setPlayerIndex(int index) {
        this.playerIndex = index;
    }
}
