package com.peprally.jeremy.peprally.db_models;

import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.JsonMarshaller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConversationJSONMarshaller extends JsonMarshaller<Conversation> implements DynamoDBMarshaller<Conversation>{

    @Override
    public String marshall(Conversation conversation) {
        JSONObject jsonConversation = new JSONObject();
        try {
            ArrayList<Message> messages = conversation.getMessages();
            if (messages != null) {
                JSONArray jsonMessagesArray = new JSONArray();
                for (Message msg : messages) {
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("conversation_id", msg.getConversationID());
                    jsonMessage.put("nickname", msg.getNickname());
                    jsonMessage.put("content", msg.getContent());
                    jsonMessage.put("timestamp", msg.getTimestamp());
                    jsonMessagesArray.put(jsonMessage);
                }
                // put messages array into top level conversation json key
                jsonConversation.put("conversation", jsonMessagesArray);
            }

            jsonConversation.put("isEmpty", conversation.isEmpty());
            JSONObject jsonUserFacebookIDs = new JSONObject();
            Map<String, String> nicknameFacebookIDMap = conversation.getNicknameFacebookIDMap();
            if (nicknameFacebookIDMap != null) {
                for (String nickname : nicknameFacebookIDMap.keySet()) {
                    jsonUserFacebookIDs.put(nickname, nicknameFacebookIDMap.get(nickname));
                }
                jsonConversation.put("user_facebook_ids", jsonUserFacebookIDs);
            }
        } catch (JSONException e) { e.printStackTrace(); }

        return jsonConversation.toString();
    }

    @Override
    public Conversation unmarshall(Class<Conversation> clazz, String json) {
        ArrayList<Message> messages = new ArrayList<>();
        Map<String, String> nicknameFacebookIDMap = new HashMap<>();
        try {
            JSONObject jsonConversation = new JSONObject(json);
            JSONArray jsonMessages = jsonConversation.getJSONArray("conversation");
            for (int i = 0; i < jsonMessages.length(); i++) {
                JSONObject jsonMsg = jsonMessages.getJSONObject(i);
                Message msg = new Message(jsonMsg);
                messages.add(msg);
            }
            JSONObject jsonFacebookIDs = jsonConversation.getJSONObject("user_facebook_ids");
            for (int i = 0; i < jsonFacebookIDs.length(); i++) {
                String nickname = jsonFacebookIDs.names().get(i).toString();
                nicknameFacebookIDMap.put(nickname, jsonFacebookIDs.getString(nickname));
            }
        }
        catch (JSONException e) { e.printStackTrace(); }

        return new Conversation(messages, nicknameFacebookIDMap);
    }
}
