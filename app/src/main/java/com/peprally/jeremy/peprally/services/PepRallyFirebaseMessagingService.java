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
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.LoginActivity;
import com.peprally.jeremy.peprally.custom.ui.CircleImageTransformation;
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
//        Log.d(TAG, "From: " + remoteMessage.getFrom());
//        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData());
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

        StringBuilder contentText = new StringBuilder(jsonData.get("sender_username") + " ");
        int notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
        switch (NotificationEnum.fromInt(Integer.parseInt(jsonData.get("notification_type")))) {
            case DIRECT_FISTBUMP:
                contentText.append(getResources().getString(R.string.notification_0_placeholder));
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationFistbump);
                break;
            case DIRECT_MESSAGE:
                contentText.append(getResources().getString(R.string.notification_1_placeholder));
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationMessage);
                break;
            case POST_COMMENT:
                contentText.append(getResources().getString(R.string.notification_2_placeholder));
                String comment = jsonData.get("comment_text");
                if (comment.length() > 50)
                    comment = comment.substring(0, 50) + "...\"";
                else
                    comment = comment + "\"";
                contentText.append(comment);
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationComment);
                break;
            case POST_FISTBUMP:
                contentText.append(getResources().getString(R.string.notification_3_placeholder));
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationFistbump);
                break;
            case COMMENT_FISTBUMP:
                contentText.append(getResources().getString(R.string.notification_4_placeholder));
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationFistbump);
                break;
            case DIRECT_FISTBUMP_MATCH:
                contentText.append(getResources().getString(R.string.notification_5_placeholder));
                notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.colorNotificationFistbump);
                break;
        }

        sendNotificationBasedOnAPIVersion(jsonData.get("sender_facebook_id"),
                                          contentText.toString(),
                                          notificationColor,
                                          pendingIntent);
    }

    private void sendNotificationBasedOnAPIVersion(final String senderFacebookId,
                                                   String contentText,
                                                   int notificationColor,
                                                   PendingIntent pendingIntent) {
        // initialize some common components
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.logo_push)
                .setContentTitle("PepRally")
                .setContentText(contentText)
                .setColor(notificationColor)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        final int notifyId = Helpers.generateRandomInteger();

        // check sdk version
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            notificationBuilder.setLargeIcon(Helpers.getFacebookProfilePictureBitmap(senderFacebookId, getApplicationContext()));
            final Notification notification = notificationBuilder.build();
            notificationManager.notify(notifyId, notification);
        } else {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.logo_push_ut));
            final Notification notification = notificationBuilder.build();
            if (senderFacebookId != null && !senderFacebookId.isEmpty()) {
                Handler uiHandler = new Handler(Looper.getMainLooper());
                uiHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        Picasso.with(getApplicationContext())
                                .load(Helpers.getFacebookProfilePictureURL(
                                        senderFacebookId,
                                        Helpers.FacebookProfilePictureEnum.LARGE))
                                .transform(new CircleImageTransformation())
                                .into(notification.contentView, android.R.id.icon, notifyId, notification);
                    }
                });
            }
            notificationManager.notify(notifyId, notification);
        }
    }
}
