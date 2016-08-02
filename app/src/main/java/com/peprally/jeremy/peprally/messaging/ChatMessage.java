package com.peprally.jeremy.peprally.messaging;

import android.support.annotation.NonNull;

import com.peprally.jeremy.peprally.utils.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage implements Comparable<ChatMessage>{
    private String conversationID;
    private String nickname;
    private String facebookID;
    private String messageContent;
    private Long timestamp;

    // Constructor
    public ChatMessage(String conversationID,
                       String nickname,
                       String facebookID,
                       String messageContent)
    {
        this.conversationID = conversationID;
        this.nickname = nickname;
        this.facebookID = facebookID;
        this.messageContent = messageContent;
        this.timestamp = Helpers.getTimestampMiliseconds();
    }

    // JSON Constructor
    public ChatMessage (JSONObject jsonMsg) {
        try {
            conversationID = jsonMsg.getString("conversation_id");
            nickname = jsonMsg.getString("nickname");
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

    public String getNickname() {return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getFacebookID() {return facebookID; }
    public void setFacebookID(String facebookID) { this.facebookID = facebookID; }

    public String getMessageContent() {return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public Long getTimestamp() {return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

}
