package com.peprally.jeremy.peprally.adapters;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserConversation;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.custom.messaging.ChatMessage;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.Helpers;

import java.util.ArrayList;
import java.util.List;

public class MessageArrayAdapter extends ArrayAdapter<ChatMessage> {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private DynamoDBHelper dynamoDBHelper;

    // General Variables
    private Context callingContext;
    private List<ChatMessage> messageHistoryList;
    private String currentUsername;

    /***********************************************************************************************
     ********************************** ADAPTER CONSTRUCTOR/METHODS ********************************
     **********************************************************************************************/
    public MessageArrayAdapter(Context callingContext,
                               ArrayList<ChatMessage> messageHistoryList,
                               String currentUsername) {
        super(callingContext, R.layout.message_right);  // default text view resource id
        this.callingContext = callingContext;
        this.currentUsername = currentUsername;
        if (messageHistoryList == null)
            this.messageHistoryList = new ArrayList<>();
        else
            this.messageHistoryList = messageHistoryList;

        dynamoDBHelper = new DynamoDBHelper(callingContext);
    }

    @Override
    public void add(ChatMessage message) {
        messageHistoryList.add(message);
        super.add(message);
    }

    @Override
    public int getCount() {
        return messageHistoryList.size();
    }

    public ChatMessage getItem(int index) {
        return messageHistoryList.get(index);
    }

    @NonNull
    public View getView(int position, View messageView, @NonNull ViewGroup parent) {
        final ChatMessage chatMessage = getItem(position);
        if (chatMessage != null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (chatMessage.getUsername().equals(currentUsername)) {
                messageView = inflater.inflate(R.layout.message_right, parent, false);
            }
            else {
                messageView = inflater.inflate(R.layout.message_left, parent, false);
                ImageView leftImageView = (ImageView) messageView.findViewById(R.id.id_image_view_message_left);
                if (leftImageView != null) {
                    Helpers.setFacebookProfileImage(callingContext, leftImageView, chatMessage.getFacebookID(), Helpers.FacebookProfilePictureEnum.LARGE, true);
                    leftImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AsyncHelpers.launchExistingUserProfileActivity(callingContext, chatMessage.getUsername(), currentUsername, null);
                        }
                    });
                }
            }
            TextView textViewMessageText = (TextView) messageView.findViewById(R.id.id_text_view_message_content);
            textViewMessageText.setText(chatMessage.getMessageContent());
        }
        return messageView;
    }

    /***********************************************************************************************
     *********************************** GENERAL_METHODS ********************************
     **********************************************************************************************/
    public void notifyReceiverNewMessage(String receiverUsername) {
        new NotifyUserNewMessageAsyncTask().execute(receiverUsername);
    }

    public void fetchNewMessages(String conversationID) {
        new FetchNewMessagesFromDBAsyncTask().execute(conversationID);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class NotifyUserNewMessageAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];
            DBUserProfile userProfile = dynamoDBHelper.loadDBUserProfile(username);
            if (userProfile != null) {
                userProfile.setHasNewMessage(true);
                dynamoDBHelper.saveDBObject(userProfile);
            }
            return null;
        }
    }

    private class FetchNewMessagesFromDBAsyncTask extends AsyncTask<String, Void, ArrayList<ChatMessage>> {
        @Override
        protected ArrayList<ChatMessage> doInBackground(String... strings) {
            String conversationID = strings[0];
            DBUserConversation userConversation = dynamoDBHelper.loadDBUserConversation(conversationID);
            if (userConversation != null)
                return userConversation.getConversation().getChatMessages();
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<ChatMessage> chatMessages) {
            super.onPostExecute(chatMessages);
            if (chatMessages != null) {
                messageHistoryList = chatMessages;
                notifyDataSetChanged();
            }
        }
    }
}
