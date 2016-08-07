package com.peprally.jeremy.peprally.adapters;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
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
import com.peprally.jeremy.peprally.activities.NewCommentActivity;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.ActivityEnum;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.NotificationEnum;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

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
        ImageView userNotifyingImage;
        TextView content;
        TextView timeStamp;

        private NotificationCardHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_notifications);
            clickableContainer = (LinearLayout) itemView.findViewById(R.id.id_recycler_view_container_notification);
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
    public void onBindViewHolder(final NotificationCardHolder notificationCardHolder, int position) {
        final DBUserNotification userNotification = notifications.get(position);
        Helpers.setFacebookProfileImage(callingContext,
                                        notificationCardHolder.userNotifyingImage,
                                        userNotification.getFacebookIDSender(),
                                        3);

        notificationCardHolder.timeStamp.setText(Helpers.getTimetampString(userNotification.getTimeInSeconds()));

        String content = "";
        NotificationEnum notificationType = NotificationEnum.fromInt(userNotification.getNotificationType());
        if (notificationType != null) {
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
        }

        notificationCardHolder.content.setText(Html.fromHtml("<b>"+userNotification.getNicknameSender()+"</b> " + content));

        notificationCardHolder.clickableContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationCardHolder.clickableContainer.setClickable(false);
                new FetchUserPostAsyncTask().execute(userNotification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private void launchNewCommentActivity(DBUserPost curPost) {
        Intent intent = new Intent(callingContext, NewCommentActivity.class);
        userProfileParcel.setCurrentActivity(ActivityEnum.NEWCOMMENT);
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
            String postId = userNotification.getPostID();
            String postNickname = postId.split("_")[0];
            Long postTimeStamp = Long.valueOf(postId.split("_")[1]);
            Log.d("NotCardAdapter: ", "postNickname = " + postNickname);
            Log.d("NotCardAdapter: ", "postTimeStamp = " + postTimeStamp);
//            NotificationEnum notificationType = NotificationEnum.fromInt(userNotification.getNotificationType());
//            if (notificationType != null) {
//                switch (notificationType) {
//                    case POST_COMMENT:
//
//                        break;
//                    case POST_FISTBUMP:
//
//                        break;
//                    case COMMENT_FISTBUMP:
//
//                        break;
//                }
//            }

            return dynamoDBHelper.loadDBUserPost(postNickname, postTimeStamp);
        }

        @Override
        protected void onPostExecute(DBUserPost userPost) {
            if (userPost != null) {
                launchNewCommentActivity(userPost);
            }
        }
    }
}
