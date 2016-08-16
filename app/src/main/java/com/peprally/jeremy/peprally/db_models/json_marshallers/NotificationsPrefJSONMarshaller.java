package com.peprally.jeremy.peprally.db_models.json_marshallers;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.JsonMarshaller;
import com.peprally.jeremy.peprally.custom.preferences.NotificationsPref;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationsPrefJSONMarshaller extends JsonMarshaller<NotificationsPref> implements DynamoDBMarshaller<NotificationsPref> {

    @Override
    public String marshall(NotificationsPref notificationsPref) {
        JSONObject jsonoNotificationPref = new JSONObject();
        try {
            jsonoNotificationPref.put("notify_direct_fistbump", notificationsPref.isNotifyDirectFistbump());
            jsonoNotificationPref.put("notify_post_fistbump", notificationsPref.isNotifyPostFistbump());
            jsonoNotificationPref.put("notify_comment_fistbump", notificationsPref.isNotifyCommentFistbump());
            jsonoNotificationPref.put("notify_post_comment", notificationsPref.isNotifyNewComment());
            jsonoNotificationPref.put("notify_direct_message", notificationsPref.isNotifyDirectMessage());
        } catch (JSONException e) { e.printStackTrace(); }
        return jsonoNotificationPref.toString();
    }

    @Override
    public NotificationsPref unmarshall(Class<NotificationsPref> clazz, String json) {
        try {
            JSONObject jsonNotificationPref = new JSONObject(json);
            return new NotificationsPref(jsonNotificationPref.getBoolean("notify_direct_fistbump"),
                                         jsonNotificationPref.getBoolean("notify_post_fistbump"),
                                         jsonNotificationPref.getBoolean("notify_comment_fistbump"),
                                         jsonNotificationPref.getBoolean("notify_post_comment"),
                                         jsonNotificationPref.getBoolean("notify_direct_message")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
