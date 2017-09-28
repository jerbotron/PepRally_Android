package com.peprally.jeremy.peprally.custom;

import android.os.Parcel;
import android.os.Parcelable;

import com.peprally.jeremy.peprally.data.PlayerProfile;
import com.peprally.jeremy.peprally.data.UserPost;
import com.peprally.jeremy.peprally.data.UserProfile;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;

import static com.peprally.jeremy.peprally.utils.Constants.INTEGER_DEFAULT_COUNT;
import static com.peprally.jeremy.peprally.utils.Constants.INTEGER_INVALID;

public class UserProfileParcel implements Parcelable {
    // Activity Data
    private ActivityEnum currentActivity;
    private String currentUsername;

    // General User Data
    private String firstname;
    private String lastname;
    private String profileUsername;
    private String cognitoID;
    private String facebookID;
    private String FMSInstanceID;
    private Integer followersCount;
    private Integer followingCount;
    private Integer sentFistbumpsCount;
    private Integer receivedFistbumpsCount;
    private Integer postsCount;
    private String favoriteTeam;
    private String favoritePlayer;
    private String pepTalk;
    private String trashTalk;
    private String dateJoined;
    private String email;
    private Boolean hasNewMessage;
    private Boolean hasNewNotification;
    private Boolean isVarsityPlayer;
    private Boolean isSelfProfile;

    // Varsity-specific User Data
    private String team;
    private Integer playerIndex;
    private Integer number;
    private String year;
    private String height;
    private String weight;
    private String position;
    private String hometown;
    private String rosterImageURL;
    private Boolean hasUserProfile;

    // LoginActivity Constructor, constructs UserProfileParcel from a UserProfile DB object
    public UserProfileParcel(ActivityEnum currentActivity,
                             UserProfile userProfile,
                             PlayerProfile playerProfile) {
        this.currentActivity = currentActivity;
        this.currentUsername = userProfile.getUsername();
        this.firstname = userProfile.getFirstname();
        this.lastname = userProfile.getLastname();
        this.profileUsername = userProfile.getUsername();
        this.cognitoID = userProfile.getCognitoId();
        this.facebookID = userProfile.getFacebookId();
        this.FMSInstanceID = userProfile.getFcmInstanceId();
        this.followersCount = userProfile.getFollowersCount();
        this.followingCount = userProfile.getFollowingCount();
        this.sentFistbumpsCount = userProfile.getSentFistbumpsCount();
        this.receivedFistbumpsCount = userProfile.getReceivedFistbumpsCount();
        this.postsCount = userProfile.getPostsCount();
        this.favoriteTeam = userProfile.getFavoriteTeam();
        this.favoritePlayer = userProfile.getFavoritePlayer();
        this.pepTalk = userProfile.getPepTalk();
        this.trashTalk = userProfile.getTrashTalk();
        this.dateJoined = userProfile.getDateJoinedUtc();
        this.email = userProfile.getEmail();
        this.hasNewMessage = userProfile.isHasNewMessage();
        this.hasNewNotification = userProfile.isHasNewNotification();
        this.isVarsityPlayer = userProfile.isVarsityPlayer();
        this.isSelfProfile = true;

        if (this.isVarsityPlayer && playerProfile != null) {
            this.team = playerProfile.getTeam();
            this.playerIndex = playerProfile.getIndex();
            this.number = playerProfile.getNumber();
            this.height = playerProfile.getHeight();
            this.weight = playerProfile.getWeight();
            this.position = playerProfile.getPosition();
            this.hometown = playerProfile.getHometown();
            this.rosterImageURL = playerProfile.getHometown();
            this.hasUserProfile = true;
        }
        else {
            this.playerIndex = INTEGER_INVALID;
            this.number = INTEGER_INVALID;
            this.hasUserProfile = false;
        }
    }

