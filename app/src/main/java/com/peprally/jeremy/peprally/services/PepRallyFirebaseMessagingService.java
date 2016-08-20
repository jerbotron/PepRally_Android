package com.peprally.jeremy.peprally.services;

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.LoginActivity;
import com.peprally.jeremy.peprally.custom.ui.CircleImageTransformation;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class PepRallyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData());
        sendNotification(remoteMessage.getData());
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param jsonData FCM json data received.
     */
    private void sendNotification(Map<String, String> jsonData) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String contentText = jsonData.get("sender_username") + " ";
        NotificationEnum notificationType = NotificationEnum.fromInt(Integer.parseInt(jsonData.get("notification_type")));
        int notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
        switch (notificationType) {
            case DIRECT_FISTBUMP:
                contentText += getResources().getString(R.string.notification_0_placeholder);
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationFistbump);
                break;
            case DIRECT_MESSAGE:
                contentText += getResources().getString(R.string.notification_1_placeholder);
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationMessage);
                break;
            case POST_COMMENT:
                contentText += getResources().getString(R.string.notification_2_placeholder);
                String comment = jsonData.get("comment_text");
                if (comment.length() > 50)
                    comment = comment.substring(0, 50) + "...\"";
                else
                    comment = comment + "\"";
                contentText += comment;
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationComment);
                break;
            case POST_FISTBUMP:
                contentText += getResources().getString(R.string.notification_3_placeholder);
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationFistbump);
                break;
            case COMMENT_FISTBUMP:
                contentText += getResources().getString(R.string.notification_4_placeholder);
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationFistbump);
                break;
            case DIRECT_FISTBUMP_MATCH:
                contentText += getResources().getString(R.string.notification_5_placeholder);
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationFistbump);
                break;
        }

        Drawable notificationDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.logo_push_ut, null);
        if (notificationDrawable != null) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setLargeIcon(((BitmapDrawable) notificationDrawable).getBitmap())
                    .setSmallIcon(R.drawable.logo_push)
                    .setColor(notificationColor)
                    .setContentTitle("PepRally")
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            final int notifyId = Helpers.generateRandomInteger();
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            final Notification notification = notificationBuilder.build();
            notificationManager.notify(notifyId, notification);

            final String senderFacebookId = jsonData.get("sender_facebook_id");
            if (senderFacebookId != null && !senderFacebookId.isEmpty()) {
                Handler uiHandler = new Handler(Looper.getMainLooper());
                uiHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        Picasso.with(getApplicationContext())
                                .load(Helpers.getFacebookProfileImageURL(senderFacebookId, 3))
                                .transform(new CircleImageTransformation())
                                .into(notification.contentView, android.R.id.icon, notifyId, notification);
                    }
                });
            }
        }
    }
}
