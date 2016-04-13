package com.peprally.jeremy.peprally;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

import java.util.HashMap;
import java.util.Map;

public class AWSCredentialProvider extends AsyncTask<Void, Void, CognitoCachingCredentialsProvider> {

    final static String IDENTITY_POOL_ID = "us-east-1:62a77974-d33d-4131-8a1d-122db8e07dfa";
    final static Regions COGNITO_REGION = Regions.US_EAST_1;

    private Context callingContext;
    private LoginActivity.AWSLoginTaskCallback loginTaskCallback;

    private static final String TAG = HomeActivity.class.getSimpleName();

    public AWSCredentialProvider(Context context, LoginActivity.AWSLoginTaskCallback taskCallback) {
        callingContext = context;
        loginTaskCallback = taskCallback;
    }

    protected CognitoCachingCredentialsProvider doInBackground(Void... params) {
        FacebookSdk.sdkInitialize(callingContext);
        AccessToken currentToken = AccessToken.getCurrentAccessToken();

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,     // Context
                IDENTITY_POOL_ID,   // Identity Pool ID
                COGNITO_REGION      // Region
        );
        credentialsProvider.clear();
        credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,     // Context
                IDENTITY_POOL_ID,   // Identity Pool ID
                COGNITO_REGION      // Region
        );

        Map<String, String> logins = new HashMap<>();
        logins.put("graph.facebook.com", currentToken.getToken());
        credentialsProvider.setLogins(logins);
        credentialsProvider.refresh();
        return credentialsProvider;
    }

    @Override
    protected void onPostExecute(CognitoCachingCredentialsProvider credentialsProvider) {
        Log.d(TAG, "credentials verified");
        loginTaskCallback.onTaskDone(credentialsProvider);
    }

}
