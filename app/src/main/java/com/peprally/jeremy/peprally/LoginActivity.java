package com.peprally.jeremy.peprally;

import android.app.Activity;
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
import com.facebook.AccessTokenTracker;

public class LoginActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private Activity self = this;

    private CallbackManager callbackManager;

    private AccessTokenTracker accessTokenTracker;

    private static final String TAG = LoginActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        Log.d(TAG, "----- STARTING Pep Rally -----");

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                updateWithToken(newAccessToken);
            }
        };

        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        if (currentToken == null) {
            Log.d(TAG, "----- no current token, show login screen -----");
            setupLoginScreen();
        }
        else {
            Log.d(TAG, "----- current token exists, go straight to app -----");
            Intent intent = new Intent(self, ProfileActivity.class);
            startActivity(intent);
        }
    }

    private void updateWithToken(AccessToken newAccessToken) {
        Log.d(TAG, "----- updating previous token -----");
        if (newAccessToken != null) {
            Log.d(TAG, "----- previous token logged in -----");
            Intent intent = new Intent(self, ProfileActivity.class);
            startActivity(intent);
        }
        else {
            Log.d(TAG, "----- previous token not logged in -----");
            setupLoginScreen();
        }
    }

    private void setupLoginScreen() {
        setContentView(R.layout.activity_login);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Intent intent = new Intent(self, ProfileActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancel() {
                Toast.makeText(self, "Login attempt canceled.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(self, "Login attempt failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}

