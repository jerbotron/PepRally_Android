package com.peprally.jeremy.peprally.custom;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Comment implements Parcelable, Comparable<Comment>{
    private String postId;
    private String commentId;
    private String commentUsername;
    private String commentFirstname;
    private String postUsername;
    private String facebookId;
    private String commentText;
    private Long timestampSeconds;
    private int fistbumpsCount;
    private Set<String> fistbumpedUsers;

    // Constructors
    public Comment(String postId,
                   String commentId,
                   String commentUsername,
                   String commentFirstname,
                   String postUsername,
                   String facebookId,
                   String commentText,
                   Long timestampSeconds,
                   int fistbumpsCount)
    {
        this.postId = postId;
        this.commentId = commentId;
        this.commentUsername = commentUsername;
        this.commentFirstname = commentFirstname;
        this.postUsername = postUsername;
        this.facebookId = facebookId;
        this.commentText = commentText;
        this.timestampSeconds = timestampSeconds;
        this.fistbumpsCount = fistbumpsCount;
        this.fistbumpedUsers = null;
    }

    /**
     * JSON Helpers
     */
    public Comment(JSONObject jsonComment) {
        try {
            postId = jsonComment.getString("post_id");
            commentId = jsonComment.getString("comment_id");
            commentUsername = jsonComment.getString("comment_username");
            commentFirstname = jsonComment.getString("comment_firstname");
            postUsername = jsonComment.getString("post_username");
            facebookId = jsonComment.getString("facebook_id");
            commentText = jsonComment.getString("comment_text");
            timestampSeconds = jsonComment.getLong("timestamp_seconds");
            fistbumpsCount = jsonComment.getInt("fistbumps_count");
            JSONArray jsonFistbumpedUsers = jsonComment.getJSONArray("fistbumped_users");
            fistbumpedUsers = new HashSet<>();
            for (int i = 0; i < jsonFistbumpedUsers.length(); i++) {
                fistbumpedUsers.add(jsonFistbumpedUsers.getString(i));
            }
        } catch (JSONException e) {
            if (e.getMessage().equals("No value for fistbumped_users"))
                fistbumpedUsers = null;
            else
                e.printStackTrace();
        }
    }

    @Override
    public int compareTo(@NonNull Comment comment) {
        return this.timestampSeconds.compareTo(comment.timestampSeconds);
    }

    public JSONObject toJSONObject() {
        JSONObject jsonComment = new JSONObject();
        try {
            jsonComment.put("post_id", postId);
            jsonComment.put("comment_id", commentId);
            jsonComment.put("comment_username", commentUsername);
            jsonComment.put("comment_firstname", commentFirstname);
            jsonComment.put("post_username", postUsername);
            jsonComment.put("facebook_id", facebookId);
            jsonComment.put("comment_text", commentText);
            jsonComment.put("timestamp_seconds", timestampSeconds);
            jsonComment.put("fistbumps_count", fistbumpsCount);
            if (fistbumpedUsers != null && fistbumpedUsers.size() > 0) {
                JSONArray jsonFistbumpedUsersArray = new JSONArray(fistbumpedUsers);
                jsonComment.put("fistbumped_users", jsonFistbumpedUsersArray);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return jsonComment;
    }

    /**
     * Parcelable Constructors
      */
    private Comment(Parcel in) {
        this.postId = in.readString();
        this.commentId = in.readString();
        this.commentUsername = in.readString();
        this.commentFirstname = in.readString();
        this.postUsername = in.readString();
        this.facebookId = in.readString();
        this.commentText = in.readString();
        this.timestampSeconds = in.readLong();
        this.fistbumpsCount = in.readInt();
        this.fistbumpedUsers = new HashSet<>(Arrays.asList(in.createStringArray()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postId);
        dest.writeString(commentId);
        dest.writeString(commentUsername);
        dest.writeString(commentFirstname);
        dest.writeString(postUsername);
        dest.writeString(facebookId);
        dest.writeString(commentText);
        dest.writeLong(timestampSeconds);
        dest.writeInt(fistbumpsCount);
        dest.writeStringArray(fistbumpedUsers.toArray(new String[fistbumpedUsers.size()]));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

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

    public void incrementFistbumpsCount() { fistbumpsCount++; }

    public void decrementFistbumpsCount() { fistbumpsCount--; }

    // Setters/Getters

    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCommentId() {
        return commentId;
    }
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentUsername() {
        return commentUsername;
    }
    public void setCommentUsername(String commentUsername) {
        this.commentUsername = commentUsername;
    }

    public String getCommentFirstname() {
        return commentFirstname;
    }
    public void setCommentFirstname(String commentFirstname) {
        this.commentFirstname = commentFirstname;
    }

    public String getPostUsername() {
        return postUsername;
    }
    public void setPostUsername(String postUsername) {
        this.postUsername = postUsername;
    }

    public String getFacebookId() {
        return facebookId;
    }
    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getCommentText() {
        return commentText;
    }
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Long getTimestampSeconds() {
        return timestampSeconds;
    }
    public void setTimestampSeconds(Long timestampSeconds) {
        this.timestampSeconds = timestampSeconds;
    }

    public int getFistbumpsCount() {
        return fistbumpsCount;
    }
    public void setFistbumpsCount(int fistbumpsCount) {
        this.fistbumpsCount = fistbumpsCount;
    }

    public Set<String> getFistbumpedUsers() {
        return fistbumpedUsers;
    }
    public void setFistbumpedUsers(Set<String> fistbumpedUsers) {
        this.fistbumpedUsers = fistbumpedUsers;
    }
}
