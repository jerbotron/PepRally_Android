package com.peprally.jeremy.peprally.db_models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import java.util.Set;

@DynamoDBTable(tableName = "UserProfiles")
public class DBUserProfile {
    private String nickname;
    private String cognitoId;
    private String facebookId;
    private String facebookLink;
    private String FCMInstanceId;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String birthday;
    private String favoriteTeam;
    private String favoritePlayer;
    private String pepTalk;
    private String trashTalk;
    private String dateJoined;
    private String team;
    private Set<String> conversationIds;
    private Set<String> usersDirectFistbumpSent;
    private Set<String> usersDirectFistbumpReceived;
    private int age;
    private int followersCount;
    private int followingCount;
    private int sentFistbumpsCount;
    private int receivedFistbumpsCount;
    private int postsCount;
    private int notificationsCount;         //TODO: implement when I need to buffer notifications
    private int playerIndex;
    private boolean hasNewMessage;
    private boolean hasNewNotification;
    private boolean isNewUser;
    private boolean isVarsityPlayer;

    @DynamoDBHashKey(attributeName = "Nickname")
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "CognitoId-index", attributeName = "CognitoId")
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

    @DynamoDBAttribute(attributeName = "FCMInstanceId")
    public String getFCMInstanceId() {
        return FCMInstanceId;
    }

    public void setFCMInstanceId(String FCMInstanceId) {
        this.FCMInstanceId = FCMInstanceId;
    }

    @DynamoDBAttribute(attributeName = "FacebookLink")
    public String getFacebookLink() {
        return facebookLink;
    }

    public void setFacebookLink(String facebookLink) {
        this.facebookLink = facebookLink;
    }

    @DynamoDBAttribute(attributeName = "Email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @DynamoDBAttribute(attributeName = "Gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @DynamoDBAttribute(attributeName = "Birthday")
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
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

    @DynamoDBAttribute(attributeName = "DateJoined")
    public String getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "Team-index", attributeName = "PlayerTeam")
    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @DynamoDBAttribute(attributeName = "Age")
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @DynamoDBAttribute(attributeName = "FollowersCount")
    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    @DynamoDBAttribute(attributeName = "FollowingCount")
    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    @DynamoDBAttribute(attributeName = "SentFistbumpsCount")
    public int getSentFistbumpsCount() {
        return sentFistbumpsCount;
    }

    public void setSentFistbumpsCount(int sentFistbumpsCount) {
        if (sentFistbumpsCount > 0)
            this.sentFistbumpsCount = sentFistbumpsCount;
        else
            this.sentFistbumpsCount = 0;
    }

    public void incrementSentFistbumpsCount() {
        sentFistbumpsCount += 1;
    }

    public void decrementSentFistbumpsCount() {
        if (sentFistbumpsCount > 0)
            sentFistbumpsCount -= 1;
    }

    @DynamoDBAttribute(attributeName = "ReceivedFistbumpsCount")
    public int getReceivedFistbumpsCount() {
        return receivedFistbumpsCount;
    }

    public void setReceivedFistbumpsCount(int receivedFistbumpsCount) {
        if (receivedFistbumpsCount > 0)
            this.receivedFistbumpsCount = receivedFistbumpsCount;
        else
            this.receivedFistbumpsCount = 0;
    }

    public void incrementReceivedFistbumpsCount() {
        receivedFistbumpsCount += 1;
    }

    public void decrementReceivedFistbumpsCount() {
        if (receivedFistbumpsCount > 0)
            receivedFistbumpsCount -= 1;
    }

    @DynamoDBAttribute(attributeName = "PostsCount")
    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        if (postsCount > 0)
            this.postsCount = postsCount;
        else
            this.postsCount = 0;
    }

    public void incrementPostCount() {
        postsCount += 1;
    }

    public void decrementPostCount() {
        if (postsCount > 0)
            postsCount -= 1;
    }

    public void setTrashTalk(String trashTalk) {
        this.trashTalk = trashTalk;
    }

    @DynamoDBAttribute(attributeName = "PlayerIndex")
    public int getPlayerIndex() {
        return playerIndex;
    }

    public void setPlayerIndex(int index) {
        this.playerIndex = index;
    }


    @DynamoDBAttribute(attributeName = "ConversationIds")
    public Set<String> getConversationIds() {
        return conversationIds;
    }

    public void setConversationIds(Set<String> conversationIds) {
        this.conversationIds = conversationIds;
    }

    public void addConversationId(String id) {
        if (conversationIds != null)
            conversationIds.add(id);
    }

    public void removeConversationId(String id) {
        if (conversationIds != null)
            conversationIds.remove(id);
    }

    @DynamoDBAttribute(attributeName = "UsersDirectFistbumpSent")
    public Set<String> getUsersDirectFistbumpSent() {
        return usersDirectFistbumpSent;
    }

    public void setUsersDirectFistbumpSent(Set<String> usersDirectFistbumpSent) {
        this.usersDirectFistbumpSent = usersDirectFistbumpSent;
    }

    public void addUsersDirectFistbumpSent(String user) {
        if (usersDirectFistbumpSent != null)
            usersDirectFistbumpSent.add(user);
    }

    public void removeUsersDirectFistbumpSent(String user) {
        if (usersDirectFistbumpSent != null)
            usersDirectFistbumpSent.remove(user);
    }

    @DynamoDBAttribute(attributeName = "UsersDirectFistbumpReceived")
    public Set<String> getUsersDirectFistbumpReceived() {
        return usersDirectFistbumpReceived;
    }

    public void setUsersDirectFistbumpReceived(Set<String> usersDirectFistbumpReceived) {
        this.usersDirectFistbumpReceived = usersDirectFistbumpReceived;
    }

    public void addUsersDirectFistbumpReceived(String user) {
        if (usersDirectFistbumpReceived != null)
            usersDirectFistbumpReceived.add(user);
    }

    public void removeUsersDirectFistbumpReceived(String user) {
        if (usersDirectFistbumpReceived != null)
            usersDirectFistbumpReceived.remove(user);
    }

    @DynamoDBAttribute(attributeName = "HasNewMessage")
    public boolean getHasNewMessage() {
        return hasNewMessage;
    }

    public void setHasNewMessage(boolean hasNewMessage) {
        this.hasNewMessage = hasNewMessage;
    }

    @DynamoDBAttribute(attributeName = "HasNewNotification")
    public boolean getHasNewNotification() {
        return hasNewNotification;
    }

    public void setHasNewNotification(boolean hasNewNotification) {
        this.hasNewNotification = hasNewNotification;
    }

    @DynamoDBAttribute(attributeName = "NewUser")
    public boolean getNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        this.isNewUser = newUser;
    }

    @DynamoDBAttribute(attributeName = "IsVarsityPlayer")
    public boolean getIsVarsityPlayer() {
        return isVarsityPlayer;
    }

    public void setIsVarsityPlayer(boolean isVarsityPlayer) {
        this.isVarsityPlayer = isVarsityPlayer;
    }
}
