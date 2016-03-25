package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.ProfilePictureView;

public class ProfileActivity extends AppCompatActivity {

    private ActionBar supportActionBar;
    private Fragment viewFragment, editFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private MenuItem editMenuItem;

    private Bundle userProfileBundle;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    private Boolean editMode = false;
    private Boolean following = false;

    private final int FAV_TEAM_REQUEST_CODE = 0;
    private final int FAV_PLAYER_REQUEST_CODE = 1;

    private static final String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),                    // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        userProfileBundle = getIntent().getBundleExtra("USER_PROFILE_BUNDLE");

        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        // Facebook related setups
        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        String userId = currentToken.getUserId();
        ProfilePictureView profilePicture = (ProfilePictureView) findViewById(R.id.profile_image_profile);
        profilePicture.setProfileId(userId);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        viewFragment = new ProfileViewFragment();
        editFragment = new ProfileEditFragment();
        viewFragment.setArguments(userProfileBundle);
        editFragment.setArguments(userProfileBundle);
        fragmentTransaction.replace(R.id.profile_fragment_container, viewFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        TextView fistBumpsTextView = (TextView) findViewById(R.id.profile_fist_bumps);
        TextView followersTextView = (TextView) findViewById(R.id.profile_followers);
        TextView followingTextView = (TextView) findViewById(R.id.profile_following);

        fistBumpsTextView.setText(Html.fromHtml("<b>"
                + Integer.toString(userProfileBundle.getInt("FISTBUMPS"))
                + "</b> " + getString(R.string.profile_fist_bumps)));
        followersTextView.setText(Html.fromHtml("<b>"
                + Integer.toString(userProfileBundle.getInt("FOLLOWERS"))
                + "</b> " + getString(R.string.profile_followers)));
        followingTextView.setText(Html.fromHtml("<b>"
                + Integer.toString(userProfileBundle.getInt("FOLLOWING"))
                + "</b> " + getString(R.string.profile_following)));

        // Follow Button Action
        final LinearLayout followButton = (LinearLayout) findViewById(R.id.button_follow_wrapper);
        final TextView followButtonContent = (TextView) findViewById(R.id.button_follow_content);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (following) {
                    followButton.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.button_follow));
                    followButtonContent.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.colorPrimary));
                    followButtonContent.setText("Follow");
                    followButtonContent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_follow, 0, 0, 0);
                    following = false;
                }
                else {
                    followButton.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.button_following));
                    followButtonContent.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.colorWhite));
                    followButtonContent.setText("Following");
                    followButtonContent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_followed, 0, 0, 0);
                    following = true;
                }
            }
        });

        // Fistbump FAB Action
        FloatingActionButton fistbumpFab = (FloatingActionButton) findViewById(R.id.profile_firstbump_fab);
        fistbumpFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "FIST BUMP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                handleBackPressed();
                return true;
            case R.id.edit_profile_item:
                if (!editMode) {
                    // Switch Fragment to editFragment
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.profile_fragment_container, editFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    editMode = true;
                    editMenuItem = item;

                    // Hide Edit Icon
                    item.setEnabled(false);
                    item.setVisible(false);

                    // Change Actionbar title
                    supportActionBar.setTitle("Edit Profile");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleBackPressed() {
        if (editMode) {
            // Push profile changes to DB
            new pushProfileChangesToDBTask().execute(credentialsProvider);

            // Switch Fragment back to viewFragment
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.profile_fragment_container, viewFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            // Re-enable Edit Icon
            editMenuItem.setEnabled(true);
            editMenuItem.setVisible(true);
            editMode = false;

            // Change back Actionbar title
            supportActionBar.setTitle(R.string.app_name);
        }
        else {
            finish();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.left_in, R.anim.left_out);
        }
    }

    public void editFavoriteTeam() {
        Intent intent = new Intent(this, FavoriteTeamActivity.class);
        startActivityForResult(intent, FAV_TEAM_REQUEST_CODE);
        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
    }

    public void editFavoritePlayer() {
        Intent intent = new Intent(this, FavoritePlayerActivity.class);
        startActivityForResult(intent, FAV_PLAYER_REQUEST_CODE);
        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
    }

    public void updateUserProfileBundleString(String key, String value) {
        userProfileBundle.putString(key, value);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FAV_TEAM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String newFavoriteTeam = data.getStringExtra("FAVORITE_TEAM");
                userProfileBundle.putString("FAVORITE_TEAM", newFavoriteTeam);
                ((ProfileEditFragment) editFragment).updateFavTeam(editFragment.getView(), newFavoriteTeam);
            }
        }
        else if (requestCode == FAV_PLAYER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "updateFavPlayer not supported yet");
//                ((ProfileEditFragment) editFragment).updateFavPlayer(editFragment.getView(), data.getStringExtra("FAVORITE_PLAYER"));
            }
        }
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "profile activity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "profile activity paused");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "profile activity started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "profile activity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "profile activity destroyed");
    }

    private class pushProfileChangesToDBTask extends AsyncTask<CognitoCachingCredentialsProvider, Void, Void> {
        private UserProfile userProfile;
        @Override
        protected Void doInBackground(CognitoCachingCredentialsProvider... params) {
            CognitoCachingCredentialsProvider credentialsProvider = params[0];
            userProfile = mapper.load(UserProfile.class, credentialsProvider.getIdentityId(), userProfileBundle.getString("FIRST_NAME"));
            pushUserProfileChanges();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Log.d(TAG, "task done");
            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
        }

        private void pushUserProfileChanges() {
            userProfile.setFollowing(userProfileBundle.getInt("FOLLOWING"));
            userProfile.setFistbumps(userProfileBundle.getInt("FISTBUMPS"));
            userProfile.setMotto(userProfileBundle.getString("MOTTO"));
            userProfile.setFavoriteTeam(userProfileBundle.getString("FAVORITE_TEAM"));
            userProfile.setFavoritePlayer(userProfileBundle.getString("FAVORITE_PLAYER"));
            userProfile.setPepTalk(userProfileBundle.getString("PEP_TALK"));
            userProfile.setTrashTalk(userProfileBundle.getString("TRASH_TALK"));
            mapper.save(userProfile);
        }
    }
}
