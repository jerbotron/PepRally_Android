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

public class AWSCredentialProvider extends AsyncTask<Void, Void, Void> {

    final String IDENTITY_POOL_ID = "us-east-1:62a77974-d33d-4131-8a1d-122db8e07dfa";
    final Regions COGNITO_REGION = Regions.US_EAST_1;

    private Context callingContext;
    private LoginActivity.AWSLoginTaskCallback loginTaskCallback;

    private static final String TAG = HomeActivity.class.getSimpleName();

    public AWSCredentialProvider(Context context, LoginActivity.AWSLoginTaskCallback taskCallback) {
        callingContext = context;
        loginTaskCallback = taskCallback;
    }

    protected Void doInBackground(Void... params) {
        FacebookSdk.sdkInitialize(callingContext);
        AccessToken currentToken = AccessToken.getCurrentAccessToken();

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,          // Context
                IDENTITY_POOL_ID,   // Identity Pool ID
                COGNITO_REGION      // Region
        );
        credentialsProvider.clear();
        credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,          // Context
                IDENTITY_POOL_ID,   // Identity Pool ID
                COGNITO_REGION      // Region
        );

        Map<String, String> logins = new HashMap<>();
        logins.put("graph.facebook.com", currentToken.getToken());
        credentialsProvider.setLogins(logins);
        credentialsProvider.refresh();

//        CognitoSyncManager client = new CognitoSyncManager(
//                callingContext,
//                COGNITO_REGION,
//                credentialsProvider);

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Log.d(TAG, "credentials verified");
        loginTaskCallback.onTaskDone();
//        Profile fb_profile = Profile.getCurrentProfile();
//        Dataset dataset = client.openOrCreateDataset(fb_profile.getFirstName());
//        dataset.put("myKey2", fb_profile.getFirstName() + " Wang");
//        dataset.synchronize(new DefaultSyncCallback() {
//            @Override
//            public void onSuccess(Dataset dataset, List<Record> updatedRecords) {
//            }
//        });
//        Toast.makeText(callingContext, "data upload success", Toast.LENGTH_SHORT).show();
    }
}
