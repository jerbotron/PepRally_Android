package com.peprally.jeremy.peprally.db_models;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private String conversationID;
    private String nickname;
    private String content;
    private Long timestamp;

    public Message (JSONObject jsonMsg) {
        try {
            conversationID = jsonMsg.getString("conversation_id");
            nickname = jsonMsg.getString("nickname");
            content = jsonMsg.getString("content");
            timestamp = jsonMsg.getLong("timestamp");
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public String getConversationID() {return conversationID; }
    public void setConversationID(String conversationID) { this.conversationID = conversationID; }

    public String getNickname() {return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getContent() {return content; }
    public void setContent(String conversationID) { this.content = content; }

    public Long getTimestamp() {return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

}
