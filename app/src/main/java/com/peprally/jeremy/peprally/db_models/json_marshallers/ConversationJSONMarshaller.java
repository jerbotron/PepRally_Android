package com.peprally.jeremy.peprally.db_models.json_marshallers;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.JsonMarshaller;
import com.peprally.jeremy.peprally.custom.messaging.ChatMessage;
import com.peprally.jeremy.peprally.custom.messaging.Conversation;

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
            jsonConversation.put("conversation_id", conversation.getConversationID());
            ArrayList<ChatMessage> messages = conversation.getChatMessages();
            if (messages != null) {
                JSONArray jsonMessagesArray = new JSONArray();
                for (ChatMessage msg : messages) {
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("conversation_id", msg.getConversationID());
                    jsonMessage.put("username", msg.getUsername());
                    jsonMessage.put("facebook_id", msg.getFacebookID());
                    jsonMessage.put("content", msg.getMessageContent());
                    jsonMessage.put("timestamp", msg.getTimestamp());
                    jsonMessagesArray.put(jsonMessage);
                }
                // put messages array into top level conversation json key
                jsonConversation.put("conversation", jsonMessagesArray);
            }

            JSONObject jsonUserFacebookIDs = new JSONObject();
            Map<String, String> usernameFacebookIDMap = conversation.getUsernameFacebookIDMap();
            if (usernameFacebookIDMap != null) {
                for (String username : usernameFacebookIDMap.keySet()) {
                    jsonUserFacebookIDs.put(username, usernameFacebookIDMap.get(username));
                }
                jsonConversation.put("user_facebook_ids", jsonUserFacebookIDs);
            }
        } catch (JSONException e) { e.printStackTrace(); }

        return jsonConversation.toString();
    }

    @Override
    public Conversation unmarshall(Class<Conversation> clazz, String json) {
        try {
            JSONObject jsonConversation = new JSONObject(json);

            ArrayList<ChatMessage> messages = new ArrayList<>();
            Map<String, String> usernameFacebookIDMap = new HashMap<>();
            String conversationID = jsonConversation.getString("conversation_id");

            JSONArray jsonMessages = jsonConversation.getJSONArray("conversation");
            for (int i = 0; i < jsonMessages.length(); i++) {
                messages.add(new ChatMessage(jsonMessages.getJSONObject(i)));
            }
            JSONObject jsonFacebookIDs = jsonConversation.getJSONObject("user_facebook_ids");
            for (int i = 0; i < jsonFacebookIDs.length(); i++) {
                String username = jsonFacebookIDs.names().get(i).toString();
                usernameFacebookIDMap.put(username, jsonFacebookIDs.getString(username));
            }
            return new Conversation(conversationID, messages, usernameFacebookIDMap);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
