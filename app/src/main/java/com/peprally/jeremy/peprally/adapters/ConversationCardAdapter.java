package com.peprally.jeremy.peprally.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.MessagingActivity;
import com.peprally.jeremy.peprally.messaging.ChatMessage;
import com.peprally.jeremy.peprally.messaging.Conversation;
import com.peprally.jeremy.peprally.db_models.DBUserConversation;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConversationCardAdapter extends RecyclerView.Adapter<ConversationCardAdapter.MessageCardHolder>{

    private Context callingContext;

    private List<DBUserConversation> conversations;

    private UserProfileParcel userProfileParcel;

    public ConversationCardAdapter(Context callingContext,
                                   List<DBUserConversation> conversations,
                                   UserProfileParcel userProfileParcel) {
        this.callingContext = callingContext;
        this.conversations = conversations;
        this.userProfileParcel = userProfileParcel;
    }

    static class MessageCardHolder extends RecyclerView.ViewHolder {
        LinearLayout clickableContainer;
        ImageView userConversationImage;
        TextView userNickname;
        TextView lastMessageContent;
        TextView timeStamp;

        private MessageCardHolder(View itemView) {
            super(itemView);
            clickableContainer = (LinearLayout) itemView.findViewById(R.id.id_recycler_view_container_conversation);
            userConversationImage = (ImageView) itemView.findViewById(R.id.id_conversation_card_profile_photo);
            userNickname = (TextView) itemView.findViewById(R.id.id_conversation_card_nickname);
            lastMessageContent = (TextView) itemView.findViewById(R.id.id_conversation_card_content);
            timeStamp = (TextView) itemView.findViewById(R.id.id_conversation_card_time_stamp);
        }
    }

    @Override
    public MessageCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_conversations, parent, false);
        return new MessageCardHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageCardHolder MessageCardHolder, int position) {
        final DBUserConversation userConversation = conversations.get(position);
        final Conversation conversation = userConversation.getConversation();
        Map<String, String> nicknameFacebookIDMap = conversation.getNicknameFacebookIDMap();
        for (String nickname : nicknameFacebookIDMap.keySet()) {
            if (!nickname.equals(userProfileParcel.getCurUserNickname())) {
                Helpers.setFacebookProfileImage(callingContext,
                        MessageCardHolder.userConversationImage,
                        nicknameFacebookIDMap.get(nickname),
                        3);
                MessageCardHolder.userNickname.setText(nickname);
            }
        }

        ArrayList<ChatMessage> messages = conversation.getChatMessages();
        if (messages != null && messages.size() > 0) {
            String preview = messages.get(messages.size() - 1).getMessageContent();
            if (preview.length() > 20) {
                preview = preview.substring(0, 20) + "...";
            }
            MessageCardHolder.lastMessageContent.setText(preview);
        }
        else {
            MessageCardHolder.lastMessageContent.setVisibility(View.INVISIBLE);
            MessageCardHolder.lastMessageContent.setHeight(0);
        }

        MessageCardHolder.timeStamp.setText(Helpers.getTimetampString(userConversation.getTimeStampLatest()));

        // conversation onclick handler
        MessageCardHolder.clickableContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(callingContext, MessagingActivity.class);
                intent.putExtra("CONVERSATION", conversation);
                intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                callingContext.startActivity(intent);
                ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

}
