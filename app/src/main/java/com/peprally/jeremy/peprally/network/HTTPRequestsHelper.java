package com.peprally.jeremy.peprally.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.peprally.jeremy.peprally.custom.preferences.NotificationsPref;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.Constants;
import com.peprally.jeremy.peprally.enums.NotificationEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HTTPRequestsHelper {

    private Context callingContext;

    public HTTPRequestsHelper(Context callingContext) { this.callingContext = callingContext;
    }

    /***********************************************************************************************
     ************************************** HTTP REQUEST METHODS ***********************************
     **********************************************************************************************/
    public void makePushNotificationRequest(Bundle bundle) {
        Log.d("HRH: ", "making push notification");
        new MakePushNotificationHTTPPostRequestAsyncTask().execute(bundle);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/

    private class MakePushNotificationHTTPPostRequestAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            try {
                DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(callingContext);
                Bundle bundle = params[0];
                DBUserProfile senderProfile = dynamoDBHelper.loadDBUserProfile(bundle.getString("SENDER_USERNAME"));
                DBUserProfile receiverProfile = dynamoDBHelper.loadDBUserProfile(bundle.getString("RECEIVER_USERNAME"));
                if (receiverProfile != null) {
                    NotificationEnum notificationType = NotificationEnum.fromInt(bundle.getInt("NOTIFICATION_TYPE"));
                    NotificationsPref receiverNotificationPref = receiverProfile.getNotificationsPref();
                    boolean pushNotify = true;
                    switch (notificationType) {
                        case DIRECT_FISTBUMP:
                            pushNotify = receiverNotificationPref.isNotifyDirectFistbump();
                            break;
                        case DIRECT_MESSAGE:
                            pushNotify = receiverNotificationPref.isNotifyDirectMessage();
                            break;
                        case POST_COMMENT:
                            pushNotify = receiverNotificationPref.isNotifyNewComment();
                            break;
                        case POST_FISTBUMP:
                            pushNotify = receiverNotificationPref.isNotifyPostFistbump();
                            break;
                        case COMMENT_FISTBUMP:
                            pushNotify = receiverNotificationPref.isNotifyCommentFistbump();
                            break;
                        case DIRECT_FISTBUMP_MATCH:
                            pushNotify = receiverNotificationPref.isNotifyDirectFistbump();
                            break;
                    }

                    if (pushNotify) {
                        // Instantiate the RequestQueue.
                        final RequestQueue queue = Volley.newRequestQueue(callingContext);

                        final String receiverFMSID = dynamoDBHelper.loadDBUserProfile(bundle.getString("RECEIVER_USERNAME")).getFCMInstanceId();

                        final HashMap<String, String> jsonData = new HashMap<>();
                        jsonData.put("receiver_id", receiverFMSID);
                        jsonData.put("receiver_username", bundle.getString("RECEIVER_USERNAME"));
                        jsonData.put("sender_username", bundle.getString("SENDER_USERNAME"));
                        jsonData.put("sender_facebook_id", senderProfile.getFacebookId());
                        jsonData.put("notification_type", String.valueOf(bundle.getInt("NOTIFICATION_TYPE")));
                        jsonData.put("comment_text", bundle.getString("COMMENT"));

                        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                Request.Method.POST,
                                Constants.PUSH_SERVER_URL,
                                new JSONObject(jsonData),
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            Log.d("HTTPRequestHelper: ", response.toString(4));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                }
                        ) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                        };

                        // https://groups.google.com/forum/#!topic/volley-users/8PE9dBbD6iA
                        // Avoid volley sending data twice bug
                        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        // Add the request to the RequestQueue.
                        queue.add(jsonObjectRequest);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
