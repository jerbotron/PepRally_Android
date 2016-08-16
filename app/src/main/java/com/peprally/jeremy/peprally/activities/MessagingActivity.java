package com.peprally.jeremy.peprally.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.MessageArrayAdapter;
import com.peprally.jeremy.peprally.custom.messaging.ChatMessage;
import com.peprally.jeremy.peprally.custom.messaging.Conversation;
import com.peprally.jeremy.peprally.db_models.DBUserConversation;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.network.SocketIO;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import org.json.JSONException;
import org.json.JSONObject;

public class MessagingActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private EditText messageChatText;
    private ListView messageListView;
    private MessageArrayAdapter messageArrayAdapter;
    private ProgressDialog progressDialogDeleteConversation;

    // Network Variables
    private DynamoDBHelper dynamoDBHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // General Variables
    private Conversation conversation;
    private UserProfileParcel userProfileParcel;
    private String receiverUsername;
    private boolean receiverJoined = false;

    private SocketIO socket;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // initialize network helpers
        dynamoDBHelper = new DynamoDBHelper(this);
        httpRequestsHelper = new HTTPRequestsHelper(this);

        // initialize parcels
        conversation = getIntent().getParcelableExtra("CONVERSATION");
        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        receiverUsername = conversation.getRecipientUsername(userProfileParcel.getCurUsername());

        // initialize socket
        socket = new SocketIO(userProfileParcel.getCurUsername(), receiverUsername);

        // setup home button on action bar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            if (receiverUsername != null)
                supportActionBar.setTitle(receiverUsername);
        }

        // initialize UI members
        messageListView = (ListView) findViewById(R.id.id_list_view_container_messaging);
        messageChatText = (EditText) findViewById(R.id.id_edit_text_messaging_container);
        ImageButton messageSendButton = (ImageButton) findViewById(R.id.id_image_button_messaging_send);

        messageArrayAdapter = new MessageArrayAdapter(getApplicationContext(),
                                                      conversation.getChatMessages(),
                                                      userProfileParcel);
        messageListView.setAdapter(messageArrayAdapter);
        messageListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChatMessage();
            }
        });

        messageArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                messageListView.setSelection(messageArrayAdapter.getCount() - 1);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChatWindow();
        socket.connect();
        setupSocketListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.id_item_delete:
                AlertDialog.Builder dialogBuilderConfirmDelete = new AlertDialog.Builder(this);
                View dialogViewConfirmDelete = View.inflate(this, R.layout.dialog_confirm_delete_conversation, null);
                TextView confirmDeleteMessage = (TextView) dialogViewConfirmDelete.findViewById(R.id.id_dialog_confirm_delete_conversation);
                ImageView confirmDeleteImage = (ImageView) dialogViewConfirmDelete.findViewById(R.id.id_dialog_confirm_delete_conversation_image);
                confirmDeleteMessage.setText("Are you sure you want to delete this conversation with " + receiverUsername + "?");
                Helpers.setFacebookProfileImage(getApplicationContext(),
                        confirmDeleteImage,
                        conversation.getUsernameFacebookIDMap().get(receiverUsername),
                        3,
                        true);
                dialogBuilderConfirmDelete.setView(dialogViewConfirmDelete);
                dialogBuilderConfirmDelete.setTitle("Confirm Delete");
                dialogBuilderConfirmDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        toggleDeletingConversationLoadingDialog(true);
                        dynamoDBHelper.deleteConversation(conversation, new DynamoDBHelper.AsyncTaskCallback() {
                            @Override
                            public void onTaskDone() {
                                toggleDeletingConversationLoadingDialog(false);
                                finish();
                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                            }
                        });
                    }
                });
                dialogBuilderConfirmDelete.setNegativeButton("No", null);
                dialogBuilderConfirmDelete.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (socket != null)
            socket.disconnect();
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private void sendChatMessage() {
        Log.d("MessagingActivity: ", "receiverJoined = " + receiverJoined);

        ChatMessage newMessage = new ChatMessage(conversation.getConversationID(),
                                                 userProfileParcel.getCurUsername(),
                                                 userProfileParcel.getFacebookID(),
                                                 messageChatText.getText().toString().trim());

        // save new message to DB
        new PushChatMessageToDBAsyncTask().execute(newMessage);

        // update UI
        messageArrayAdapter.add(newMessage);

        messageChatText.setText("");
    }

    private void refreshChatWindow() {
        messageArrayAdapter.fetchNewMessages(conversation.getConversationID());
    }

    private void setupSocketListeners() {
        // on message received listener
        socket.registerListener("new_message_" + userProfileParcel.getCurUsername(), onNewMessageHandler);
        // on receiver join chat listener
        socket.registerListener("on_join_" + receiverUsername, onReceiverJoinChatHandler);
        // on receiver leave chat listener
        socket.registerListener("on_leave_" + receiverUsername, onReceiverLeaveChatHandler);
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void toggleDeletingConversationLoadingDialog(boolean show) {
        if (show)
            progressDialogDeleteConversation = ProgressDialog.show(this, "Delete Conversation", "Deleting ... ", true);
        else
            progressDialogDeleteConversation.dismiss();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class PushChatMessageToDBAsyncTask extends AsyncTask<ChatMessage, Void, Void> {
        @Override
        protected Void doInBackground(ChatMessage... chatMessages) {
            ChatMessage chatMessage = chatMessages[0];
            DBUserConversation userConversation = dynamoDBHelper.loadDBUserConversation(chatMessage.getConversationID());
            userConversation.setTimeStampLatest(Helpers.getTimestampSeconds());
            userConversation.addConversationChatMessage(chatMessage);
            dynamoDBHelper.saveDBObject(userConversation);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // send update notification to messaging server
            if (receiverJoined) {
                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("receiver_username", receiverUsername);
                    jsonData.put("sender_username", userProfileParcel.getCurUsername());
                    jsonData.put("data", messageChatText.getText().toString().trim());
                } catch (JSONException e) { e.printStackTrace(); }
                socket.emitString("send_message", jsonData.toString());
            }
            else {
                Bundle bundle = new Bundle();
                bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.DIRECT_MESSAGE.toInt());
                bundle.putString("RECEIVER_USERNAME", receiverUsername);
                bundle.putString("SENDER_USERNAME", userProfileParcel.getCurUsername());
                httpRequestsHelper.makePushNotificationRequest(bundle);

                // notify receiving user of new message alert next time they open the app
                messageArrayAdapter.notifyReceiverNewMessage(receiverUsername);
            }
        }
    }

    /***********************************************************************************************
     ************************************ Socket Emitter Listeners *********************************
     **********************************************************************************************/
    private Emitter.Listener onNewMessageHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args.length > 0) {
                        String senderUsername = (String) args[0];
                        if (senderUsername.equals(receiverUsername)) {
//                            Log.d("MessagingActivity: ", "Refreshing chat window");
                            refreshChatWindow();
                        }
                    }
                }
            });
        }
    };

    private Emitter.Listener onReceiverJoinChatHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (args.length > 0) {
                String response = (String) args[0];
                if (response.equals("callback_request")) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("receiver_username", receiverUsername);
                        jsonData.put("sender_username", userProfileParcel.getCurUsername());
                    } catch (JSONException e) { e.printStackTrace(); }
                    socket.emitString("callback_ack", jsonData.toString());
                }
//                Log.d("MessagingActivity: ", receiverUsername + " joined, " + response);
            }
//            else {
//                Log.d("MessagingActivity: ", receiverUsername + " is already in the room");
//            }
            receiverJoined = true;
        }
    };

    private Emitter.Listener onReceiverLeaveChatHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            receiverJoined = false;
//            Log.d("MessagingActivity: ", receiverUsername + " left");
        }
    };
}
