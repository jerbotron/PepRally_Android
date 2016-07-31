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

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.MessageArrayAdapter;
import com.peprally.jeremy.peprally.messaging.ChatMessage;
import com.peprally.jeremy.peprally.messaging.Conversation;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

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

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        conversation = getIntent().getParcelableExtra("CONVERSATION");
        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");

        // setup home button on action bar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            String recipientNickname = conversation.getRecipientNickname(userProfileParcel.getCurUserNickname());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private void sendChatMessage() {
        messageArrayAdapter.add(new ChatMessage(conversation.getConversationID(),
                                                userProfileParcel.getCurUserNickname(),
                                                userProfileParcel.getFacebookID(),
                                                messageChatText.getText().toString().trim()));
        messageChatText.setText("");
    }
}
