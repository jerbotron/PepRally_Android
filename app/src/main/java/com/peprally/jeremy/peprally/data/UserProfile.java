package com.peprally.jeremy.peprally.data;

import com.peprally.jeremy.peprally.custom.FeedbackContainer;
import com.peprally.jeremy.peprally.custom.preferences.NotificationsPref;
import com.peprally.jeremy.peprally.db_models.json_marshallers.FeedbacksJSONMarshaller;
import com.peprally.jeremy.peprally.db_models.json_marshallers.NotificationsPrefJSONMarshaller;

public class UserProfile {
	
	private String username;
	private String cognitoId;
	private String facebookId;
	private String facebookLink;
	private String fcmInstanceId;
	private String email;
	private String firstname;
	private String lastname;
	private String gender;
	private String birthday;
	private String favoriteTeam;
	private String favoritePlayer;
	private String pepTalk;
	private String trashTalk;
	private String dateJoinedUtc;
	private String dateLastLoggedInUtc;
	private String schoolName;
	private String team;
	private SetData conversationIds;
	private SetData usersDirectFistbumpSent;
	private SetData usersDirectFistbumpReceived;
	private int age;
	private int followersCount;
	private int followingCount;
	private int sentFistbumpsCount;
	private int receivedFistbumpsCount;
	private int postsCount;
	private int playerIndex;
	private long lastLoggedInTimestampInMs;
	private boolean hasNewMessage;
	private boolean hasNewNotification;
	private boolean newUser;
	private boolean isVarsityPlayer;
	private String notificationsPref;
	private String feedbacks;
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getCognitoId() {
		return cognitoId;
	}
	
	public void setCognitoId(String cognitoId) {
		this.cognitoId = cognitoId;
	}
	
	public String getFacebookId() {
		return facebookId;
	}
	
	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}
	
	public String getFacebookLink() {
		return facebookLink;
	}
	
	public void setFacebookLink(String facebookLink) {
		this.facebookLink = facebookLink;
	}
	
	public String getFcmInstanceId() {
		return fcmInstanceId;
	}
	
	public void setFcmInstanceId(String FCMInstanceId) {
		this.fcmInstanceId = FCMInstanceId;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getBirthday() {
		return birthday;
	}
	
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	
	public String getFavoriteTeam() {
		return favoriteTeam;
	}
	
	public void setFavoriteTeam(String favoriteTeam) {
		this.favoriteTeam = favoriteTeam;
	}
	
	public String getFavoritePlayer() {
		return favoritePlayer;
	}
	
	public void setFavoritePlayer(String favoritePlayer) {
		this.favoritePlayer = favoritePlayer;
	}
	
	public String getPepTalk() {
		return pepTalk;
	}
	
	public void setPepTalk(String pepTalk) {
		this.pepTalk = pepTalk;
	}
	
	public String getTrashTalk() {
		return trashTalk;
	}
	
	public void setTrashTalk(String trashTalk) {
		this.trashTalk = trashTalk;
	}
	
	public String getDateJoinedUtc() {
		return dateJoinedUtc;
	}
	
	public void setDateJoinedUtc(String dateJoinedUtc) {
		this.dateJoinedUtc = dateJoinedUtc;
	}
	
	public String getDateLastLoggedInUtc() {
		return dateLastLoggedInUtc;
	}
	
	public void setDateLastLoggedInUtc(String dateLastLoggedInUtc) {
		this.dateLastLoggedInUtc = dateLastLoggedInUtc;
	}
	
	public String getSchoolName() {
		return schoolName;
	}
	
	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}
	
	public String getTeam() {
		return team;
	}
	
	public void setTeam(String team) {
		this.team = team;
	}
	
	public SetData getConversationIds() {
		return conversationIds;
	}
	
	public void setConversationIds(SetData conversationIds) {
		this.conversationIds = conversationIds;
	}
	
	public SetData getUsersDirectFistbumpSent() {
		return usersDirectFistbumpSent;
	}
	
	public void setUsersDirectFistbumpSent(SetData usersDirectFistbumpSent) {
		this.usersDirectFistbumpSent = usersDirectFistbumpSent;
	}
	
	public SetData getUsersDirectFistbumpReceived() {
		return usersDirectFistbumpReceived;
	}
	
	public void setUsersDirectFistbumpReceived(SetData usersDirectFistbumpReceived) {
		this.usersDirectFistbumpReceived = usersDirectFistbumpReceived;
	}
	
	public int getAge() {
		return age;
	}
	
	public void setAge(int age) {
		this.age = age;
	}
	
	public int getFollowersCount() {
		return followersCount;
	}
	
	public void setFollowersCount(int followersCount) {
		this.followersCount = followersCount;
	}
	
	public int getFollowingCount() {
		return followingCount;
	}
	
	public void setFollowingCount(int followingCount) {
		this.followingCount = followingCount;
	}
	
	public int getSentFistbumpsCount() {
		return sentFistbumpsCount;
	}
	
	public void setSentFistbumpsCount(int sentFistbumpsCount) {
		this.sentFistbumpsCount = sentFistbumpsCount;
	}
	
	public int getReceivedFistbumpsCount() {
		return receivedFistbumpsCount;
	}
	
	public void setReceivedFistbumpsCount(int receivedFistbumpsCount) {
		this.receivedFistbumpsCount = receivedFistbumpsCount;
	}
	
	public int getPostsCount() {
		return postsCount;
	}
	
	public void setPostsCount(int postsCount) {
		this.postsCount = postsCount;
	}
	
	public int getPlayerIndex() {
		return playerIndex;
	}
	
	public void setPlayerIndex(int playerIndex) {
		this.playerIndex = playerIndex;
	}
	
	public long getLastLoggedInTimestampInMs() {
		return lastLoggedInTimestampInMs;
	}
	
	public void setLastLoggedInTimestampInMs(long lastLoggedInTimestampInMs) {
		this.lastLoggedInTimestampInMs = lastLoggedInTimestampInMs;
	}
	
	public boolean isHasNewMessage() {
		return hasNewMessage;
	}
	
	public void setHasNewMessage(boolean hasNewMessage) {
		this.hasNewMessage = hasNewMessage;
	}
	
	public boolean isHasNewNotification() {
		return hasNewNotification;
	}
	
	public void setHasNewNotification(boolean hasNewNotification) {
		this.hasNewNotification = hasNewNotification;
	}
	
	public boolean isNewUser() {
		return newUser;
	}
	
	public void setNewUser(boolean newUser) {
		this.newUser = newUser;
	}
	
	public boolean isVarsityPlayer() {
		return isVarsityPlayer;
	}
	
	public void setVarsityPlayer(boolean varsityPlayer) {
		isVarsityPlayer = varsityPlayer;
	}
	
	public NotificationsPref getNotificationsPref() {
		NotificationsPrefJSONMarshaller jsonMarshaller = new NotificationsPrefJSONMarshaller();
		return jsonMarshaller.unmarshall(NotificationsPref.class, notificationsPref);
	}
	
	public FeedbackContainer getFeedbacks() {
		FeedbacksJSONMarshaller jsonMarshaller = new FeedbacksJSONMarshaller();
		return jsonMarshaller.unmarshall(FeedbackContainer.class, feedbacks);
	}
}