    // HomeActivity Constructor, only used to initialize a few required members
    public UserProfileParcel(ActivityEnum currentActivity,
                             String currentUsername,
                             String firstname,
                             String lastname,
                             String profileUsername,
                             String facebookID,
                             Boolean isSelfProfile)
    {
        this.currentActivity = currentActivity;
        this.currentUsername = currentUsername;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profileUsername = profileUsername;
        this.facebookID = facebookID;
        this.isSelfProfile = isSelfProfile;

        // Initialize un-used integer members to temporary invalid value
        this.followersCount = INTEGER_DEFAULT_COUNT;
        this.followingCount = INTEGER_DEFAULT_COUNT;
        this.sentFistbumpsCount = INTEGER_DEFAULT_COUNT;
        this.receivedFistbumpsCount = INTEGER_DEFAULT_COUNT;
        this.postsCount = INTEGER_DEFAULT_COUNT;
        this.playerIndex = INTEGER_INVALID;
        this.number = INTEGER_INVALID;
        // Initialize un-used boolean members to temporary false value
        this.hasNewMessage = false;
        this.hasNewNotification = false;
        this.isVarsityPlayer = false;
        this.hasUserProfile = false;
    }

    // BrowsePlayersActivity Constructor, only used to initialize a few required members
    public UserProfileParcel(ActivityEnum currentActivity,
                             String currentUsername,
                             String firstname,
                             String team,
                             Integer playerIndex,
                             Boolean isSelfProfile)
    {
        this.currentActivity = currentActivity;
        this.currentUsername = currentUsername;
        this.firstname = firstname;
        this.team = team;
        this.playerIndex = playerIndex;
        this.isSelfProfile = isSelfProfile;
        this.isVarsityPlayer = true;

        // Initialize integer values to invalid value
        this.followersCount = INTEGER_DEFAULT_COUNT;
        this.followingCount = INTEGER_DEFAULT_COUNT;
        this.sentFistbumpsCount = INTEGER_DEFAULT_COUNT;
        this.receivedFistbumpsCount = INTEGER_DEFAULT_COUNT;
        this.postsCount = INTEGER_DEFAULT_COUNT;
        this.number = INTEGER_INVALID;
        // Initialize un-used boolean members to temporary false value
        this.hasNewMessage = false;
        this.hasNewNotification = false;
        this.hasUserProfile = false;
    }

    // PostLikeResponse Adapter Constructor, only used to initialize a few required members
    public UserProfileParcel(ActivityEnum currentActivity,
                             String currentUsername,
                             UserPost userPost)
    {
        this.currentActivity = currentActivity;
        this.currentUsername = currentUsername;
        this.firstname = userPost.getFirstname();
        this.profileUsername = userPost.getUsername();
        this.facebookID = userPost.getFacebookId();
        this.cognitoID = userPost.getCognitoId();
        this.isSelfProfile = false;

        // Initialize integer values to invalid value
        this.followersCount = INTEGER_DEFAULT_COUNT;
        this.followingCount = INTEGER_DEFAULT_COUNT;
        this.sentFistbumpsCount = INTEGER_DEFAULT_COUNT;
        this.receivedFistbumpsCount = INTEGER_DEFAULT_COUNT;
        this.postsCount = INTEGER_DEFAULT_COUNT;
        this.playerIndex = INTEGER_INVALID;
        this.number = INTEGER_INVALID;
        // Initialize un-used boolean members to temporary false value
        this.hasNewMessage = false;
        this.hasNewNotification = false;
        this.hasUserProfile = false;
        this.isVarsityPlayer = false;
    }

    // Comment Adapter Constructor, only used to initialize a few required members
    public UserProfileParcel(ActivityEnum currentActivity,
                             String currentUsername,
                             Comment userComment)
    {
        this.currentActivity = currentActivity;
        this.currentUsername = currentUsername;
        this.firstname = userComment.getCommentFirstname();
        this.profileUsername = userComment.getCommentUsername();
        this.facebookID = userComment.getFacebookId();
        this.isSelfProfile = false;

        // Initialize integer values to invalid value
        this.followersCount = INTEGER_DEFAULT_COUNT;
        this.followingCount = INTEGER_DEFAULT_COUNT;
        this.sentFistbumpsCount = INTEGER_DEFAULT_COUNT;
        this.receivedFistbumpsCount = INTEGER_DEFAULT_COUNT;
        this.postsCount = INTEGER_DEFAULT_COUNT;
        this.playerIndex = INTEGER_INVALID;
        this.number = INTEGER_INVALID;
        // Initialize un-used boolean members to temporary false value
        this.hasNewMessage = false;
        this.hasNewNotification = false;
        this.hasUserProfile = false;
        this.isVarsityPlayer = false;
    }

