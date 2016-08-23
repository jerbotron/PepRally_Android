package com.peprally.jeremy.peprally.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

public class MessagingActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private EditText messageChatText;
    private ProgressDialog progressDialogDeleteConversation;

    // Network Variables
    private static DynamoDBHelper dynamoDBHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // General Variables
    private boolean receiverJoined = false;
    private boolean conversationEmpty;
    private static Conversation conversation;
    private FragmentManager fragmentManager;
    private MessagesFragment messagesFragment;
    private static String currentUsername;
    private static String currentUserFacebookId;
    private static String receiverUsername;

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
        currentUsername = getIntent().getStringExtra("CURRENT_USERNAME");
        currentUserFacebookId = getIntent().getStringExtra("CURRENT_USER_FACEBOOK_ID");
        receiverUsername = conversation.getRecipientUsername(currentUsername);

        // initialize socket
        socket = new SocketIO(currentUsername, receiverUsername);

        // setup fragments
        fragmentManager = getSupportFragmentManager();
        if (conversation.getChatMessages() == null || conversation.getChatMessages().isEmpty()) {
            conversationEmpty = true;
            NoMessageFragment noMessageFragment = new NoMessageFragment();
            fragmentManager.beginTransaction().add(R.id.id_container_messaging_fragment, noMessageFragment).commit();
        } else {
            conversationEmpty = false;
            messagesFragment = new MessagesFragment();
            fragmentManager.beginTransaction().add(R.id.id_container_messaging_fragment, messagesFragment).commit();
        }

        // initialize UI variables
        messageChatText = (EditText) findViewById(R.id.id_edit_text_messaging_container);
        ImageButton messageSendButton = (ImageButton) findViewById(R.id.id_image_button_messaging_send);
        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageChatText.getText().toString().trim();
                if (!msg.isEmpty())
                    sendChatMessage(msg);
            }
        });

        // setup home button on action bar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            if (receiverUsername != null)
                supportActionBar.setTitle(receiverUsername);
        }
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
                Helpers.setFacebookProfileImage(MessagingActivity.this,
                        confirmDeleteImage,
                        conversation.getUsernameFacebookIdMap().get(receiverUsername),
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
     *********************************** GENERAL_METHODS ********************************
     **********************************************************************************************/
    private void sendChatMessage(String message) {
        ChatMessage newMessage = new ChatMessage(conversation.getConversationID(),
                currentUsername,
                currentUserFacebookId,
                message);

        // save new message to DB
        new PushChatMessageToDBAsyncTask().execute(newMessage);

        // switch fragments on first message
        if (conversationEmpty) {
            conversationEmpty = false;
            messagesFragment = new MessagesFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("HAS_FIRST_MESSAGE", true);
            bundle.putParcelable("FIRST_MESSAGE", newMessage);
            messagesFragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.id_container_messaging_fragment, messagesFragment).commit();
        } else {
            // update UI
            messagesFragment.addMessage(newMessage);
        }
        messageChatText.setText("");
    }

    private void setupSocketListeners() {
        // on message received listener
        socket.registerListener("new_message_" + currentUsername, onNewMessageHandler);
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

    private void launchConversationHasBeenDeletedDialog() {
        AlertDialog.Builder dialogBuilderConfirmDelete = new AlertDialog.Builder(this);
        View dialogViewConfirmDelete = View.inflate(this, R.layout.dialog_confirm_delete, null);
        dialogBuilderConfirmDelete.setView(dialogViewConfirmDelete);
        dialogBuilderConfirmDelete.setTitle("Conversation Deleted");
        dialogBuilderConfirmDelete.setMessage("Sorry, this conversation has been deleted by the other user :(");
        dialogBuilderConfirmDelete.setPositiveButton("Close", null);
        dialogBuilderConfirmDelete.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialogBuilderConfirmDelete.create().show();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class PushChatMessageToDBAsyncTask extends AsyncTask<ChatMessage, Void, Boolean> {
        @Override
        protected Boolean doInBackground(ChatMessage... chatMessages) {
            ChatMessage chatMessage = chatMessages[0];
            DBUserConversation userConversation = dynamoDBHelper.loadDBUserConversation(chatMessage.getConversationID());
            if (userConversation != null) {
                userConversation.setTimeStampLatest(Helpers.getTimestampSeconds());
                userConversation.addConversationChatMessage(chatMessage);
                dynamoDBHelper.saveDBObject(userConversation);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean conversationSavedSuccess) {
            if (conversationSavedSuccess) {
                // send update notification to messaging server
                if (receiverJoined) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("receiver_username", receiverUsername);
                        jsonData.put("sender_username", currentUsername);
                        jsonData.put("data", messageChatText.getText().toString().trim());
                    } catch (JSONException e) { e.printStackTrace(); }
                    socket.emitString("send_message", jsonData.toString());
                }
                else {
                    Bundle bundle = new Bundle();
                    bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.DIRECT_MESSAGE.toInt());
                    bundle.putString("RECEIVER_USERNAME", receiverUsername);
                    bundle.putString("SENDER_USERNAME", currentUsername);
                    bundle.putString("SENDER_FACEBOOK_ID", currentUserFacebookId);
                    httpRequestsHelper.makePushNotificationRequest(bundle);

                    // notify receiving user of new message alert next time they open the app
                    messagesFragment.notifyReceiverNewMessageAlert();
                }
            } else {
                launchConversationHasBeenDeletedDialog();
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
                            if (conversationEmpty) {
                                conversationEmpty = false;
                                messagesFragment = new MessagesFragment();
                                fragmentManager.beginTransaction().replace(R.id.id_container_messaging_fragment, messagesFragment).commit();
                            } else {
                                messagesFragment.refreshChatWindow();
                            }
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
                        jsonData.put("sender_username", currentUsername);
                    } catch (JSONException e) { e.printStackTrace(); }
                    socket.emitString("callback_ack", jsonData.toString());
                }
            }
            receiverJoined = true;
        }
    };

    private Emitter.Listener onReceiverLeaveChatHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            receiverJoined = false;
        }
    };

    /***********************************************************************************************
     **************************************** Fragment Classes *************************************
     **********************************************************************************************/
    public static class NoMessageFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_messaging_no_message, container, false);
            final TextView titleText = (TextView) view.findViewById(R.id.id_messaging_default_title);
            final TextView messageText = (TextView) view.findViewById(R.id.id_messaging_default_message);
            final ImageView profileImage = (ImageView) view.findViewById(R.id.id_messaging_default_image);

            String title = "You fistbumped with " + receiverUsername;
            String message = Helpers.getTimetampString(conversation.getTimestampCreated(), false);
            String facebookId = conversation.getUserFacebookId(receiverUsername);
            titleText.setText(title);
            messageText.setText(message);
            Helpers.setFacebookProfileImage(getContext(),
                    profileImage,
                    facebookId,
                    3,
                    true);

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    getActivity().finish();
                    AsyncHelpers.launchExistingUserProfileActivity(getContext(), receiverUsername, currentUsername);
                }
            });

            return view;
        }
    }

    public static class MessagesFragment extends Fragment {

        // UI Variables
        private ListView messageListView;
        private MessageArrayAdapter messageArrayAdapter;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            messageArrayAdapter = new MessageArrayAdapter(getActivity(),
                    conversation.getChatMessages(),
                    currentUsername);

            // see if it's the first time someone sent a message in this conversation
            Bundle bundle = getArguments();
            if (bundle != null && bundle.getBoolean("HAS_FIRST_MESSAGE")) {
                ChatMessage newMessage = bundle.getParcelable("FIRST_MESSAGE");
                if (newMessage != null)
                    addMessage(newMessage);
            }

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_messaging_messages, container, false);

            // initialize UI members
            messageListView = (ListView) view.findViewById(R.id.id_list_view_container_messaging);
            messageListView.setAdapter(messageArrayAdapter);
            messageListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

            messageArrayAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    messageListView.setSelection(messageArrayAdapter.getCount() - 1);
                }
            });

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            refreshChatWindow();
        }

        private void refreshChatWindow() {
            messageArrayAdapter.fetchNewMessages(conversation.getConversationID());
        }

        private void notifyReceiverNewMessageAlert() {
            messageArrayAdapter.notifyReceiverNewMessage(receiverUsername);
        }

        private void addMessage(ChatMessage newMessage) {
            messageArrayAdapter.add(newMessage);
        }
    }
}
