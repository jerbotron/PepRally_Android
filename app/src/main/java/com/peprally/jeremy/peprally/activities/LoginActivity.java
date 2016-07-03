package com.peprally.jeremy.peprally.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.AccessToken;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserNickname;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.ActivityEnum;
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private DynamoDBHelper dbHelper;

    // FB Variables
    private AccessTokenTracker accessTokenTracker;
    private CallbackManager callbackManager;

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
    private Bundle fbDataBundle;
    private boolean connectionSecured;
    private String FMSInstanceID;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        if (!Helpers.checkIfNetworkConnectionAvailable((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE))) {
            setContentView(R.layout.activity_login);
            connectionSecured = false;
            LoginManager.getInstance().logOut();
            final LinearLayout container = (LinearLayout) findViewById(R.id.id_activity_login_container);
            final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
            assert container != null && loginButton != null;
            final Snackbar snackbar = Snackbar.make(container, getResources().getString(R.string.no_connection_text), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OKAY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }});
            TextView tv_snackbar = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            tv_snackbar.setMaxLines(5);
            snackbar.show();
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.show();
                }
            });
        }
        else {
            connectionSecured = true;
            Log.d(TAG, "----- CHECKING Google Play Services -----");
            if (Helpers.checkGooglePlayServicesAvailable(this)) {

                Log.d(TAG, "----- STARTING Pep Rally -----");

                callbackManager = CallbackManager.Factory.create();

                dbHelper = new DynamoDBHelper(this);

                fbDataBundle = new Bundle();

                FMSInstanceID = Helpers.getFMSInstanceID(this);

                accessTokenTracker = new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                        // When a new facebook account is used to login
                        updateWithToken();
                    }
                };

                updateWithToken();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        } catch (NullPointerException e) {
            Log.d(TAG, "No connection error, handled by login button OnClick");
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Helpers.checkGooglePlayServicesAvailable(this);
    }

    @Override
    public void onBackPressed() {
        if (connectionSecured)
        {
            finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionSecured)
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
                dbHelper.refresh(getApplicationContext());
            }
        });
        credentialProviderTask.execute();
    }

    private void updateWithToken() {
        if (AccessToken.getCurrentAccessToken() == null) {
            setupLoginScreen();
        } else {
            AWSLoginTask();
        }
    }

    private String getFacebookDataSafely(GraphResponse response, String key) {
        try {
            return response.getJSONObject().getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void bundleFacebookData() {
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/{user-id}",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.d(TAG, response.toString());
                        String id = getFacebookDataSafely(response, "id");
                        String email = getFacebookDataSafely(response, "email");
                        String firstName = getFacebookDataSafely(response, "first_name");
                        String lastName = getFacebookDataSafely(response, "last_name");
                        String gender = getFacebookDataSafely(response, "gender");
                        String birthday = getFacebookDataSafely(response, "birthday");
                        Profile profile = Profile.getCurrentProfile();
                        String link = profile.getLinkUri().toString();
                        fbDataBundle.putString("ID", id);
                        fbDataBundle.putString("LINK", link);
                        fbDataBundle.putString("EMAIL", email);
                        fbDataBundle.putString("FIRSTNAME", firstName);
                        fbDataBundle.putString("LASTNAME", lastName);
                        fbDataBundle.putString("GENDER", gender);
                        fbDataBundle.putString("BIRTHDAY", birthday);
                    }
                });
        Bundle fbData = new Bundle();
        fbData.putString("fields", "id, email, first_name, last_name, gender, birthday");
        request.setParameters(fbData);
        request.executeAsync();
    }

    private void setupLoginScreen() {
        setContentView(R.layout.activity_login);
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        assert loginButton != null;
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "fb login success");
                bundleFacebookData();
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

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void showNewNicknameDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_create_nickname, null);
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

    private void showVerifyVarsityPlayerDialog(final DBUserProfile userProfile,
                                               final DBPlayerProfile playerProfile) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_verify_varsity_player, null);
        dialogBuilder.setView(dialogView);

        TextView textViewVerifyVarsityLine1 = (TextView) dialogView.findViewById(R.id.id_text_view_verify_varsity_line1);
        TextView textViewVerifyVarsityLine2 = (TextView) dialogView.findViewById(R.id.id_text_view_verify_varsity_line2);

        String line1Text = playerProfile.getFirstName() + " " + playerProfile.getLastName();
        String line2Text = playerProfile.getTeam() + " | " + playerProfile.getPosition() + " | " + playerProfile.getHometown();
        switch (playerProfile.getTeam()) {
            case "Volleyball":
            case "Soccer":
            case "Basketball":
            case "Football":
            case "Baseball":
            case "Softball":
                line1Text = line1Text + " #" + playerProfile.getNumber();
        }
        textViewVerifyVarsityLine1.setText(line1Text);
        textViewVerifyVarsityLine2.setText(line2Text);

        dialogBuilder.setTitle("Are you a varsity player?");
        dialogBuilder.setMessage("We detected that you might be a varsity player, please confirm if you are the person below. " +
                "(False confirmations will be detected and your account may be banned.)");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                playerProfile.setHasUserProfile(true);
                playerProfile.setNickname(userProfile.getNickname());
                new PushPlayerProfileChangesToDBTask().execute(playerProfile);
                UserProfileParcel userProfileParcel = new UserProfileParcel(ActivityEnum.HOME, userProfile, playerProfile);
                finish();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("PLAYER_PROFILE_PARCEL", userProfileParcel);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
            }
        });
        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                UserProfileParcel userProfileParcel = new UserProfileParcel(ActivityEnum.HOME, userProfile, playerProfile);
                finish();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("PLAYER_PROFILE_PARCEL", userProfileParcel);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
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
        DBPlayerProfile playerProfile;
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
                if (results.size() == 1) {
                    userProfile = results.get(0);
                    if (userProfile.getIsVarsityPlayer()) {
                        playerProfile = mapper.load(DBPlayerProfile.class, userProfile.getTeam(), userProfile.getPlayerIndex());
                    }
//                    userProfile.setFMSInstanceID(FMSInstanceID);
//                    dbHelper.saveDBObject(userProfile);
                }
                else{
                    Log.d(TAG, "Query result should have only returned single user!");
                }
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isNewUser) {
            if (isNewUser) {
                bundleFacebookData();
                showNewNicknameDialog();
            }
            else {
                UserProfileParcel userProfileParcel = new UserProfileParcel(ActivityEnum.HOME, userProfile, playerProfile);
                finish();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
            }
        }
    }

    private class CheckUniqueNicknameDBTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // params[0] = nickname
            DBUserNickname userNickname = dbHelper.loadDBNickname(params[0]);
            return userNickname != null;
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

    private class CreateNewUserProfileDBEntryTask extends AsyncTask<String, Void, DBUserProfile> {
        @Override
        protected DBUserProfile doInBackground(String... params) {
            String userNickname = params[0];
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            DBUserProfile userProfile = dbHelper.loadDBUserProfile(userNickname);
            if (userProfile == null) {
                userProfile = new DBUserProfile();
                userProfile.setNickname(userNickname);
                userProfile.setCognitoId(dbHelper.getIdentityID());
                userProfile.setFMSInstanceID(FMSInstanceID);
                userProfile.setFacebookID(fbDataBundle.getString("ID"));
                userProfile.setFacebookLink(fbDataBundle.getString("LINK"));
                userProfile.setEmail(fbDataBundle.getString("EMAIL"));
                userProfile.setFirstName(fbDataBundle.getString("FIRSTNAME"));
                userProfile.setLastName(fbDataBundle.getString("LASTNAME"));
                userProfile.setGender(fbDataBundle.getString("GENDER"));
                userProfile.setBirthday(fbDataBundle.getString("BIRTHDAY"));
                userProfile.setNewUser(true);
                userProfile.setDateJoined(df.format(c.getTime()));
                dbHelper.saveDBObject(userProfile);
                return userProfile;
            }
            return null;
        }

        @Override
        protected void onPostExecute(DBUserProfile userProfile) {
            if (userProfile != null) {
                new CheckIfNewUserIsVarsityPlayerDBTask().execute(userProfile);
            }
        }
    }

    private class CheckIfNewUserIsVarsityPlayerDBTask extends AsyncTask<DBUserProfile, Void, Boolean> {
        String userNickname;
        DBUserProfile userProfile;
        DBPlayerProfile playerProfile;
        @Override
        protected Boolean doInBackground(DBUserProfile... params) {
            userProfile = params[0];
            userNickname = userProfile.getNickname();
            String gender = "M";
            if (userProfile.getGender().equals("female")) gender = "F";

            playerProfile = new DBPlayerProfile();
            playerProfile.setGender(gender);
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(playerProfile)
                    .withConsistentRead(false);

            List<DBPlayerProfile> results = dbHelper.getMapper().query(DBPlayerProfile.class, queryExpression);
            for (DBPlayerProfile profile : results) {
                if (profile.getFirstName().equals(userProfile.getFirstName()) &&
                        profile.getLastName().equals(userProfile.getLastName())) {
                    playerProfile = profile;
                    userProfile.setIsVarsityPlayer(true);
                    userProfile.setTeam(profile.getTeam());
                    userProfile.setPlayerIndex(profile.getIndex());
                    playerProfile.setHasUserProfile(true);
                    dbHelper.saveDBObject(userProfile);
                    dbHelper.saveDBObject(playerProfile);
                    Log.d(TAG, "VARSITY PLAYER HIT");
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isVarsityPlayer) {
            if (isVarsityPlayer) {
                showVerifyVarsityPlayerDialog(userProfile, playerProfile);
            }
            else {
                UserProfileParcel userProfileParcel = new UserProfileParcel(ActivityEnum.HOME, userProfile, null);
                finish();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
            }
        }
    }

    private class PushNewNicknameToDBTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            DBUserNickname newUserNickname = new DBUserNickname();
            newUserNickname.setNickname(params[0]);
            newUserNickname.setCognitoID(dbHelper.getIdentityID());
            newUserNickname.setFacebookID(AccessToken.getCurrentAccessToken().getUserId());
            dbHelper.saveDBObject(newUserNickname);
            return null;
        }
    }

    private class PushPlayerProfileChangesToDBTask extends AsyncTask<DBPlayerProfile, Void, Void> {
        @Override
        protected Void doInBackground(DBPlayerProfile... params) {
            dbHelper.saveDBObject(params[0]);
            return null;
        }
    }
}

