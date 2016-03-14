package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.AccessToken;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    public interface AWSLoginTaskCallback {
        void onTaskDone(CognitoCachingCredentialsProvider credentialsProvider);
    }

    // AWS/FB Variables
    private AccessTokenTracker accessTokenTracker;
    private CallbackManager callbackManager;
    private boolean AWSLoginVerified = false;

    // UI Variables
    private LoginButton loginButton;

    private static final String TAG = LoginActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        Log.d(TAG, "----- STARTING Pep Rally -----");

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
//                Log.d(TAG, "access token changed");
                updateWithToken(newAccessToken);
            }
        };

        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        updateWithToken(currentToken);
    }

    private void updateWithToken(AccessToken newAccessToken) {
//        Log.d(TAG, "----- updating previous token -----");
        if (newAccessToken == null) {
            setupLoginScreen();
        } else {
//            Log.d(TAG, "----- previous token not logged in -----");
            AWSLoginTask();
        }
    }

    private void setupLoginScreen() {
        setContentView(R.layout.activity_login);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "fb login success");
//                setContentView(R.layout.activity_login);
//                AWSLoginTask();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Login attempt canceled.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Login attempt failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    private void AWSLoginTask() {
        setContentView(R.layout.splash_screen);
        AWSCredentialProvider credentialProviderTask = new AWSCredentialProvider(getApplicationContext(), new AWSLoginTaskCallback() {
            @Override
            public void onTaskDone(CognitoCachingCredentialsProvider credentialsProvider) {
                safeLoginToApp(credentialsProvider);
            }
        });
        credentialProviderTask.execute();
    }

    private void safeLoginToApp(CognitoCachingCredentialsProvider credentialsProvider) {
        AWSLoginVerified = true;
        new CreateNewUserProfileDBEntryTask().execute(credentialsProvider);
    }

    private class CreateNewUserProfileDBEntryTask extends AsyncTask<CognitoCachingCredentialsProvider, Void, Void> {
        @Override
        protected Void doInBackground(CognitoCachingCredentialsProvider... params) {
            CognitoCachingCredentialsProvider credentialsProvider = params[0];
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            Profile FBProfile = Profile.getCurrentProfile();
            HashMap<String, AttributeValue> primaryKey = new HashMap<>();
            primaryKey.put("CognitoId", new AttributeValue().withS(credentialsProvider.getIdentityId()));
            primaryKey.put("FirstName", new AttributeValue().withS(FBProfile.getFirstName()));
            primaryKey.put("LastName", new AttributeValue().withS(FBProfile.getLastName()));
            primaryKey.put("NewUser", new AttributeValue().withBOOL(true));
            Map<String, ExpectedAttributeValue> expected = new HashMap<>();
            expected.put("CognitoId", new ExpectedAttributeValue(false));
            PutItemRequest request = new PutItemRequest().withTableName("UserProfile")
                    .withItem(primaryKey)
                    .withExpected(expected);
            try {
                ddbClient.putItem(request);
                Log.d(TAG, "New cognito ID added");
            } catch (ConditionalCheckFailedException e) {
                Log.d(TAG, "ID already existed, not creating a new entry");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            finish();
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
        }
    }
}