    // Parcel Constructor
    private UserProfileParcel(Parcel in) {
        this.currentActivity = ActivityEnum.fromString(in.readString());
        this.currentUsername = in.readString();
        this.firstname = in.readString();
        this.lastname = in.readString();
        this.profileUsername = in.readString();
        this.cognitoID = in.readString();
        this.facebookID = in.readString();
        this.FMSInstanceID = in.readString();
        this.followersCount = in.readInt();
        this.followingCount = in.readInt();
        this.sentFistbumpsCount = in.readInt();
        this.receivedFistbumpsCount = in.readInt();
        this.postsCount = in.readInt();
        this.favoriteTeam = in.readString();
        this.favoritePlayer = in.readString();
        this.pepTalk = in.readString();
        this.trashTalk = in.readString();
        this.dateJoined = in.readString();
        this.email = in.readString();
        this.hasNewMessage = in.readByte() != 0;
        this.hasNewNotification = in.readByte() != 0;
        this.isVarsityPlayer = in.readByte() != 0;
        this.isSelfProfile = in.readByte() != 0;

        this.team = in.readString();
        this.playerIndex = in.readInt();
        this.number = in.readInt();
        this.year = in.readString();
        this.height = in.readString();
        this.weight = in.readString();
        this.position = in.readString();
        this.hometown = in.readString();
        this.rosterImageURL = in.readString();
        this.hasUserProfile = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currentActivity.getName());
        dest.writeString(currentUsername);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(profileUsername);
        dest.writeString(cognitoID);
        dest.writeString(facebookID);
        dest.writeString(FMSInstanceID);
        dest.writeInt(followersCount);
        dest.writeInt(followingCount);
        dest.writeInt(sentFistbumpsCount);
        dest.writeInt(receivedFistbumpsCount);
        dest.writeInt(postsCount);
        dest.writeString(favoriteTeam);
        dest.writeString(favoritePlayer);
        dest.writeString(pepTalk);
        dest.writeString(trashTalk);
        dest.writeString(dateJoined);
        dest.writeString(email);
        dest.writeByte((byte) (hasNewMessage ? 1 : 0));
        dest.writeByte((byte) (hasNewNotification ? 1 : 0));
        dest.writeByte((byte) (isVarsityPlayer ? 1 : 0));
        dest.writeByte((byte) (isSelfProfile ? 1 : 0));

