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
	private String lastLoggedInTimestamp;
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
	private long timestampLastLoggedIn;
	private boolean hasNewMessage;
	private boolean hasNewNotification;
	private boolean newUser;
	private boolean isVarsityPlayer;
	private String notificationsPref;
	private String feedbacks;
	
	public String getUsername() {
		return username;
	}
	
	public String getCognitoId() {
		return cognitoId;
	}
	
	public String getFacebookId() {
		return facebookId;
	}
	
	public String getFacebookLink() {
		return facebookLink;
	}
	
	public String getFCMInstanceId() {
		return FCMInstanceId;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public String getGender() {
		return gender;
	}
	
	public String getBirthday() {
		return birthday;
	}
	
	public String getFavoriteTeam() {
		return favoriteTeam;
	}
	
	public String getFavoritePlayer() {
		return favoritePlayer;
	}
	
	public String getPepTalk() {
		return pepTalk;
	}
	
	public String getTrashTalk() {
		return trashTalk;
	}
	
	public String getDateJoined() {
		return dateJoined;
	}
	
	public String getLastLoggedInTimestamp() {
		return lastLoggedInTimestamp;
	}
	
	public String getSchoolName() {
		return schoolName;
	}
	
	public String getTeam() {
		return team;
	}
	
	public SetData getConversationIds() {
		return conversationIds;
	}
	
	public SetData getUsersDirectFistbumpSent() {
		return usersDirectFistbumpSent;
	}
	
	public SetData getUsersDirectFistbumpReceived() {
		return usersDirectFistbumpReceived;
	}
	
	public int getAge() {
		return age;
	}
	
	public int getFollowersCount() {
		return followersCount;
	}
	
	public int getFollowingCount() {
		return followingCount;
	}
	
	public int getSentFistbumpsCount() {
		return sentFistbumpsCount;
	}
	
	public int getReceivedFistbumpsCount() {
		return receivedFistbumpsCount;
	}
	
	public int getPostsCount() {
		return postsCount;
	}
	
	public int getPlayerIndex() {
		return playerIndex;
	}
	
	public long getTimestampLastLoggedIn() {
		return timestampLastLoggedIn;
	}
	
	public boolean isHasNewMessage() {
		return hasNewMessage;
	}
	
	public boolean isHasNewNotification() {
		return hasNewNotification;
	}
	
	public boolean isNewUser() {
		return newUser;
	}
	
	public boolean isVarsityPlayer() {
		return isVarsityPlayer;
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
