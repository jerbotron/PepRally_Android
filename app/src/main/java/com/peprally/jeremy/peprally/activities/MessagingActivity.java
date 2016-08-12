package com.peprally.jeremy.peprally.activities;

import android.content.DialogInterface;
import android.database.DataSetObserver;
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
import android.widget.ListView;

import com.github.nkzawa.emitter.Emitter;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.MessageArrayAdapter;
import com.peprally.jeremy.peprally.messaging.ChatMessage;
import com.peprally.jeremy.peprally.messaging.Conversation;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.network.SocketIO;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import org.json.JSONException;
import org.json.JSONObject;

public class MessagingActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private MessageArrayAdapter messageArrayAdapter;
    private ListView messageListView;
    private EditText messageChatText;

    // Network Variables
    private DynamoDBHelper dynamoDBHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // General Variables
    private Conversation conversation;
    private UserProfileParcel userProfileParcel;
    private String receiverNickname;
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
        receiverNickname = conversation.getRecipientNickname(userProfileParcel.getCurUserNickname());

        // initialize socket
        socket = new SocketIO(userProfileParcel.getCurUserNickname(), receiverNickname);

        // setup home button on action bar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            if (receiverNickname != null)
                supportActionBar.setTitle(receiverNickname);
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
                View dialogViewConfirmDelete = View.inflate(this, R.layout.dialog_confirm_delete, null);
                dialogBuilderConfirmDelete.setView(dialogViewConfirmDelete);
                dialogBuilderConfirmDelete.setTitle("Confirm Delete");
                dialogBuilderConfirmDelete.setMessage("Are you sure you want to delete this conversation?");
                dialogBuilderConfirmDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d("MessagingActivity: ", "conversation deleted");
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
        // send update notification to messaging server
        if (receiverJoined) {
            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("receiver_nickname", receiverNickname);
                jsonData.put("sender_nickname", userProfileParcel.getCurUserNickname());
                jsonData.put("data", messageChatText.getText().toString().trim());
            } catch (JSONException e) { e.printStackTrace(); }
            socket.emitString("send_message", jsonData.toString());
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.DIRECT_MESSAGE.toInt());
            bundle.putString("RECEIVER_NICKNAME", receiverNickname);
            bundle.putString("SENDER_NICKNAME", userProfileParcel.getCurUserNickname());
            httpRequestsHelper.makePushNotificationRequest(bundle);
        }

        // notify receiving user of new message alert next time they open the app
        messageArrayAdapter.notifyReceiverNewMessage(receiverNickname);

        // update UI
        messageArrayAdapter.add(new ChatMessage(conversation.getConversationID(),
                                                userProfileParcel.getCurUserNickname(),
                                                userProfileParcel.getFacebookID(),
                                                messageChatText.getText().toString().trim()));
        messageChatText.setText("");
    }

    private void refreshChatWindow() {
        messageArrayAdapter.fetchNewMessages(conversation.getConversationID());
    }

    private void setupSocketListeners() {
        // on message received listener
        socket.registerListener("new_message_" + userProfileParcel.getCurUserNickname(), onNewMessageHandler);
        // on receiver join chat listener
        socket.registerListener("on_join_" + receiverNickname, onReceiverJoinChatHandler);
        // on receiver leave chat listener
        socket.registerListener("on_leave_" + receiverNickname, onReceiverLeaveChatHandler);
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
                        String senderNickname = (String) args[0];
                        if (senderNickname.equals(receiverNickname)) {
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
                        jsonData.put("receiver_nickname", receiverNickname);
                        jsonData.put("sender_nickname", userProfileParcel.getCurUserNickname());
                    } catch (JSONException e) { e.printStackTrace(); }
                    socket.emitString("callback_ack", jsonData.toString());
                }
//                Log.d("MessagingActivity: ", receiverNickname + " joined, " + response);
            }
//            else {
//                Log.d("MessagingActivity: ", receiverNickname + " is already in the room");
//            }
            receiverJoined = true;
        }
    };

    private Emitter.Listener onReceiverLeaveChatHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            receiverJoined = false;
//            Log.d("MessagingActivity: ", receiverNickname + " left");
        }
    };
}