        dest.writeString(team);
        dest.writeInt(playerIndex);
        dest.writeInt(number);
        dest.writeString(year);
        dest.writeString(height);
        dest.writeString(weight);
        dest.writeString(position);
        dest.writeString(hometown);
        dest.writeString(rosterImageURL);
        dest.writeByte((byte) (hasUserProfile ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<UserProfileParcel> CREATOR = new Parcelable.Creator<UserProfileParcel>() {
        @Override
        public UserProfileParcel createFromParcel(Parcel source) {
            return new UserProfileParcel(source);
        }

        @Override
        public UserProfileParcel[] newArray(int size) {
            return new UserProfileParcel[size];
        }
    };

    // Get Methods
    public ActivityEnum getCurrentActivity() {
        return currentActivity;
    }
    public String getCurrentUsername() {
        return currentUsername;
    }
    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public String getProfileUsername() {
        return profileUsername;
    }
    public String getCognitoID() {
        return cognitoID;
    }
    public String getFMSInstanceID() {
        return FMSInstanceID;
    }
    public String getFacebookID() {
        return facebookID;
    }
    public Integer getFollowersCount() {
        return followersCount;
    }
    public Integer getFollowingCount() {
        return followingCount;
    }
    public Integer getSentFistbumpsCount() {
        return sentFistbumpsCount;
    }
    public Integer getReceivedFistbumpsCount() {
        return receivedFistbumpsCount;
    }
    public Integer getPostsCount() {
        return postsCount;
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
    public String getEmail() {
        return email;
    }
    public Boolean hasNewMessage() {
        return hasNewMessage;
    }
    public Boolean hasNewNotification() {
        return hasNewNotification;
    }
    public Boolean isVarsityPlayer() {
        return isVarsityPlayer;
    }
    public Boolean isSelfProfile() {
        return isSelfProfile;
    }
    // Varsity-specific Get Methods
    public String getTeam() {
        return team;
    }
    public Integer getPlayerIndex() {
        return playerIndex;
    }
    public Integer getNumber() {
        return number;
    }
    public String getYear() {
        return year;
    }
    public String getHeight() {
        return height;
    }
    public String getWeight() {
        return weight;
    }
    public String getPosition() {
        return position;
    }
    public String getHometown() {
        return hometown;
    }
    public String getRosterImageURL() {
        return rosterImageURL;
    }
    public Boolean getHasUserProfile() {
        return hasUserProfile;
    }

    // Set Methods
    public void setCurrentActivity(ActivityEnum currentActivity) {
        this.currentActivity = currentActivity;
    }
    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public void setProfileUsername(String profileUsername) {
        this.profileUsername = profileUsername;
    }
    public void setCognitoID(String cognitoID) {
        this.cognitoID = cognitoID;
    }
    public void setFMSInstanceID(String FMSInstanceID) {
        this.FMSInstanceID = FMSInstanceID;
    }
    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
    }
    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }
    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }
    public void setSentFistbumpsCount(Integer sentFistbumpsCount) {
        this.sentFistbumpsCount = sentFistbumpsCount;
    }
    public void setReceivedFistbumpsCount(Integer receivedFistbumpsCount) {
        this.receivedFistbumpsCount = receivedFistbumpsCount;
    }
    public void setPostsCount(Integer postsCount) {
        this.postsCount = postsCount;
    }
    public void setFavoriteTeam(String favoriteTeam) {
        this.favoriteTeam = favoriteTeam;
    }
    public void setFavoritePlayer(String favoritePlayer) {
        this.favoritePlayer = favoritePlayer;
    }
    public void setPepTalk(String pepTalk) {
        this.pepTalk = pepTalk;
    }
    public void setTrashTalk(String trashTalk) {
        this.trashTalk = trashTalk;
    }
    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setHasNewMessage(Boolean hasNewMessage) {
        this.hasNewMessage = hasNewMessage;
    }
    public void setHasNewNotification(Boolean hasNewNotification) {
        this.hasNewNotification = hasNewNotification;
    }
    public void setIsVarsityPlayer(Boolean isVarsityPlayer) {
        this.isVarsityPlayer = isVarsityPlayer;
    }
    public void setIsSelfProfile(Boolean isSelfProfile) {
        this.isSelfProfile = isSelfProfile;
    }
    // Varsity-specific Set Methods
    public void setTeam(String team) {
        this.team = team;
    }
    public void setPlayerIndex(Integer playerIndex) {
        this.playerIndex = playerIndex;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
    public void setYear(String year) {
        this.year = year;
    }
    public void setHeight(String height) {
        this.height = height;
    }
    public void setWeight(String weight) {
        this.weight = weight;
    }
    public void setPosition(String position) {
        this.position = position;
    }
    public void setHometown(String hometown) {
        this.hometown = hometown;
    }
    public void setRosterImageURL(String rosterImageURL) {
        this.rosterImageURL = rosterImageURL;
    }
    public void setHasUserProfile(Boolean hasUserProfile) {
        this.hasUserProfile = hasUserProfile;
    }
}
