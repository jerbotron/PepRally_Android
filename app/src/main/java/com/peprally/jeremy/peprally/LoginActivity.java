package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.AccessToken;

import java.io.Serializable;

public class LoginActivity extends AppCompatActivity implements Serializable{

    public interface AWSLoginTaskCallback {
        void onTaskDone();
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
            public void onTaskDone() {
                safeLoginToApp();
            }
        });
        credentialProviderTask.execute();
    }

    private void safeLoginToApp() {
        AWSLoginVerified = true;
        finish();
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
    }
}

