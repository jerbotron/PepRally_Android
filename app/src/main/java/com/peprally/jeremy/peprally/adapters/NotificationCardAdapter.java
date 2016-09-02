package com.peprally.jeremy.peprally.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.PostCommentActivity;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.List;

public class NotificationCardAdapter extends RecyclerView.Adapter<NotificationCardAdapter.NotificationCardHolder>{

    private DynamoDBHelper dynamoDBHelper;

    private Context callingContext;
    private List<DBUserNotification> notifications;
    private UserProfileParcel userProfileParcel;

    public NotificationCardAdapter(Context callingContext,
                                   List<DBUserNotification> notifications,
                                   UserProfileParcel userProfileParcel) {
        this.callingContext = callingContext;
        this.notifications = notifications;
        this.userProfileParcel = userProfileParcel;

        dynamoDBHelper = new DynamoDBHelper(callingContext);
    }

    static class NotificationCardHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        LinearLayout clickableContainer;
        ImageView profileImage;
        TextView content;
        TextView timeStamp;

        private NotificationCardHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_notifications);
            clickableContainer = (LinearLayout) itemView.findViewById(R.id.id_notification_card_container_clickable);
            profileImage = (ImageView) itemView.findViewById(R.id.id_notification_card_profile_photo);
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
    public void onBindViewHolder(final NotificationCardHolder notificationCardHolder, int position) {
        final DBUserNotification userNotification = notifications.get(position);
        Helpers.setFacebookProfileImage(callingContext,
                                        notificationCardHolder.profileImage,
                                        userNotification.getFacebookIdSender(),
                                        3,
                                        true);

        notificationCardHolder.timeStamp.setText(Helpers.getTimetampString(userNotification.getTimestampSeconds(), true));

        String content = "";
        NotificationEnum notificationType = NotificationEnum.fromInt(userNotification.getNotificationType());
        switch (notificationType) {
            case DIRECT_FISTBUMP:
                content = callingContext.getResources().getString(R.string.notification_0_placeholder);
                break;
            case POST_COMMENT:
                content = callingContext.getResources().getString(R.string.notification_2_placeholder);
                String comment = userNotification.getComment();
                if (comment.length() > 50)
                    comment = comment.substring(0, 50) + "...\"";
                else
                    comment = comment + "\"";
                content = content + comment;
                break;
            case POST_FISTBUMP:
                content = callingContext.getResources().getString(R.string.notification_3_placeholder);
                break;
            case COMMENT_FISTBUMP:
                content = callingContext.getResources().getString(R.string.notification_4_placeholder);
                break;
        }

        notificationCardHolder.content.setText(Helpers.getAPICompatHtml("<b>"+userNotification.getSenderUsername()+"</b> " + content));

        // clickable handlers
        notificationCardHolder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncHelpers.launchExistingUserProfileActivity(callingContext, userNotification.getSenderUsername(), userNotification.getUsername(), null);
            }
        });

        notificationCardHolder.clickableContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (NotificationEnum.fromInt(userNotification.getNotificationType())) {
                    case DIRECT_FISTBUMP:
                        AsyncHelpers.launchExistingUserProfileActivity(callingContext, userNotification.getSenderUsername(), userNotification.getUsername(), null);
                        break;
                    case POST_COMMENT:
                    case POST_FISTBUMP:
                    case COMMENT_FISTBUMP:
                        notificationCardHolder.clickableContainer.setClickable(false);
                        new FetchUserPostAsyncTask().execute(userNotification);
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /***********************************************************************************************
     *********************************** GENERAL_METHODS ********************************
     **********************************************************************************************/
    private void launchNewCommentActivity(DBUserPost curPost) {
        Intent intent = new Intent(callingContext, PostCommentActivity.class);
        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
        intent.putExtra("MAIN_POST", curPost);
        callingContext.startActivity(intent);
        ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchUserPostAsyncTask extends AsyncTask<DBUserNotification, Void, DBUserPost> {
        @Override
        protected DBUserPost doInBackground(DBUserNotification... dbUserNotifications) {
            DBUserNotification userNotification = dbUserNotifications[0];
            return dynamoDBHelper.loadDBUserPost(userNotification.getPostId());
        }

        @Override
        protected void onPostExecute(DBUserPost userPost) {
            if (userPost != null) {
                launchNewCommentActivity(userPost);
            }
        }
    }
}
