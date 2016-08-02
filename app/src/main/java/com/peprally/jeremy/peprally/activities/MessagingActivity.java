package com.peprally.jeremy.peprally.activities;

import android.database.DataSetObserver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.peprally.jeremy.peprally.network.SocketIO;
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

    // General Variables
    private Conversation conversation;
    private UserProfileParcel userProfileParcel;
    private String recipientNickname;

    private SocketIO mSocket;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // initialize parcels
        conversation = getIntent().getParcelableExtra("CONVERSATION");
        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        recipientNickname = conversation.getRecipientNickname(userProfileParcel.getCurUserNickname());

        // initialize socket
        mSocket = new SocketIO(userProfileParcel.getCurUserNickname());
        mSocket.registerListener(userProfileParcel.getCurUserNickname(), onNewMessageHandler);

        // setup home button on action bar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            if (recipientNickname != null)
                supportActionBar.setTitle(recipientNickname);
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
        mSocket.emitString("join_chat", userProfileParcel.getCurUserNickname());
        mSocket.connect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSocket != null)
            mSocket.disconnect();
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private void sendChatMessage() {
        // send update notification to messaging server
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("receiver_nickname", recipientNickname);
            jsonData.put("sender_nickname", userProfileParcel.getCurUserNickname());
            jsonData.put("data", messageChatText.getText().toString().trim());
            mSocket.emitString("send_message", jsonData.toString());
        } catch (JSONException e) { e.printStackTrace(); }

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

    private Emitter.Listener onNewMessageHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String sender = (String) args[0];
                    if (sender.equals(recipientNickname)) {
                        Log.d("MessagingActivity: ", "Refreshing chat window");
                        refreshChatWindow();
                    }
                    else {
                        Log.d("MessagingActivity: ", sender);
                    }
                }
            });
        }
    };
}
