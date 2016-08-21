package com.peprally.jeremy.peprally.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.regions.Regions;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.peprally.jeremy.peprally.activities.LoginActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peprally.jeremy.peprally.utils.Constants.COGNITO_REGION;
import static com.peprally.jeremy.peprally.utils.Constants.IDENTITY_POOL_ID;

public class AWSCredentialProvider extends AsyncTask<Void, Void, CognitoCachingCredentialsProvider> {

    private Context callingContext;
    private LoginActivity.AWSLoginTaskCallback loginTaskCallback;

    private static final String TAG = "AWSCredentialsProvider";

    public AWSCredentialProvider(Context context,
                                 LoginActivity.AWSLoginTaskCallback taskCallback) {
        callingContext = context;
        loginTaskCallback = taskCallback;
    }

    protected CognitoCachingCredentialsProvider doInBackground(Void... params) {

        Log.d(TAG, "verifying credentials");

        try {
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
        } catch (AmazonClientException err) {
            err.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(final CognitoCachingCredentialsProvider credentialsProvider) {
        if (credentialsProvider == null) {
            new AWSCredentialProvider(callingContext, loginTaskCallback).execute();
        } else {
            Log.d(TAG, "credentials verified");
//            Log.d(TAG, "credentials: " + credentialsProvider.getCredentials().toString());
//            Log.d(TAG, "identity pool id: " + credentialsProvider.getIdentityPoolId());
//            Log.d(TAG, "identity provider: " + credentialsProvider.getIdentityProvider());
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

}
