package com.peprally.jeremy.peprally.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.AccessToken;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserNickname;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient ddbClient;
    private DynamoDBMapper mapper;

    // FB Variables
    private AccessTokenTracker accessTokenTracker;
    private CallbackManager callbackManager;
    private AccessToken currentToken;

    // UI Variables
    private EditText editTextNickname;
    private InputFilter nicknameFilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (Character.isSpaceChar(source.charAt(i))) {
                    return "_";
                }
                else if (!Character.isLetterOrDigit(source.charAt(i)) && !String.valueOf(source.charAt(i)).equals("_")) {
                    Toast.makeText(LoginActivity.this, R.string.invalid_characters_message, Toast.LENGTH_SHORT).show();
                    return "";
                }
            }
            return null;
        }
    };

    // General Variables
    private static final String TAG = LoginActivity.class.getSimpleName();

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "----- STARTING PepRally -----");

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                LoginActivity.this,                         // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                updateWithToken(newAccessToken);
            }
        };

        currentToken = AccessToken.getCurrentAccessToken();
        updateWithToken(currentToken);
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

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    public interface AWSLoginTaskCallback {
        void onTaskDone(CognitoCachingCredentialsProvider credentialsProvider);
    }

    private void AWSLoginTask() {
        setContentView(R.layout.splash_screen);
        AWSCredentialProvider credentialProviderTask = new AWSCredentialProvider(getApplicationContext(), new AWSLoginTaskCallback() {
            @Override
            public void onTaskDone(CognitoCachingCredentialsProvider credentialsProvider) {
                new CheckIfNewUserDBTask().execute(credentialsProvider);
            }
        });
        credentialProviderTask.execute();
    }

    private void setupLoginScreen() {
        setContentView(R.layout.activity_login);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        assert loginButton != null;
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "fb login success");
                currentToken = AccessToken.getCurrentAccessToken();
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

    private void updateWithToken(AccessToken newAccessToken) {
        if (newAccessToken == null) {
            setupLoginScreen();
        } else {
            AWSLoginTask();
        }
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void showNewNicknameDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_create_nickname, null);
        dialogBuilder.setView(dialogView);

        editTextNickname = (EditText) dialogView.findViewById(R.id.id_edit_text_new_nickname_dialog);
        editTextNickname.setFilters(new InputFilter[] {nicknameFilter});
        editTextNickname.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editTextNickname.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty() || s.length() < 2) {
                    editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
                }
                else {
                    new CheckUniqueNicknameDBTask().execute(s.toString().trim().replace(" ", "_"));
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        dialogBuilder.setTitle("Welcome to PepRally!");
        dialogBuilder.setMessage("Enter a unique nickname:");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String nickname = editTextNickname.getText().toString().trim().replace(" ", "_");
                if (nickname.trim().isEmpty() || nickname.length() < 2) {
                    Toast.makeText(LoginActivity.this, "Nickname must be at least 2 characters long.", Toast.LENGTH_SHORT).show();
                    showNewNicknameDialog();
                }
                else {
                    new CreateNewUserProfileDBEntryTask().execute(nickname);
                    new PushNewNicknameToDBTask().execute(nickname);
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                LoginManager.getInstance().logOut();
                setupLoginScreen();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showNicknameTaken() {
        if (!editTextNickname.getText().toString().trim().isEmpty()) {
            editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_error,0);
        }
    }

    private void showNicknameAvailable() {
        if (!editTextNickname.getText().toString().trim().isEmpty()) {
            editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_check,0);
        }
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class CheckIfNewUserDBTask extends AsyncTask<CognitoCachingCredentialsProvider, Void, Boolean> {
        CognitoCachingCredentialsProvider credentialsProvider;
        DBUserProfile userProfile;
        @Override
        protected Boolean doInBackground(CognitoCachingCredentialsProvider... params) {
            credentialsProvider = params[0];
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            ddbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            userProfile = new DBUserProfile();
            userProfile.setCognitoId(credentialsProvider.getIdentityId());
            DynamoDBQueryExpression<DBUserProfile> queryExpression = new DynamoDBQueryExpression<DBUserProfile>()
                    .withIndexName("CognitoID-index")
                    .withHashKeyValues(userProfile)
                    .withConsistentRead(false);

            List<DBUserProfile> results = mapper.query(DBUserProfile.class, queryExpression);
            if (results == null || results.size() == 0) {
                return true;
            }
            else{
                if (results.size() == 1)
                    userProfile = results.get(0);
                else{
                    Log.d(TAG, "Query result should have only returned single user!");
                }
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isNewUser) {
            if (isNewUser) {
                showNewNicknameDialog();
            }
            else {
                finish();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("NICKNAME", userProfile.getNickname());
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
            }
        }
    }

    private class CheckUniqueNicknameDBTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            DBUserNickname userNickname = mapper.load(DBUserNickname.class, params[0].toLowerCase());
            if (userNickname == null) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean nicknameTaken) {
            if (nicknameTaken) {
                showNicknameTaken();
            }
            else {
                showNicknameAvailable();
            }
        }
    }

    private class CreateNewUserProfileDBEntryTask extends AsyncTask<String, Void, Void> {
        String userNickname;
        @Override
        protected Void doInBackground(String... params) {
            userNickname = params[0];
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Profile fbProfile = Profile.getCurrentProfile();
            HashMap<String, AttributeValue> primaryKey = new HashMap<>();
            primaryKey.put("Nickname", new AttributeValue().withS(userNickname));
            primaryKey.put("CognitoID", new AttributeValue().withS(credentialsProvider.getIdentityId()));
            primaryKey.put("FacebookID", new AttributeValue().withS(fbProfile.getId()));
            primaryKey.put("FirstName", new AttributeValue().withS(fbProfile.getFirstName()));
            primaryKey.put("LastName", new AttributeValue().withS(fbProfile.getLastName()));
            primaryKey.put("NewUser", new AttributeValue().withBOOL(true));
            primaryKey.put("DateJoined", new AttributeValue().withS(df.format(c.getTime())));
            PutItemRequest request = new PutItemRequest().withTableName("UserProfiles")
                    .withItem(primaryKey);
            ddbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
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
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("NICKNAME", userNickname);
            startActivity(intent);
            overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
        }
    }

    private class PushNewNicknameToDBTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HashMap<String, AttributeValue> primaryKey = new HashMap<>();
            primaryKey.put("Nickname", new AttributeValue().withS(params[0]));
            primaryKey.put("CognitoID", new AttributeValue().withS(credentialsProvider.getIdentityId()));
            primaryKey.put("FacebookID", new AttributeValue().withS(currentToken.getUserId()));
            ddbClient.putItem(new PutItemRequest().withTableName("UserNicknames").withItem(primaryKey));
            return null;
        }
    }
}

