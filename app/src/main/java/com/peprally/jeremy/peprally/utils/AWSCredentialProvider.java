package com.peprally.jeremy.peprally.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.regions.Regions;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.peprally.jeremy.peprally.activities.HomeActivity;
import com.peprally.jeremy.peprally.activities.LoginActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSCredentialProvider extends AsyncTask<Void, Void, CognitoCachingCredentialsProvider> {

    public final static String IDENTITY_POOL_ID = "us-east-1:62a77974-d33d-4131-8a1d-122db8e07dfa";
    public final static Regions COGNITO_REGION = Regions.US_EAST_1;

    private Context callingContext;
    private LoginActivity.AWSLoginTaskCallback loginTaskCallback;

    private static final String TAG = LoginActivity.class.getSimpleName();

    public AWSCredentialProvider(Context context,
                                 LoginActivity.AWSLoginTaskCallback taskCallback) {
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
    protected void onPostExecute(final CognitoCachingCredentialsProvider credentialsProvider) {
        Log.d(TAG, "credentials verified");
        Log.d(TAG, "credentials: " + credentialsProvider.getCredentials().toString());
        Log.d(TAG, "identity pool id: " + credentialsProvider.getIdentityPoolId());
        Log.d(TAG, "identity provider: " + credentialsProvider.getIdentityProvider());
        CognitoSyncManager syncClient = new CognitoSyncManager(
                callingContext,
                Regions.US_EAST_1,
                credentialsProvider);
        Profile fbProfile = Profile.getCurrentProfile();
        Dataset dataset = syncClient.openOrCreateDataset(credentialsProvider.getIdentityId());
        dataset.put("UserFullName", fbProfile.getFirstName() + " " + fbProfile.getLastName());
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List<Record> updatedRecords) {
                loginTaskCallback.onTaskDone(credentialsProvider);
            }
        });
    }

}
