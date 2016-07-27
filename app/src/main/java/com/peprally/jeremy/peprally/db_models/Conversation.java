package com.peprally.jeremy.peprally.db_models;

import java.util.ArrayList;
import java.util.Map;

public class Conversation {
    private ArrayList<Message> messages;
    private Map<String, String> nicknameFacebookIDMap;
    private boolean isEmpty;

    public Conversation(ArrayList<Message> messages, Map<String, String> facebookIDMap) {
        this.messages = messages;
        this.nicknameFacebookIDMap = facebookIDMap;
        this.isEmpty = (messages == null);
    }

    public Map<String, String> getNicknameFacebookIDMap() { return nicknameFacebookIDMap; }
    public void setNicknameFacebookIDMap(Map<String, String> facebookIDMap) { this.nicknameFacebookIDMap = facebookIDMap; }

    public ArrayList<Message> getMessages() { return messages; }
    public void setMessages(ArrayList<Message> messages) { this.messages = messages; }

    public boolean isEmpty() { return isEmpty; }
    public void setEmpty(boolean isEmpty) { this.isEmpty = isEmpty; }
}
