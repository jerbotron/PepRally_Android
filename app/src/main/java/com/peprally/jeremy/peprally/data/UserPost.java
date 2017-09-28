package com.peprally.jeremy.peprally.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.db_models.json_marshallers.CommentsJSONMarshaller;

import java.util.ArrayList;
import java.util.List;

public class UserPost implements Parcelable {

    private String username;
    private String postId;
    private String cognitoId;
    private String facebookId;
    private String firstname;
    private String postText;
    private Long timestampSeconds;
    private int fistbumpsCount;
    private int commentsCount;
    private SetData fistbumpedUsers;
    private List<Comment> comments;

    // Default Constructors

    // Empty Constructor for queries
    public UserPost() {}

    // New PostLikeResponse Constructor
    public UserPost(String username,
                      String postId,
                      String cognitoId,
                      String facebookId,
                      String firstname,
                      String postText,
                      long timestampSeconds)
    {
        this.username = username;
        this.postId = postId;
        this.cognitoId = cognitoId;
        this.facebookId = facebookId;
        this.firstname = firstname;
        this.postText = postText;
        this.timestampSeconds = timestampSeconds;
        this.fistbumpsCount = 0;
        this.commentsCount = 0;
        this.comments = new ArrayList<>();
    }

    // Helpers
    public void addFistbumpedUser(String user) {
        if (fistbumpedUsers == null) {
            fistbumpedUsers = new SetData(user);
        } else {
            fistbumpedUsers.addItem(user);
        }
    }

    public void removeFistbumpedUser(String user) {
        if (fistbumpedUsers != null) {
            fistbumpedUsers.removeItem(user);
            if (fistbumpedUsers.isEmpty()) {
                fistbumpedUsers = null;
            }
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        commentsCount++;
    }

    public void deleteCommentAt(int index) {
        comments.remove(index);
        commentsCount--;
    }

    public boolean hasComment(String commentId) {
        if (comments != null) {
            for (Comment comment : comments) {
                if (comment.getCommentId().equals(commentId)) return true;
            }
        }
        return false;
    }

    // Parcelable Methods
    private UserPost(Parcel in) {
        this.username = in.readString();
        this.postId = in.readString();
        this.cognitoId = in.readString();
        this.facebookId = in.readString();
        this.firstname = in.readString();
        this.postText = in.readString();
        this.timestampSeconds = in.readLong();
        this.fistbumpsCount = in.readInt();
        this.commentsCount = in.readInt();
        if (in.readByte() == 1) {
            this.fistbumpedUsers = new SetData(in);
        }
        this.comments = CommentsJSONMarshaller.decodeJSONComments(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(postId);
        dest.writeString(cognitoId);
        dest.writeString(facebookId);
        dest.writeString(firstname);
        dest.writeString(postText);
        dest.writeLong(timestampSeconds);
        dest.writeInt(fistbumpsCount);
        dest.writeInt(commentsCount);
        if (fistbumpedUsers != null) {
            dest.writeByte((byte) 1);
            dest.writeStringArray(fistbumpedUsers.toArray());
        }
        else {
            dest.writeByte((byte) 0);
        }
        dest.writeString(CommentsJSONMarshaller.convertCommentsToJSON(comments));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<UserPost> CREATOR = new Parcelable.Creator<UserPost>() {
        @Override
        public UserPost createFromParcel(Parcel source) {
            return new UserPost(source);
        }

        @Override
        public UserPost[] newArray(int size) {
            return new UserPost[size];
        }
    };

    // Getters/Setters
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
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

    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getPostText() {
        return postText;
    }
    public void setPostText(String postText) {
        this.postText = postText;
    }

    public long getTimestampSeconds() {
        return timestampSeconds;
    }
    public void setTimestampSeconds(long timestampSeconds) {
        this.timestampSeconds = timestampSeconds;
    }

    public int getFistbumpsCount() {
        return fistbumpsCount;
    }
    public void setFistbumpsCount(int fistbumpsCount) {
        this.fistbumpsCount = fistbumpsCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public SetData getFistbumpedUsers() {
        return fistbumpedUsers;
    }
    public void setFistbumpedUsers(SetData fistbumpedUsers) {
        this.fistbumpedUsers = fistbumpedUsers;
    }

    public List<Comment> getComments() {
        return comments;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
