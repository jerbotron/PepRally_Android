package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ActionBar supportActionBar;
    private Fragment viewFragment, editFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private MenuItem editMenuItem;

    private Bundle userProfileBundle;
    private String playerProfileTeam;
    private int playerProfileIndex;
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    private boolean editMode = false;
    private boolean following = false;

    private boolean selfProfile;    // if user is editing his/her own profile
    private boolean userNicknameChanged = false;

    private final int FAV_TEAM_REQUEST_CODE = 0;
    private final int FAV_PLAYER_REQUEST_CODE = 1;

    private static final String TAG = ProfileActivity.class.getSimpleName();

    public static class UserNicknameDBAsyncTaskParams {
        public CognitoCachingCredentialsProvider credentialsProvider;
        public String nickname;
        UserNicknameDBAsyncTaskParams(CognitoCachingCredentialsProvider credentialsProvider, String nickname) {
            this.credentialsProvider = credentialsProvider;
            this.nickname = nickname;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccessToken currentToken = AccessToken.getCurrentAccessToken();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),                    // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        userProfileBundle = getIntent().getBundleExtra("USER_PROFILE_BUNDLE");
        String userFacebookID = userProfileBundle.getString("FACEBOOK_ID");

        // 3 Profile Activity cases currently:
        // - view/edit your own profile as a fan
        // - view/edit your own profile as a player
        // - view a varsity player profile
        if (userFacebookID.equals(currentToken.getUserId())) {
            selfProfile = true;
            new LoadFBProfilePictureTask().execute(userFacebookID);
        }
        else {
            selfProfile = false;
            playerProfileTeam = userProfileBundle.getString("PLAYER_TEAM");
            playerProfileIndex = userProfileBundle.getInt("PLAYER_INDEX", -1);
        }

        new LoadUserProfileFromDBTask().execute(userFacebookID);

        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        supportActionBar = getSupportActionBar();
        supportActionBar.setTitle(null);
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        // Follow Button and FAB
        final LinearLayout followButton = (LinearLayout) findViewById(R.id.button_follow_wrapper);
        final TextView followButtonContent = (TextView) findViewById(R.id.button_follow_content);
        FloatingActionButton fistbumpFab = (FloatingActionButton) findViewById(R.id.profile_firstbump_fab);
        CoordinatorLayout.LayoutParams fistbumpFabLayoutParams = (CoordinatorLayout.LayoutParams) fistbumpFab.getLayoutParams();
        // If user is viewing their own profile
        if (selfProfile) {
            followButton.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.button_view_fistbumps));
            followButtonContent.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.colorAccentDark));
            followButtonContent.setText(Html.fromHtml("<b>VIEW FISTBUMPS</b>"));
            followButtonContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ProfileActivity.this, "VIEW FISTBUMPS", Toast.LENGTH_SHORT).show();
                }
            });

            fistbumpFabLayoutParams.setAnchorId(View.NO_ID);
            fistbumpFab.setLayoutParams(fistbumpFabLayoutParams);
            fistbumpFab.setVisibility(View.GONE);
        }
        // If user is viewing another user's profile
        else {
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

            fistbumpFabLayoutParams.setAnchorId(R.id.profile_app_bar);
            fistbumpFab.setLayoutParams(fistbumpFabLayoutParams);
            fistbumpFab.setVisibility(View.VISIBLE);
            fistbumpFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ProfileActivity.this, "FIST BUMP", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        if (!selfProfile) {
            menu.findItem(R.id.edit_profile_item).setVisible(false);
        }
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

    private Bitmap getFacebookProfilePicture(String userID) throws IOException {
        URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
        Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        return bitmap;
    }


    private void createView() {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        viewFragment = new ProfileViewFragment();
        editFragment = new ProfileEditFragment();
        viewFragment.setArguments(userProfileBundle);
        editFragment.setArguments(userProfileBundle);
        fragmentTransaction.replace(R.id.profile_fragment_container, viewFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        ImageView profilePicture = (ImageView) findViewById(R.id.profile_roster_image);
        if (selfProfile) {
//            profilePicture.setImageBitmap(profileBitmap);
        }
        else {
            String rootImageURL = "https://s3.amazonaws.com/rosterphotos/";
            String extension = userProfileBundle.getString("TEAM").replace(" ", "+") + "/" + userProfileBundle.getString("ROSTER_IMAGE_URL");
            String url = rootImageURL + extension;
            Picasso.with(ProfileActivity.this)
                    .load(url)
                    .into(profilePicture);
        }

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
    }

    private void handleBackPressed() {
        if (editMode) {
            // Push profile changes to DB
            new PushProfileChangesToDBTask().execute();

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
            supportActionBar.setTitle(null);
        }
        else {
            finish();
            if (selfProfile) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
            }
        }
    }

    public void editFavoriteTeam() {
        Intent intent = new Intent(this, FavoriteTeamActivity.class);
        startActivityForResult(intent, FAV_TEAM_REQUEST_CODE);
        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
    }

    public void editFavoritePlayer() {
        String favTeam = ((ProfileEditFragment) editFragment).getFavTeam(editFragment.getView());
        if (favTeam.isEmpty()) {
            Toast.makeText(ProfileActivity.this, "Pick a favorite team first!", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this, FavoritePlayerActivity.class);
            intent.putExtra("CALLING_ACTIVITY", "ProfileActivity");
            intent.putExtra("TEAM", favTeam);
            startActivityForResult(intent, FAV_PLAYER_REQUEST_CODE);
            overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
        }
    }

    public void updateUserProfileBundleString(String key, String value) {
        if (key.equals("NICKNAME")) {
            userNicknameChanged = true;

        }
        userProfileBundle.putString(key, value);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FAV_TEAM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String favoriteTeam = data.getStringExtra("FAVORITE_TEAM");
                userProfileBundle.putString("FAVORITE_TEAM", favoriteTeam);
                ((ProfileEditFragment) editFragment).setFavTeam(editFragment.getView(), favoriteTeam);
            }
        }
        else if (requestCode == FAV_PLAYER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String favoritePlayer = data.getStringExtra("FAVORITE_PLAYER");
                userProfileBundle.putString("FAVORITE_PLAYER", favoritePlayer);
                ((ProfileEditFragment) editFragment).setFavPlayer(editFragment.getView(), favoritePlayer);
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
//        Log.d(TAG, "profile activity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.d(TAG, "profile activity paused");
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.d(TAG, "profile activity started");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.d(TAG, "profile activity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d(TAG, "profile activity destroyed");
    }

    /********************************** AsyncTasks **********************************/

    private class LoadFBProfilePictureTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap profileBitmap = null;
            try {
                profileBitmap = getFacebookProfilePicture(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "COULD NOT GET USER PROFILE");
            }
            return profileBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                ImageView profilePicture = (ImageView) findViewById(R.id.profile_roster_image);
                profilePicture.setImageBitmap(bitmap);
            }
        }
    }

    private class LoadUserProfileFromDBTask extends AsyncTask<String, Void, Void> {
        private DBUserProfile userProfile;
        private DBPlayerProfile playerProfile;
        private DBUserNickname userNickname = null;
        String nickname = userProfileBundle.getString("NICKNAME");
        boolean newProfile = false;
        @Override
        protected Void doInBackground(String... params) {
            if (selfProfile) {
                userProfile = mapper.load(DBUserProfile.class, credentialsProvider.getIdentityId(), userProfileBundle.getString("FIRST_NAME"));
                if (userProfile.getNewUser()) {
                    newProfile = true;
                    SetupNewUserProfile();
                }
                else {
                    Log.d(TAG, "default user already created, fetching user data");
                }
            }
            else {
                playerProfile = mapper.load(DBPlayerProfile.class, playerProfileTeam, playerProfileIndex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (newProfile) {
                new PushNewNicknameToDBTask().execute(nickname);
            }
            BundleUserProfileData();
            createView();
        }

        private void SetupNewUserProfile() {
            userNickname = mapper.load(DBUserNickname.class, credentialsProvider.getIdentityId(), nickname);
            if (userNickname != null) {
                Integer count = 0;
                do {
                    nickname = nickname + "_" + Integer.toString(count);
                    userNickname = mapper.load(DBUserNickname.class, credentialsProvider.getIdentityId(), nickname);
                    count++;
                }
                while (userNickname != null);
            }
            userProfile.setFollowers(0);
            userProfile.setFollowing(0);
            userProfile.setFistbumps(0);
            userProfile.setNickname(nickname);
            userProfile.setFavoriteTeam(null);
            userProfile.setFavoritePlayer(null);
            userProfile.setPepTalk(null);
            userProfile.setTrashTalk(null);
            userProfile.setNewUser(false);
            mapper.save(userProfile);
        }

        private void BundleUserProfileData() {
            if (selfProfile) {
                userProfileBundle.putString("FIRST_NAME", userProfile.getFirstName());
                userProfileBundle.putString("LAST_NAME", userProfile.getLastName());
                userProfileBundle.putString("NICKNAME", userProfile.getNickname());
                userProfileBundle.putInt("FOLLOWERS", userProfile.getFollowers());
                userProfileBundle.putInt("FOLLOWING", userProfile.getFollowing());
                userProfileBundle.putInt("FISTBUMPS", userProfile.getFistbumps());
                userProfileBundle.putString("FAVORITE_TEAM", userProfile.getFavoriteTeam());
                userProfileBundle.putString("FAVORITE_PLAYER", userProfile.getFavoritePlayer());
                userProfileBundle.putString("PEP_TALK", userProfile.getPepTalk());
                userProfileBundle.putString("TRASH_TALK", userProfile.getTrashTalk());
                userProfileBundle.putBoolean("IS_VARSITY_PLAYER", userProfile.getIsVarsityPlayer());
                userProfileBundle.putString("TEAM", userProfile.getTeam());
            }
            else {
                userProfileBundle.putString("FIRST_NAME", playerProfile.getFirstName());
                userProfileBundle.putString("LAST_NAME", playerProfile.getLastName());
                userProfileBundle.putString("NICKNAME", null);
                userProfileBundle.putInt("FOLLOWERS", 0);
                userProfileBundle.putInt("FOLLOWING", 0);
                userProfileBundle.putInt("FISTBUMPS", 0);
                userProfileBundle.putString("FAVORITE_TEAM", null);
                userProfileBundle.putString("FAVORITE_PLAYER", null);
                userProfileBundle.putString("PEP_TALK", null);
                userProfileBundle.putString("TRASH_TALK", null);
                // TODO: fix hardcoded true for IS_VARSITY_PLAYER
                // TODO: allow users to view non-player profiles
                userProfileBundle.putBoolean("IS_VARSITY_PLAYER", true);
                userProfileBundle.putString("TEAM", playerProfile.getTeam());
                userProfileBundle.putInt("NUMBER", playerProfile.getNumber());
                userProfileBundle.putString("YEAR", playerProfile.getYear());
                userProfileBundle.putString("HEIGHT", playerProfile.getHeight());
                userProfileBundle.putString("WEIGHT", playerProfile.getWeight());
                userProfileBundle.putString("POSITION", playerProfile.getPosition());
                userProfileBundle.putString("HOMETOWN", playerProfile.getHometown());
                userProfileBundle.putString("ROSTER_IMAGE_URL", playerProfile.getImageURL());
                userProfileBundle.putBoolean("HAS_USER_PROFILE", playerProfile.getHasUserProfile());
            }
        }
    }

    private class PushNewNicknameToDBTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String nickname = params[0];
            DBUserNickname userNickname = mapper.load(DBUserNickname.class, credentialsProvider.getIdentityId(), nickname);
            if (userNickname != null) {
                mapper.delete(userNickname);
            }
            HashMap<String, AttributeValue> primaryKey = new HashMap<>();
            primaryKey.put("Nickname", new AttributeValue().withS(nickname));
            primaryKey.put("CognitoID", new AttributeValue().withS(credentialsProvider.getIdentityId()));
            primaryKey.put("FacebookID", new AttributeValue().withS(userProfileBundle.getString("FACEBOOK_ID")));
            ddbClient.putItem(new PutItemRequest().withTableName("UserNicknames").withItem(primaryKey));
            return null;
        }
    }

    private class PushProfileChangesToDBTask extends AsyncTask<Void, Void, Void> {
        private DBUserProfile DBUserProfile;
        @Override
        protected Void doInBackground(Void... params) {
            DBUserProfile = mapper.load(DBUserProfile.class, credentialsProvider.getIdentityId(), userProfileBundle.getString("FIRST_NAME"));
            pushUserProfileChanges();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (userNicknameChanged) {
                new PushNewNicknameToDBTask().execute(userProfileBundle.getString("NICKNAME"));
            }
            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
        }

        private void pushUserProfileChanges() {
            DBUserProfile.setFollowing(userProfileBundle.getInt("FOLLOWING"));
            DBUserProfile.setFistbumps(userProfileBundle.getInt("FISTBUMPS"));
            DBUserProfile.setNickname(userProfileBundle.getString("NICKNAME"));
            DBUserProfile.setFavoriteTeam(userProfileBundle.getString("FAVORITE_TEAM"));
            DBUserProfile.setFavoritePlayer(userProfileBundle.getString("FAVORITE_PLAYER"));
            DBUserProfile.setPepTalk(userProfileBundle.getString("PEP_TALK"));
            DBUserProfile.setTrashTalk(userProfileBundle.getString("TRASH_TALK"));
            mapper.save(DBUserProfile);
        }
    }
}
