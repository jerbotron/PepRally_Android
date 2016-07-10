package com.peprally.jeremy.peprally.adapters;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.List;

public class NotificationCardAdapter extends RecyclerView.Adapter<NotificationCardAdapter.NotificationCardHolder>{

    Context callingContext;

    private List<DBUserNotification> notifications;
    private UserProfileParcel userProfileParcel;

    public NotificationCardAdapter(Context callingContext,
                                   List<DBUserNotification> notifications,
                                   UserProfileParcel userProfileParcel) {
        this.callingContext = callingContext;
        this.notifications = notifications;
        this.userProfileParcel = userProfileParcel;
    }

    static class NotificationCardHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        LinearLayout clickableContainer;
        ImageView userNotifyingImage;
        TextView content;
        TextView timeStamp;

        private NotificationCardHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_notifications);
            clickableContainer = (LinearLayout) itemView.findViewById(R.id.id_notification_card_container_clickable);
            userNotifyingImage = (ImageView) itemView.findViewById(R.id.id_notification_card_profile_photo);
            content = (TextView) itemView.findViewById(R.id.id_notification_card_content);
            timeStamp = (TextView) itemView.findViewById(R.id.id_notification_card_time_stamp);
        }
    }

    @Override
    public NotificationCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_notifications, parent, false);
        return new NotificationCardHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationCardHolder notificationCardHolder, int position) {
        DBUserNotification userNotification = notifications.get(position);
        Helpers.setFacebookProfileImage(callingContext,
                                        notificationCardHolder.userNotifyingImage,
                                        userNotification.getFacebookIDSender(),
                                        3);

        notificationCardHolder.timeStamp.setText(Helpers.getTimeStampString(userNotification.getTimeInSeconds()));

        String content;
        switch (userNotification.getType()) {
            case 0: // direct fistbump
                content = callingContext.getResources().getString(R.string.notification_0_placeholder);
                break;
            case 1: // commented on post
                content = callingContext.getResources().getString(R.string.notification_1_placeholder);
                String comment = userNotification.getComment();
                if (comment.length() > 50)
                    comment = comment.substring(0, 50) + "...\"";
                else
                    comment = comment + "\"";
                content = content + comment;
                break;
            case 2: // fistbumped post
                content = callingContext.getResources().getString(R.string.notification_2_placeholder);
                break;
            case 3: // fistbumped comment
                content = callingContext.getResources().getString(R.string.notification_3_placeholder);
                break;
            default:
                //TODO: better error handling on invalid notification type
                content = "";
                break;
        }

        notificationCardHolder.content.setText(Html.fromHtml("<b>"+userNotification.getNicknameSender()+"</b>" + content));

        notificationCardHolder.clickableContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("NOTIFICATIONS ADAPTER", "NOTIFICATION RECEIVED");
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

}
