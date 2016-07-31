package com.peprally.jeremy.peprally.db_models;

import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.peprally.jeremy.peprally.messaging.ChatMessage;
import com.peprally.jeremy.peprally.messaging.Conversation;
import com.peprally.jeremy.peprally.messaging.ConversationJSONMarshaller;

@DynamoDBTable(tableName = "UserConversations")
public class DBUserConversation implements Comparable<DBUserConversation> {
    private String conversationID;
    private Long timeStampLatest;
    private Long timeStampCreated;
    private Conversation conversation;

    @Override
    public int compareTo(@NonNull DBUserConversation another) {
        return this.timeStampLatest.compareTo(another.timeStampLatest);
    }

    @DynamoDBHashKey(attributeName = "ConversationID")
    public String getConversationID() { return conversationID; }
    public void setConversationID(String conversationID) { this.conversationID = conversationID; }

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

    public void addConversationChatMessage(ChatMessage message) {
        conversation.addChatMessage(message);
    }
}
