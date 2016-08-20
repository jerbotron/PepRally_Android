package com.peprally.jeremy.peprally.custom.messaging;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.peprally.jeremy.peprally.utils.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage implements Comparable<ChatMessage>, Parcelable{
    private String conversationID;
    private String username;
    private String facebookID;
    private String messageContent;
    private Long timestamp;

    // Constructor
    public ChatMessage(String conversationID,
                       String username,
                       String facebookID,
                       String messageContent)
    {
        this.conversationID = conversationID;
        this.username = username;
        this.facebookID = facebookID;
        this.messageContent = messageContent;
        this.timestamp = Helpers.getTimestampSeconds();
    }

    // JSON Constructor
    public ChatMessage (JSONObject jsonMsg) {
        try {
            conversationID = jsonMsg.getString("conversation_id");
            username = jsonMsg.getString("username");
            facebookID = jsonMsg.getString("facebook_id");
            messageContent = jsonMsg.getString("content");
            timestamp = jsonMsg.getLong("timestamp");
        } catch (JSONException e) { e.printStackTrace(); }
    }

    // Comparator
    @Override
    public int compareTo(@NonNull ChatMessage message) {
        return this.timestamp.compareTo(message.timestamp);
    }

    // Parcelable Constructor
    private ChatMessage(Parcel in) {
        this.conversationID = in.readString();
        this.username = in.readString();
        this.facebookID = in.readString();
        this.messageContent = in.readString();
        this.timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(conversationID);
        parcel.writeString(username);
        parcel.writeString(facebookID);
        parcel.writeString(messageContent);
        parcel.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ChatMessage> CREATOR = new Parcelable.Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel source) {
            return new ChatMessage(source);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    // Getters/Setters
    public String getConversationID() {return conversationID; }
    public void setConversationID(String conversationID) { this.conversationID = conversationID; }

    public String getUsername() {return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFacebookID() {return facebookID; }
    public void setFacebookID(String facebookID) { this.facebookID = facebookID; }

    public String getMessageContent() {return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public Long getTimestamp() {return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

}
