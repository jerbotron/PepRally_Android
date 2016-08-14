package com.peprally.jeremy.peprally.custom.messaging;

import android.support.annotation.NonNull;

import com.peprally.jeremy.peprally.utils.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage implements Comparable<ChatMessage>{
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
