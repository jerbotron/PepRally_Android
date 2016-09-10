package com.peprally.jeremy.peprally.db_models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.db_models.json_marshallers.CommentsJSONMarshaller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@DynamoDBTable(tableName = "UserPosts")
public class DBUserPost implements Parcelable{
    private String username;
    private String postId;
    private String cognitoId;
    private String facebookId;
    private String firstname;
    private String postText;
    private Long timestampSeconds;
    private int fistbumpsCount;
    private int commentsCount;
    private Set<String> fistbumpedUsers;
    private ArrayList<Comment> comments;

    // Default Constructors

    // Empty Constructor for queries
    public DBUserPost() {}

    // New Post Constructor
    public DBUserPost(String username,
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
            fistbumpedUsers = new HashSet<>(Collections.singletonList(user));
        } else {
            fistbumpedUsers.add(user);
        }
    }

    public void removeFistbumpedUser(String user) {
        if (fistbumpedUsers != null) {
            fistbumpedUsers.remove(user);
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
                Log.d("DH: ", "comment id = " + comment.getCommentId());
                if (comment.getCommentId().equals(commentId)) return true;
            }
        }
        return false;
    }

    // Parcelable Methods
    private DBUserPost(Parcel in) {
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
            this.fistbumpedUsers = new HashSet<>(Arrays.asList(in.createStringArray()));
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
            dest.writeStringArray(fistbumpedUsers.toArray(new String[fistbumpedUsers.size()]));
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

    public static final Parcelable.Creator<DBUserPost> CREATOR = new Parcelable.Creator<DBUserPost>() {
        @Override
        public DBUserPost createFromParcel(Parcel source) {
            return new DBUserPost(source);
        }

        @Override
        public DBUserPost[] newArray(int size) {
            return new DBUserPost[size];
        }
    };

    // Getters/Setters
    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBAttribute(attributeName = "PostId")
    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }

    @DynamoDBAttribute(attributeName = "CognitoId")
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

    @DynamoDBAttribute(attributeName = "Firstname")
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @DynamoDBAttribute(attributeName = "TextContent")
    public String getPostText() {
        return postText;
    }
    public void setPostText(String postText) {
        this.postText = postText;
    }

    @DynamoDBRangeKey(attributeName = "TimestampSeconds")
    public long getTimestampSeconds() {
        return timestampSeconds;
    }
    public void setTimestampSeconds(long timestampSeconds) {
        this.timestampSeconds = timestampSeconds;
    }

    @DynamoDBAttribute(attributeName = "FistbumpsCount")
    public int getFistbumpsCount() {
        return fistbumpsCount;
    }
    public void setFistbumpsCount(int fistbumpsCount) {
        this.fistbumpsCount = fistbumpsCount;
    }

    @DynamoDBAttribute(attributeName = "CommentsCount")
    public int getCommentsCount() {
        return commentsCount;
    }
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    @DynamoDBAttribute(attributeName = "FistbumpedUsers")
    public Set<String> getFistbumpedUsers() {
        return fistbumpedUsers;
    }
    public void setFistbumpedUsers(Set<String> fistbumpedUsers) {
        this.fistbumpedUsers = fistbumpedUsers;
    }

    @DynamoDBAttribute(attributeName = "CommentsJson")
    @DynamoDBMarshalling (marshallerClass = CommentsJSONMarshaller.class)
    public ArrayList<Comment> getComments() {
        return comments;
    }
    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }
}
