package com.peprally.jeremy.peprally.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;

public class UserProfileParcel implements Parcelable {
    // Activity Data
    private ActivityEnum currentActivity;
    private String curUserNickname;

    // General User Data
    private String firstname;
    private String lastname;
    private String profileNickname;
    private String cognitoID;
    private String facebookID;
    private String FMSInstanceID;
    private Integer followersCount;
    private Integer followingCount;
    private Integer fistbumpsCount;
    private Integer postsCount;
    private String favoriteTeam;
    private String favoritePlayer;
    private String pepTalk;
    private String trashTalk;
    private Boolean isVarsityPlayer;
    private Boolean isSelfProfile;

    // Varsity-specific User Data
    private String team;
    private Integer index;
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
                             DBUserProfile userProfile,
                             DBPlayerProfile playerProfile) {
        this.currentActivity = currentActivity;
        this.curUserNickname = userProfile.getNickname();
        this.firstname = userProfile.getFirstName();
        this.lastname = userProfile.getLastName();
        this.profileNickname = userProfile.getNickname();
        this.cognitoID = userProfile.getCognitoId();
        this.facebookID = userProfile.getFacebookID();
        this.FMSInstanceID = userProfile.getFMSInstanceID();
        this.followersCount = userProfile.getFollowersCount();
        this.followingCount = userProfile.getFollowingCount();
        this.fistbumpsCount = userProfile.getFistbumpsCount();
        this.postsCount = userProfile.getPostsCount();
        this.favoriteTeam = userProfile.getFavoriteTeam();
        this.favoritePlayer = userProfile.getFavoritePlayer();
        this.pepTalk = userProfile.getPepTalk();
        this.trashTalk = userProfile.getTrashTalk();
        this.isVarsityPlayer = userProfile.getIsVarsityPlayer();
        this.isSelfProfile = true;

        if (this.isVarsityPlayer && playerProfile != null) {
            this.team = playerProfile.getTeam();
            this.index = playerProfile.getIndex();
            this.number = playerProfile.getNumber();
            this.height = playerProfile.getHeight();
            this.weight = playerProfile.getWeight();
            this.position = playerProfile.getPosition();
            this.hometown = playerProfile.getHometown();
            this.rosterImageURL = playerProfile.getHometown();
            this.hasUserProfile = true;
        }
        else {
            this.index = Helpers.INTEGER_INVALID;
            this.number = Helpers.INTEGER_INVALID;
            this.hasUserProfile = false;
        }
    }

    // HomeActivity Constructor, only used to initialize a few required members
    public UserProfileParcel(ActivityEnum currentActivity,
                             String curUserNickname,
                             String firstname,
                             String lastname,
                             String profileNickname,
                             String facebookID,
                             Boolean isSelfProfile)
    {
        this.currentActivity = currentActivity;
        this.curUserNickname = curUserNickname;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profileNickname = profileNickname;
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
                             String curUserNickname,
                             String firstname,
                             String team,
                             Integer index,
                             Boolean isSelfProfile)
    {
        this.currentActivity = currentActivity;
        this.curUserNickname = curUserNickname;
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
                             String curUserNickname,
                             DBUserPost userPost)
    {
        this.currentActivity = currentActivity;
        this.curUserNickname = curUserNickname;
        this.firstname = userPost.getFirstname();
        this.profileNickname = userPost.getNickname();
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
        this.curUserNickname = in.readString();
        this.firstname = in.readString();
        this.lastname = in.readString();
        this.profileNickname = in.readString();
        this.cognitoID = in.readString();
        this.facebookID = in.readString();
        this.FMSInstanceID = in.readString();
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
        dest.writeString(curUserNickname);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(profileNickname);
        dest.writeString(cognitoID);
        dest.writeString(facebookID);
        dest.writeString(FMSInstanceID);
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
    public ActivityEnum getCurrentActivity() {
        return currentActivity;
    }
    public String getCurUserNickname() {
        return curUserNickname;
    }
    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public String getProfileNickname() {
        return profileNickname;
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
    public void setCurrentActivity(ActivityEnum currentActivity) {
        this.currentActivity = currentActivity;
    }
    public void setCurUserNickname(String curUserNickname) {
        this.curUserNickname = curUserNickname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public void setProfileNickname(String profileNickname) {
        this.profileNickname = profileNickname;
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
