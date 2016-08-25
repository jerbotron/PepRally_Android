package com.peprally.jeremy.peprally.db_models;

import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.peprally.jeremy.peprally.custom.messaging.ChatMessage;
import com.peprally.jeremy.peprally.custom.messaging.Conversation;
import com.peprally.jeremy.peprally.db_models.json_marshallers.ConversationJSONMarshaller;

@DynamoDBTable(tableName = "UserConversations")
public class DBUserConversation implements Comparable<DBUserConversation> {
    private String conversationID;
    private String senderUsername;
    private String receiverUsername;
    private Long timeStampLatest;
    private Long timeStampCreated;
    private Conversation conversation;

    @Override
    public int compareTo(@NonNull DBUserConversation another) {
        return this.timeStampLatest.compareTo(another.timeStampLatest);
    }

    // Helpers
    public void addConversationChatMessage(ChatMessage message) {
        conversation.addChatMessage(message);
    }

    public String getOtherUsername(String currentUsername) {
        return (currentUsername.equals(senderUsername)) ? receiverUsername : senderUsername;
    }

    // Getters/Setters
    @DynamoDBHashKey(attributeName = "ConversationID")
    public String getConversationID() { return conversationID; }
    public void setConversationID(String conversationID) { this.conversationID = conversationID; }

    @DynamoDBAttribute(attributeName = "ReceiverUsername")
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    @DynamoDBAttribute(attributeName = "SenderUsername")
    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }

    @DynamoDBAttribute(attributeName = "TimestampLatest")
    public Long getTimeStampLatest() { return timeStampLatest; }
    public void setTimeStampLatest(Long timeStampLatest) { this.timeStampLatest = timeStampLatest; }

    @DynamoDBAttribute(attributeName = "TimestampCreated")
    public Long getTimeStampCreated() { return timeStampCreated; }
    public void setTimeStampCreated(Long timeStampCreated) { this.timeStampCreated = timeStampCreated; }

    @DynamoDBAttribute(attributeName = "JSONConversation")
    @DynamoDBMarshalling (marshallerClass = ConversationJSONMarshaller.class)
    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
}
