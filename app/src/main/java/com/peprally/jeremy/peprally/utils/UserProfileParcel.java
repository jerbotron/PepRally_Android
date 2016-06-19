package com.peprally.jeremy.peprally.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.peprally.jeremy.peprally.db_models.DBUserPost;

public class UserProfileParcel implements Parcelable {
    // Activity Data
    ActivityEnum currentActivity;

    // General User Data
    String firstname;
    String lastname;
    String nickname;
    String cognitoID;
    String facebookID;
    Integer followersCount;
    Integer followingCount;
    Integer fistbumpsCount;
    Integer postsCount;
    String favoriteTeam;
    String favoritePlayer;
    String pepTalk;
    String trashTalk;
    Boolean isVarsityPlayer;
    Boolean isSelfProfile;

    // Varsity-specific User Data
    String team;
    Integer index;
    Integer number;
    String year;
    String height;
    String weight;
    String position;
    String hometown;
    String rosterImageURL;
    Boolean hasUserProfile;

    // HomeActivity Constructor, only used to initialize a few required members
    public UserProfileParcel(ActivityEnum currentActivity,
                             String firstname,
                             String lastname,
                             String nickname,
                             String facebookID,
                             Boolean isSelfProfile)
    {
        this.currentActivity = currentActivity;
        this.firstname = firstname;
        this.lastname = lastname;
        this.nickname = nickname;
        this.facebookID = facebookID;
        this.isSelfProfile = isSelfProfile;

        // Initialize un-used integer members to temporary invalid value
        this.followersCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.followingCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.fistbumpsCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.postsCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.index = Helpers.INTEGER_INVALID;
        this.number = Helpers.INTEGER_INVALID;
        // Initialize un-used boolean members to temporary false value
        this.isVarsityPlayer = false;
        this.hasUserProfile = false;
    }

    // FavoritePlayerActivity Constructor, only used to initialize a few required members
    public UserProfileParcel(ActivityEnum currentActivity,
                             String firstname,
                             String team,
                             Integer index,
                             Boolean isSelfProfile)
    {
        this.currentActivity = currentActivity;
        this.firstname = firstname;
        this.team = team;
        this.index = index;
        this.isSelfProfile = isSelfProfile;
        this.isVarsityPlayer = true;

        // Initialize integer values to invalid value
        this.followersCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.followingCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.fistbumpsCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.postsCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.number = Helpers.INTEGER_INVALID;
        // Initialize un-used boolean members to temporary false value
        this.hasUserProfile = false;
    }

    // Post Adapter Constructor, only used to initialize a few required members
    public UserProfileParcel(ActivityEnum currentActivity,
                             DBUserPost userPost)
    {
        this.currentActivity = currentActivity;
        this.firstname = userPost.getFirstname();
        this.nickname = userPost.getNickname();
        this.facebookID = userPost.getFacebookID();
        this.cognitoID = userPost.getCognitoID();
        this.isSelfProfile = false;

        // Initialize integer values to invalid value
        this.followersCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.followingCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.fistbumpsCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.postsCount = Helpers.INTEGER_DEFAULT_COUNT;
        this.index = Helpers.INTEGER_INVALID;
        this.number = Helpers.INTEGER_INVALID;
        // Initialize un-used boolean members to temporary false value
        this.hasUserProfile = false;
        this.isVarsityPlayer = false;
    }

    // Parcel Constructor
    private UserProfileParcel(Parcel in) {
        this.currentActivity = ActivityEnum.fromString(in.readString());
        this.firstname = in.readString();
        this.lastname = in.readString();
        this.nickname = in.readString();
        this.cognitoID = in.readString();
        this.facebookID = in.readString();
        this.followersCount = in.readInt();
        this.followingCount = in.readInt();
        this.fistbumpsCount = in.readInt();
        this.postsCount = in.readInt();
        this.favoriteTeam = in.readString();
        this.favoritePlayer = in.readString();
        this.pepTalk = in.readString();
        this.trashTalk = in.readString();
        this.isVarsityPlayer = in.readByte() != 0;
        this.isSelfProfile = in.readByte() != 0;

        this.team = in.readString();
        this.index = in.readInt();
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
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(nickname);
        dest.writeString(cognitoID);
        dest.writeString(facebookID);
        dest.writeInt(followersCount);
        dest.writeInt(followingCount);
        dest.writeInt(fistbumpsCount);
        dest.writeInt(postsCount);
        dest.writeString(favoriteTeam);
        dest.writeString(favoritePlayer);
        dest.writeString(pepTalk);
        dest.writeString(trashTalk);
        dest.writeByte((byte) (isVarsityPlayer ? 1 : 0));
        dest.writeByte((byte) (isSelfProfile ? 1 : 0));

        dest.writeString(team);
        dest.writeInt(index);
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
    public ActivityEnum getCurrentActivity() { return currentActivity; }
    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public String getNickname() {
        return nickname;
    }
    public String getCognitoID() {
        return cognitoID;
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
    public Integer getFistbumpsCount() {
        return fistbumpsCount;
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
    public Boolean getIsVarsityPlayer() {
        return isVarsityPlayer;
    }
    public Boolean getIsSelfProfile() {
        return isSelfProfile;
    }
    // Varsity-specific Get Methods
    public String getTeam() {
        return team;
    }
    public Integer getIndex() {
        return index;
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
    public void setCurrentActivity(ActivityEnum currentActivity) { this.currentActivity = currentActivity; }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setCognitoID(String cognitoID) {
        this.cognitoID = cognitoID;
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
    public void setFistbumpsCount(Integer fistbumpsCount) {
        this.fistbumpsCount = fistbumpsCount;
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
    public void setIndex(Integer index) {
        this.index = index;
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
