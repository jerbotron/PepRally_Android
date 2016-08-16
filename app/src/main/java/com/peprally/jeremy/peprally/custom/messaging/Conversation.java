package com.peprally.jeremy.peprally.custom.messaging;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Conversation implements Parcelable{
    private String conversationID;
    private ArrayList<ChatMessage> chatMessages;
    private Map<String, String> usernameFacebookIDMap;

    public Conversation(String conversationID, ArrayList<ChatMessage> chatMessages, Map<String, String> facebookIDMap) {
        this.conversationID = conversationID;
        this.chatMessages = chatMessages;
        this.usernameFacebookIDMap = facebookIDMap;
    }

    public String getConversationID() { return conversationID; }
    public void setConversationID(String conversationID) { this.conversationID = conversationID; }

    public Map<String, String> getUsernameFacebookIDMap() { return usernameFacebookIDMap; }
    public void setUsernameFacebookIDMap(Map<String, String> facebookIDMap) { this.usernameFacebookIDMap = facebookIDMap; }

    public ArrayList<ChatMessage> getChatMessages() {
        ArrayList<ChatMessage> chatMessagesSortedOut = null;
        if (chatMessages != null) {
            chatMessagesSortedOut = chatMessages;
            Collections.sort(chatMessagesSortedOut);
        }
        return chatMessagesSortedOut;
    }
    public void setChatMessages(ArrayList<ChatMessage> chatMessages) { this.chatMessages = chatMessages; }

    public void addChatMessage(ChatMessage message) {
        chatMessages.add(message);
    }

    public String getRecipientUsername(String senderUsername) {
        for (Map.Entry<String, String> entry : usernameFacebookIDMap.entrySet()) {
            String username = entry.getKey();
            if (!username.equals(senderUsername)) {
                return username;
            }
        }
        return null;
    }

    // Parcelable Methods
    private ArrayList<ChatMessage> convertJSONToChatMessagesArrayList(String jsonChatMessages) {
        if (jsonChatMessages != null && !jsonChatMessages.isEmpty()) {
            ArrayList<ChatMessage> chatMessagesOut = new ArrayList<>();
            try {
                JSONArray jsonMessagesArray = new JSONArray(jsonChatMessages);
                for (int i = 0; i < jsonMessagesArray.length(); i++) {
                    JSONObject jsonMessage = jsonMessagesArray.getJSONObject(i);
                    chatMessagesOut.add(new ChatMessage(jsonMessage));
                }
            }
            catch (JSONException e) { e.printStackTrace(); }
            if (chatMessagesOut.size() > 0)
                return chatMessagesOut;
        }
        return null;
    }

    private String convertChatMessagesArrayListToJSON() {
        if (chatMessages != null && chatMessages.size() > 0) {
            JSONArray jsonChatMessages = new JSONArray();
            for (ChatMessage chatMessage : chatMessages) {
                try {
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("conversation_id", chatMessage.getConversationID());
                    jsonMessage.put("username", chatMessage.getUsername());
                    jsonMessage.put("facebook_id", chatMessage.getFacebookID());
                    jsonMessage.put("content", chatMessage.getMessageContent());
                    jsonMessage.put("timestamp", chatMessage.getTimestamp());
                    jsonChatMessages.put(jsonMessage);
                }
                catch (JSONException e) { e.printStackTrace(); }
            }
            return jsonChatMessages.toString();
        }
        return null;
    }

    // Parcel Constructor
    private Conversation(Parcel in) {
        this.conversationID = in.readString();
        this.chatMessages = convertJSONToChatMessagesArrayList(in.readString());
        int mapSize = in.readInt();
        this.usernameFacebookIDMap = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.usernameFacebookIDMap.put(key, value);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(conversationID);
        dest.writeString(convertChatMessagesArrayListToJSON());
        dest.writeInt(usernameFacebookIDMap.size());
        for (Map.Entry<String, String> entry : usernameFacebookIDMap.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Conversation> CREATOR = new Parcelable.Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel source) {
            return new Conversation(source);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };
}
