package com.peprally.jeremy.peprally.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.custom.SpinnerArrayAdapter;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;
import com.peprally.jeremy.peprally.custom.preferences.NotificationsPref;
import com.peprally.jeremy.peprally.data.PlayerProfile;
import com.peprally.jeremy.peprally.data.UserProfile;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.db_models.DBUsername;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.enums.SchoolsSupportedEnum;
import com.peprally.jeremy.peprally.model.UserResponse;
import com.peprally.jeremy.peprally.model.UsernameResponse;
import com.peprally.jeremy.peprally.network.AWSCredentialProvider;
import com.peprally.jeremy.peprally.network.ApiManager;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private DynamoDBHelper dynamoDBHelper;

    // FB Variables
    private AccessTokenTracker accessTokenTracker;
    private CallbackManager callbackManager;
    private LoginButton facebookLoginButton;

    // UI Variables
    private EditText editTextUsername;
    private InputFilter usernameFilter = new InputFilter() {
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
    private boolean connectionSecured;
    private boolean isNewUsernameTaken = true;
    private Bundle fbDataBundle;
    private SchoolsSupportedEnum userSelectedSchool;
    private String FCMInstanceId;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        // update userSelectedSchool to whatever was stored in sharedPref (will set to default choice if nothing is in sharedPref)
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        userSelectedSchool = SchoolsSupportedEnum.fromString(sharedPref.getString(getResources().getString(R.string.pref_key_school_network), ""));

        if (!Helpers.checkIfNetworkConnectionAvailable((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE))) {
            connectionSecured = false;
            setContentView(R.layout.activity_login);
            setupSchoolSpinner();
            LoginManager.getInstance().logOut();
            facebookLoginButton = (LoginButton) findViewById(R.id.login_button);
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.id_activity_login_container), getResources().getString(R.string.no_connection_text), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OKAY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }});
            final TextView textViewSnackbar = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            textViewSnackbar.setMaxLines(5);
            snackbar.show();
            facebookLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.show();
                }
            });
        }
        else {
            connectionSecured = true;
            if (Helpers.checkGooglePlayServicesAvailable(this)) {
                // facebook login initialization
                callbackManager = CallbackManager.Factory.create();

                dynamoDBHelper = new DynamoDBHelper(this);
                fbDataBundle = new Bundle();
                FCMInstanceId = Helpers.getFCMInstanceId(this);
//                Log.d(TAG, "fcm id = " + FCMInstanceId);
                accessTokenTracker = new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
//                        Log.d(TAG, "access token changed");
                        if (newAccessToken != null) {
                            bundleFacebookData(newAccessToken);
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPreferences.edit().putString("CURRENT_FACEBOOK_ID", newAccessToken.getUserId()).apply();
                        }
                    }
                };

                if (AccessToken.getCurrentAccessToken() == null) {
                    setupLoginScreen();
                } else {
                    setContentView(R.layout.splash);
                    AWSLoginTask();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        } catch (NullPointerException e) {
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
        if (accessTokenTracker != null && connectionSecured)
            accessTokenTracker.stopTracking();
    }

    /***********************************************************************************************
     *********************************** GENERAL_METHODS ********************************
     **********************************************************************************************/
    public interface AWSLoginTaskCallback {
        void onTaskDone(CognitoCachingCredentialsProvider credentialsProvider);
    }

    private void AWSLoginTask() {
        AWSCredentialProvider credentialProviderTask = new AWSCredentialProvider(getApplicationContext(), new AWSLoginTaskCallback() {
            @Override
            public void onTaskDone(CognitoCachingCredentialsProvider credentialsProvider) {
//                new CheckIfNewUserDBTask().execute(credentialsProvider);
	            ApiManager.getInstance()
			            .getLoginService()
			            .tryLogin(credentialsProvider.getIdentityId())
			            .enqueue(new UserResponseCallback());
                dynamoDBHelper.refresh(getApplicationContext());
            }
        });
        credentialProviderTask.execute();
    }

    private String getFacebookDataSafely(GraphResponse response, String key) {
        try {
            return response.getJSONObject().getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void bundleFacebookData(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        String id = getFacebookDataSafely(response, "id");
                        String email = getFacebookDataSafely(response, "email");
                        String firstName = getFacebookDataSafely(response, "first_name");
                        String lastName = getFacebookDataSafely(response, "last_name");
                        String gender = getFacebookDataSafely(response, "gender");
                        String birthday = getFacebookDataSafely(response, "birthday");
                        Profile profile = Profile.getCurrentProfile();
                        if (profile != null) {
                            String link = profile.getLinkUri().toString();
                            fbDataBundle.putString("ID", id);
                            fbDataBundle.putString("LINK", link);
                            fbDataBundle.putString("EMAIL", email);
                            fbDataBundle.putString("FIRSTNAME", firstName);
                            fbDataBundle.putString("LASTNAME", lastName);
                            fbDataBundle.putString("GENDER", gender);
                            fbDataBundle.putString("BIRTHDAY", birthday);
                        }
                    }
                });
        Bundle fbData = new Bundle();
        fbData.putString("fields", "id, email, first_name, last_name, gender, birthday");
        request.setParameters(fbData);
        request.executeAsync();
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void setupLoginScreen() {
        setContentView(R.layout.activity_login);
        setupSchoolSpinner();
        facebookLoginButton = (LoginButton) findViewById(R.id.login_button);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));

        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userSelectedSchool == SchoolsSupportedEnum.PROMPT_TEXT) {
                    Toast.makeText(LoginActivity.this, "Must select a school first!", Toast.LENGTH_SHORT).show();
                    facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            LoginManager.getInstance().logOut();
                        }
                        @Override
                        public void onCancel() {}
                        @Override
                        public void onError(FacebookException error) {}
                    });
                } else {
                    facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            setContentView(R.layout.splash);
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPreferences.edit().putString("CURRENT_FACEBOOK_ID", loginResult.getAccessToken().getUserId()).apply();
                            AWSLoginTask();
                        }

                        @Override
                        public void onCancel() {
                            setContentView(R.layout.activity_login);
                            setupSchoolSpinner();
                            Toast.makeText(LoginActivity.this, "Login attempt canceled.", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(FacebookException error) {
                            setContentView(R.layout.activity_login);
                            setupSchoolSpinner();
                            Toast.makeText(LoginActivity.this, "Login attempt failed.", Toast.LENGTH_LONG).show();
                            error.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    private void setupSchoolSpinner() {
        Spinner schoolSpinner = (Spinner) findViewById(R.id.id_spinner_school_picker);
        schoolSpinner.setAdapter(new SpinnerArrayAdapter(LoginActivity.this, R.layout.spinner_login_item, getResources().getStringArray(R.array.schools_array)));
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        userSelectedSchool = SchoolsSupportedEnum.fromString(sharedPref.getString(getResources().getString(R.string.pref_key_school_network), ""));
        schoolSpinner.setSelection(userSelectedSchool.getIndex());

        schoolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String[] schoolList = getResources().getStringArray(R.array.schools_array);
                userSelectedSchool = SchoolsSupportedEnum.fromString(schoolList[pos]);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                sharedPref.edit().putString(getResources().getString(R.string.pref_key_school_network), userSelectedSchool.toString()).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void showNewUsernameDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_create_username, null);
        dialogBuilder.setView(dialogView);

        editTextUsername = (EditText) dialogView.findViewById(R.id.id_edit_text_new_username_dialog);
        editTextUsername.setFilters(new InputFilter[] {usernameFilter});
        editTextUsername.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editTextUsername.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty() || s.length() < 2) {
	                showUsernameTaken();
                }
                else {
	                ApiManager.getInstance()
			                .getLoginService()
			                .verifyUsername(s.toString().trim().replace(" ", "_"))
			                .enqueue(new VerifyUsernameCallback());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        dialogBuilder.setTitle("Welcome to PepRally!");
        dialogBuilder.setMessage("Pick a username:");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String username = editTextUsername.getText().toString().trim().replace(" ", "_");
                if (username.trim().isEmpty() || username.length() < 2) {
                    Toast.makeText(LoginActivity.this, "Username must be at least 2 characters long.", Toast.LENGTH_SHORT).show();
                    showNewUsernameDialog();
                } else if (isNewUsernameTaken) {
                    Toast.makeText(LoginActivity.this, "Username taken.", Toast.LENGTH_SHORT).show();
                    showNewUsernameDialog();
                } else {
                    new CreateNewUserProfileDBEntryTask().execute(username);
                    new PushNewUserToDBAsyncTask().execute(username);
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

    private void launchVerifyVarsityPlayerDialog(final DBUserProfile userProfile,
                                                 final DBPlayerProfile playerProfile) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_confirm_varsity_player, null);
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
        dialogBuilder.setMessage("We detected that you might be a varsity player, please confirm if you are the player below: ");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                playerProfile.setHasUserProfile(true);
                playerProfile.setUsername(userProfile.getUsername());
                new PushPlayerProfileChangesToDBTask().execute(playerProfile);
                loginPeprally(new UserProfileParcel(ActivityEnum.HOME, userProfile, playerProfile));
            }
        });
        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                loginPeprally(new UserProfileParcel(ActivityEnum.HOME, userProfile, playerProfile));
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showUsernameTaken() {
        if (!editTextUsername.getText().toString().trim().isEmpty()) {
            editTextUsername.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    Helpers.getAPICompatVectorDrawable(getApplicationContext(), R.drawable.ic_error), null);
        }
    }

    private void showUsernameAvailable() {
        if (!editTextUsername.getText().toString().trim().isEmpty()) {
            editTextUsername.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    Helpers.getAPICompatVectorDrawable(getApplicationContext(), R.drawable.ic_check), null);
        }
    }

    private void loginPeprally(UserProfileParcel userProfileParcel) {
        // save current user's username and first name to shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.edit().putString("CURRENT_USERNAME", userProfileParcel.getCurrentUsername()).apply();
        sharedPreferences.edit().putString("CURRENT_FIRSTNAME", userProfileParcel.getFirstname()).apply();

        finish();
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
        startActivity(intent);
        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
    }
    
    private void handleCallbackFailure(Throwable throwable) {
	    StackTraceElement[] arr = throwable.getStackTrace();
	    for (StackTraceElement s : arr) {
		    Log.d(getClass().getName(), s.toString());
	    }
	    Log.d(getClass().getName(), "error msg = " + throwable.getMessage());
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
	private class UserResponseCallback implements Callback<UserResponse> {
		@Override
		public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
			if (response != null) {
				UserResponse userResponse = response.body();
				if (userResponse != null && userResponse.getUserProfile() != null) {
					UserProfile userProfile = userResponse.getUserProfile();
					PlayerProfile playerProfile = userResponse.getPlayerProfile();
					String userSchoolName = userProfile.getSchoolName();
					// make sure user is logging into their selected school network
					if (!userSchoolName.equals(userSelectedSchool.toString()) && !userSchoolName.equals(SchoolsSupportedEnum.PROMPT_TEXT.toString())) {
						Toast.makeText(LoginActivity.this, "Please log into your selected school network! You may change your network in user settings after you log in.", Toast.LENGTH_LONG).show();
						LoginManager.getInstance().logOut();
						setupLoginScreen();
					} else {
//						loginPeprally(new UserProfileParcel(ActivityEnum.HOME, userProfile, playerProfile));
					}
				} else {
					bundleFacebookData(AccessToken.getCurrentAccessToken());
					showNewUsernameDialog();
				}
			} else {
				onFailure(call, new Exception("Null response"));
			}
		}
		
		@Override
		public void onFailure(Call<UserResponse> call, Throwable throwable) {
			handleCallbackFailure(throwable);
		}
	}
	
	private class VerifyUsernameCallback implements Callback<UsernameResponse> {
		@Override
		public void onResponse(Call<UsernameResponse> call, Response<UsernameResponse> response) {
			UsernameResponse usernameResponse = response.body();
			if (usernameResponse != null) {
				if (usernameResponse.isUniqueUsername()) {
					showUsernameAvailable();
				} else {
					showUsernameTaken();
				}
			}
		}
		
		@Override
		public void onFailure(Call<UsernameResponse> call, Throwable throwable) {
			handleCallbackFailure(throwable);
		}
	}

    private class CreateNewUserProfileDBEntryTask extends AsyncTask<String, Void, DBUserProfile> {
        @Override
        protected DBUserProfile doInBackground(String... params) {
            String username = params[0];
            DBUserProfile userProfile = dynamoDBHelper.loadDBUserProfile(username);
            if (userProfile == null) {
                userProfile = new DBUserProfile();
                userProfile.setUsername(username);
                userProfile.setCognitoId(dynamoDBHelper.getIdentityID());
                userProfile.setFCMInstanceId(FCMInstanceId);
                userProfile.setFacebookId(fbDataBundle.getString("ID"));
                userProfile.setFacebookLink(fbDataBundle.getString("LINK"));
                userProfile.setEmail(fbDataBundle.getString("EMAIL"));
                userProfile.setFirstname(fbDataBundle.getString("FIRSTNAME"));
                userProfile.setLastname(fbDataBundle.getString("LASTNAME"));
                userProfile.setGender(fbDataBundle.getString("GENDER"));
                userProfile.setBirthday(fbDataBundle.getString("BIRTHDAY"));
                userProfile.setSchoolName(userSelectedSchool.toString());
                userProfile.setNotificationsPref(new NotificationsPref(true, true, true, true, true));
                userProfile.setNewUser(true);
                userProfile.setHasNewMessage(false);
                userProfile.setHasNewNotification(false);
                userProfile.setDateJoined(Helpers.getTimestampString());
                userProfile.setDateLastLoggedIn(Helpers.getTimestampString());
                userProfile.setTimestampLastLoggedIn(Helpers.getTimestampSeconds());
                dynamoDBHelper.saveDBObject(userProfile);
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

    @SuppressWarnings("unchecked")
    private class CheckIfNewUserIsVarsityPlayerDBTask extends AsyncTask<DBUserProfile, Void, Boolean> {
        DBUserProfile userProfile;
        DBPlayerProfile playerProfile;
        @Override
        protected Boolean doInBackground(DBUserProfile... params) {
            userProfile = params[0];

            playerProfile = new DBPlayerProfile();
            playerProfile.setFirstName(userProfile.getFirstname());
            playerProfile.setLastName(userProfile.getLastname());
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(playerProfile)
                    .withIndexName("FirstName-LastName-index")
                    .withRangeKeyCondition("LastName", new Condition()
                            .withComparisonOperator(ComparisonOperator.EQ)
                            .withAttributeValueList(new AttributeValue().withS(userProfile.getLastname())))
                    .withConsistentRead(false);

            PaginatedQueryList<DBPlayerProfile> results = dynamoDBHelper.getMapper().query(DBPlayerProfile.class, queryExpression);
            for (DBPlayerProfile profile : results) {
                if (profile.getFirstName().equals(userProfile.getFirstname()) &&
                        profile.getLastName().equals(userProfile.getLastname())) {
                    playerProfile = profile;
                    userProfile.setIsVarsityPlayer(true);
                    userProfile.setTeam(profile.getTeam());
                    userProfile.setPlayerIndex(profile.getIndex());
                    playerProfile.setHasUserProfile(true);
                    dynamoDBHelper.saveDBObject(userProfile);
                    dynamoDBHelper.saveDBObject(playerProfile);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isVarsityPlayer) {
            if (isVarsityPlayer) {
                launchVerifyVarsityPlayerDialog(userProfile, playerProfile);
            }
            else {
                loginPeprally(new UserProfileParcel(ActivityEnum.HOME, userProfile, null));
            }
        }
    }

    private class PushNewUserToDBAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            DBUsername newUsername = new DBUsername();
            newUsername.setUsername(params[0]);
            newUsername.setCognitoId(dynamoDBHelper.getIdentityID());
            newUsername.setFacebookId(AccessToken.getCurrentAccessToken().getUserId());
            dynamoDBHelper.saveDBObject(newUsername);
            return null;
        }
    }

    private class PushPlayerProfileChangesToDBTask extends AsyncTask<DBPlayerProfile, Void, Void> {
        @Override
        protected Void doInBackground(DBPlayerProfile... params) {
            dynamoDBHelper.saveDBObject(params[0]);
            return null;
        }
    }
}

