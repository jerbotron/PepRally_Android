package com.peprally.jeremy.peprally.db_models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.peprally.jeremy.peprally.custom.Feedback;
import com.peprally.jeremy.peprally.custom.FeedbackContainer;
import com.peprally.jeremy.peprally.custom.preferences.NotificationsPref;
import com.peprally.jeremy.peprally.db_models.json_marshallers.FeedbacksJSONMarshaller;
import com.peprally.jeremy.peprally.db_models.json_marshallers.NotificationsPrefJSONMarshaller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Deprecated
@DynamoDBTable(tableName = "UserProfiles")
public class DBUserProfile {
    private String username;
    private String cognitoId;
    private String facebookId;
    private String facebookLink;
    private String FCMInstanceId;
    private String email;
    private String firstname;
    private String lastname;
    private String gender;
    private String birthday;
    private String favoriteTeam;
    private String favoritePlayer;
    private String pepTalk;
    private String trashTalk;
    private String dateJoined;
    private String dateLastLoggedIn;
    private String schoolName;
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
    private int playerIndex;
    private long timestampLastLoggedIn;
    private boolean hasNewMessage;
    private boolean hasNewNotification;
    private boolean isNewUser;
    private boolean isVarsityPlayer;
    private NotificationsPref notificationsPref;
    private FeedbackContainer feedbackContainer;

    // Helpers
    public void addConversationId(String id) {
        if (conversationIds == null)
            conversationIds = new HashSet<>(Collections.singletonList(id));
        else
            conversationIds.add(id);
    }

    public void removeConversationId(String id) {
        if (conversationIds != null) {
            conversationIds.remove(id);
            if (conversationIds.isEmpty())
                conversationIds = null;
        }
    }

    public void addUsersDirectFistbumpSent(String user) {
        if (usersDirectFistbumpSent == null)
            usersDirectFistbumpSent = new HashSet<>(Collections.singletonList(user));
        else
            usersDirectFistbumpSent.add(user);
    }

    public void removeUsersDirectFistbumpSent(String user) {
        if (usersDirectFistbumpSent != null) {
            usersDirectFistbumpSent.remove(user);
            if (usersDirectFistbumpSent.isEmpty())
                usersDirectFistbumpSent = null;
        }
    }

    public void addUsersDirectFistbumpReceived(String user) {
        if (usersDirectFistbumpReceived == null)
            usersDirectFistbumpReceived = new HashSet<>(Collections.singletonList(user));
        else
            usersDirectFistbumpReceived.add(user);
    }

    public void removeUsersDirectFistbumpReceived(String user) {
        if (usersDirectFistbumpReceived != null) {
            usersDirectFistbumpReceived.remove(user);
            if (usersDirectFistbumpReceived.isEmpty())
                usersDirectFistbumpReceived = null;
        }
    }

    public void addFeedback(Feedback feedback) {
        if (feedbackContainer == null) {
            feedbackContainer = new FeedbackContainer(new ArrayList<Feedback>());
        }
        feedbackContainer.addFeedback(feedback);
    }

    // Setters/Getters
    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
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

    @DynamoDBAttribute(attributeName = "Firstname")
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @DynamoDBAttribute(attributeName = "Lastname")
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
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

    @DynamoDBAttribute(attributeName = "DateLastLoggedIn")
    public String getDateLastLoggedIn() {
        return dateLastLoggedIn;
    }
    public void setDateLastLoggedIn(String dateLastLoggedIn) {
        this.dateLastLoggedIn = dateLastLoggedIn;
    }

    @DynamoDBAttribute(attributeName = "SchoolName")
    public String getSchoolName() {
        return schoolName;
    }
    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    @DynamoDBAttribute(attributeName = "PlayerTeam")
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

    @DynamoDBAttribute(attributeName = "PlayerIndex")
    public int getPlayerIndex() {
        return playerIndex;
    }
    public void setPlayerIndex(int playerIndex) {
        this.playerIndex = playerIndex;
    }

    @DynamoDBAttribute(attributeName = "LastLoggedInTimestamp")
    public long getTimestampLastLoggedIn() {
        return timestampLastLoggedIn;
    }
    public void setTimestampLastLoggedIn(long timestampLastLoggedIn) {
        this.timestampLastLoggedIn = timestampLastLoggedIn;
    }

    @DynamoDBAttribute(attributeName = "SetData")
    public Set<String> getConversationIds() {
        return conversationIds;
    }
    public void setConversationIds(Set<String> conversationIds) {
        this.conversationIds = conversationIds;
    }

    @DynamoDBAttribute(attributeName = "UsersDirectFistbumpSent")
    public Set<String> getUsersDirectFistbumpSent() {
        return usersDirectFistbumpSent;
    }
    public void setUsersDirectFistbumpSent(Set<String> usersDirectFistbumpSent) {
        this.usersDirectFistbumpSent = usersDirectFistbumpSent;
    }

    @DynamoDBAttribute(attributeName = "UsersDirectFistbumpReceived")
    public Set<String> getUsersDirectFistbumpReceived() {
        return usersDirectFistbumpReceived;
    }
    public void setUsersDirectFistbumpReceived(Set<String> usersDirectFistbumpReceived) {
        this.usersDirectFistbumpReceived = usersDirectFistbumpReceived;
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

    @DynamoDBAttribute(attributeName = "NotificationsPref")
    @DynamoDBMarshalling(marshallerClass = NotificationsPrefJSONMarshaller.class)
    public NotificationsPref getNotificationsPref() { return notificationsPref; }
    public void setNotificationsPref(NotificationsPref notificationsPref) { this.notificationsPref = notificationsPref; }

    @DynamoDBAttribute(attributeName = "Feedbacks")
    @DynamoDBMarshalling(marshallerClass = FeedbacksJSONMarshaller.class)
    public FeedbackContainer getFeedbacks() { return feedbackContainer; }
    public void setFeedbacks(FeedbackContainer feedbackContainer) { this.feedbackContainer = feedbackContainer; }
}
