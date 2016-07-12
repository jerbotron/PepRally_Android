package com.peprally.jeremy.peprally.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HTTPRequestsHelper {

//    private static final String pushServerURL = "http://peprally-push.dcif4cvzmx.us-west-2.elasticbeanstalk.com/send";
    private static final String pushServerURL = "http://ec2-107-21-196-112.compute-1.amazonaws.com/send";

    private Context callingContext;

    public HTTPRequestsHelper(Context callingContext) {
        this.callingContext = callingContext;
    }

    /***********************************************************************************************
     ************************************** HTTP REQUEST METHODS ***********************************
     **********************************************************************************************/
    public void makeHTTPPostRequest(Bundle bundle) {
        new POSTRequestAsyncTask().execute(new AsyncTaskObjectHTTPBundle(callingContext, bundle));
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private static class AsyncTaskObjectHTTPBundle {
        Context callingContext;
        Bundle bundle;
        AsyncTaskObjectHTTPBundle(Context callingContext,
                                         Bundle bundle) {
            this.callingContext = callingContext;
            this.bundle = bundle;
        }
    }

    private static class POSTRequestAsyncTask extends AsyncTask<AsyncTaskObjectHTTPBundle, Void, Void> {
        @Override
        protected Void doInBackground(AsyncTaskObjectHTTPBundle... params) {
            try {
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(params[0].callingContext);
                DynamoDBHelper dbHelper = new DynamoDBHelper(params[0].callingContext);
                Bundle bundle = params[0].bundle;
                String receiverFMSID = dbHelper.loadDBUserProfile(bundle.getString("RECEIVER_NICKNAME")).getFMSInstanceID();

                HashMap<String, String> jsonData = new HashMap<>();
                jsonData.put("receiver_id", receiverFMSID);
                jsonData.put("receiver_nickname", bundle.getString("RECEIVER_NICKNAME"));
                jsonData.put("sender_nickname", bundle.getString("SENDER_NICKNAME"));
                jsonData.put("notification_type", String.valueOf(bundle.getInt("TYPE")));
                jsonData.put("comment_text", bundle.getString("COMMENT"));

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, pushServerURL, new JSONObject(jsonData),
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
                                Log.d("HTTPRequestHelper: ", "Error: " + error.getMessage());
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

                // Add the request to the RequestQueue.
                queue.add(jsonObjectRequest);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
