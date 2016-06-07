package com.peprally.jeremy.peprally.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMappingException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.ProfileViewPagerAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserNickname;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.fragments.ProfileEditFragment;
import com.peprally.jeremy.peprally.fragments.ProfilePostsFragment;
import com.peprally.jeremy.peprally.fragments.ProfileInfoFragment;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.ProfileViewPager;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    // UI Variables
    private ActionBar supportActionBar;
    private AppBarLayout appBarLayout;
    private FloatingActionButton actionFAB;
    private ProfileInfoFragment infoFragment;
    private ProfilePostsFragment postsFragment;
    private ProfileEditFragment editFragment;
    private MenuItem editMenuItem;
    private TabLayout tabLayout;
    private ViewPager viewPagerProfile;
    private ProfileViewPagerAdapter adapter;

    // General Variables
    public static UserProfileParcel userProfileParcel;

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private boolean following = false;  // TODO: TEMP FLAG, REMOVE ONCE FOLLOW FEATURE IS IMPLEMENTED
    private boolean editMode = false;
    private boolean selfProfile;        // if user is editing his/her own profile

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),                    // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        userProfileParcel = getIntent().getExtras().getParcelable("USER_PROFILE_PARCEL");
        assert userProfileParcel != null;
        selfProfile = userProfileParcel.getIsSelfProfile();
        final String userFacebookID = userProfileParcel.getFacebookID();

        // 3 Profile Activity cases currently:
        // - view/edit your own profile as a fan
        // - view/edit your own profile as a player
        // - view a varsity player profile
        if (selfProfile = userProfileParcel.getIsSelfProfile()) {
            new LoadFBProfilePictureTask().execute(userFacebookID);
        }

        new LoadUserProfileFromDBTask().execute(selfProfile);

        setContentView(R.layout.activity_profile);
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.profile_collapse_toolbar);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        assert collapsingToolbarLayout != null && toolbar != null;
        collapsingToolbarLayout.setTitleEnabled(false);
        setSupportActionBar(toolbar);
        supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle(null);
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        appBarLayout = (AppBarLayout) findViewById(R.id.profile_app_bar_layout);
        assert appBarLayout != null;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (!editMode) {
                    if (verticalOffset == 0 && supportActionBar.getTitle() != null) {
                        supportActionBar.setTitle(null);
                    }
                    else if(verticalOffset <= -375 && supportActionBar.getTitle() == null) {
                        supportActionBar.setTitle(userProfileParcel.getFirstname());
                    }
                }
            }
        });

        // Follow Button and FAB
        final LinearLayout followButton = (LinearLayout) findViewById(R.id.button_follow_wrapper);
        final TextView followButtonContent = (TextView) findViewById(R.id.button_follow_content);
        actionFAB = (FloatingActionButton) findViewById(R.id.fab_profile_action);
        assert followButton != null && followButtonContent != null && actionFAB != null;
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

            actionFAB.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_msg));
            actionFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorFABPost)));
            actionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
                    startActivityForResult(intent, Helpers.NEW_POST_REQUEST_CODE);
                    overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
                }
            });
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

            actionFAB.setImageDrawable(getResources().getDrawable(R.drawable.icon_fist_bump));
            actionFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorFABFistbump)));
            actionFAB.setOnClickListener(new View.OnClickListener() {
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
            menu.findItem(R.id.id_item_edit_profile).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                handleBackPressed();
                return true;
            case R.id.id_item_edit_profile:
                if (!editMode) {
                    // Switch Fragment to editFragment
                    appBarLayout.setExpanded(false, false);
                    tabLayout.setVisibility(View.GONE);
                    actionFAB.setVisibility(View.INVISIBLE);
                    adapter.addFrag(editFragment, "Edit Profile");
                    adapter.attachFrag(2);
                    adapter.notifyDataSetChanged();
                    viewPagerProfile.setCurrentItem(2);
                    ((ProfileViewPager) viewPagerProfile).setAllowedSwipeDirection(ProfileViewPager.SwipeDirection.none);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Helpers.FAV_TEAM_REQUEST_CODE:
                    String favoriteTeam = data.getStringExtra("FAVORITE_TEAM");
                    userProfileParcel.setFavoriteTeam(favoriteTeam);
                    editFragment.setFavTeam(favoriteTeam);
                    break;
                case Helpers.FAV_PLAYER_REQUEST_CODE:
                    String favoritePlayer = data.getStringExtra("FAVORITE_PLAYER");
                    userProfileParcel.setFavoritePlayer(favoritePlayer);
                    editFragment.setFavPlayer(favoritePlayer);
                    break;
                case Helpers.NEW_POST_REQUEST_CODE:
                    postsFragment.addPostToAdapter(data.getStringExtra("NEW_POST_TEXT"));
                    viewPagerProfile.setCurrentItem(1);
                    break;
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

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    public UserProfileParcel getUserProfileParcel() {
        return userProfileParcel;
    }

    public void editFavoriteTeam() {
        Intent intent = new Intent(this, FavoriteTeamActivity.class);
        startActivityForResult(intent, Helpers.FAV_TEAM_REQUEST_CODE);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public void editFavoritePlayer() {
        String favTeam = editFragment.getFavTeam();
        if (favTeam.isEmpty()) {
            Toast.makeText(ProfileActivity.this, "Pick a favorite team first!", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this, FavoritePlayerActivity.class);
            intent.putExtra("CALLING_ACTIVITY", "ProfileActivity");
            intent.putExtra("TEAM", favTeam);
            startActivityForResult(intent, Helpers.FAV_PLAYER_REQUEST_CODE);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }

    private void refreshPostsFragment() {
        // Set view pager to postsFragment
        viewPagerProfile.setCurrentItem(1);
        postsFragment.refreshAdapter();
    }

    private Bitmap getFacebookProfilePicture(String userID) throws IOException {
        URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
        return BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void createView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        infoFragment = new ProfileInfoFragment();
        postsFragment = new ProfilePostsFragment();
        editFragment = new ProfileEditFragment();

        viewPagerProfile = (ProfileViewPager) findViewById(R.id.viewpager_profile);
        adapter = new ProfileViewPagerAdapter(fragmentManager);
        adapter.addFrag(infoFragment, "Info");
        adapter.addFrag(postsFragment, "Posts");
        viewPagerProfile.setAdapter(adapter);
        viewPagerProfile.setCurrentItem(0);

        tabLayout = (TabLayout) findViewById(R.id.tablayout_profile);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPagerProfile);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPagerProfile.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        ImageView profilePicture = (ImageView) findViewById(R.id.profile_roster_image);
        if (!selfProfile) {
            String rootImageURL = "https://s3.amazonaws.com/rosterphotos/";
            String team = userProfileParcel.getTeam();
            String extension = team.replace(" ", "+") + "/" + userProfileParcel.getRosterImageURL();
            String url = rootImageURL + extension;
            Picasso.with(ProfileActivity.this)
                    .load(url)
                    .placeholder(R.drawable.default_placeholder)
                    .into(profilePicture);
        }

        final TextView fistBumpsTextView = (TextView) findViewById(R.id.profile_fist_bumps);
        final TextView followersTextView = (TextView) findViewById(R.id.profile_followers);
        final TextView followingTextView = (TextView) findViewById(R.id.profile_following);
        assert fistBumpsTextView != null && followersTextView != null && followingTextView != null;

        fistBumpsTextView.setText(Html.fromHtml("<b>"
                + Integer.toString(userProfileParcel.getFistbumpsCount())
                + "</b> " + getString(R.string.profile_fist_bumps)));
        followersTextView.setText(Html.fromHtml("<b>"
                + Integer.toString(userProfileParcel.getFollowersCount())
                + "</b> " + getString(R.string.profile_followers)));
        followingTextView.setText(Html.fromHtml("<b>"
                + Integer.toString(userProfileParcel.getFollowingCount())
                + "</b> " + getString(R.string.profile_following)));
    }

    private void handleBackPressed() {
        if (editMode) {
            // Push profile changes to DB
            new PushProfileChangesToDBTask().execute();

            // Switch Fragment back to infoFragment
            appBarLayout.setExpanded(true, false);
            tabLayout.setVisibility(View.VISIBLE);
            actionFAB.setVisibility(View.VISIBLE);
            adapter.detachFrag(2);
            adapter.removeFrag(2);
            adapter.notifyDataSetChanged();
            viewPagerProfile.setCurrentItem(0);
            ((ProfileViewPager) viewPagerProfile).setAllowedSwipeDirection(ProfileViewPager.SwipeDirection.all);

            // Re-enable Edit Icon
            editMenuItem.setEnabled(true);
            editMenuItem.setVisible(true);
            editMode = false;

            // Change back Actionbar title
            supportActionBar.setTitle(null);
        }
        else {
            if (selfProfile) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("NICKNAME", userProfileParcel.getNickname());
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
            else {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        }
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
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
                final ImageView profilePicture = (ImageView) findViewById(R.id.profile_roster_image);
                assert profilePicture != null;
                profilePicture.setImageBitmap(bitmap);
            }
        }
    }

    private class LoadUserProfileFromDBTask extends AsyncTask<Boolean, Void, Boolean> {
        private DBUserProfile userProfile;
        private DBPlayerProfile playerProfile;
        @Override
        protected Boolean doInBackground(Boolean... params) {
            // If loading ProfileActivity for user's own profile
            if (params[0]) {
                try {
                    userProfile = mapper.load(DBUserProfile.class, userProfileParcel.getNickname());
                }
                catch (DynamoDBMappingException e) {
                    Log.d(TAG, "LoadUserProfileFromDBTask: mapping exception occurred, returning from task.");
                    this.cancel(true);
                }
                if (userProfile.getNewUser()) {
                    SetupNewUserProfile();
                    return true;
                }
                else {
                    Log.d(TAG, "default user already created, fetching user data");
                }
            }
            else {
                playerProfile = mapper.load(DBPlayerProfile.class, userProfileParcel.getTeam(), userProfileParcel.getIndex());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean newUserProfile) {
            UpdateUserProfileParcel();
            createView();
        }

        private void SetupNewUserProfile() {
//            /*
//            Handle cases of duplicate first/last name users,
//            append a number in the back of default nicknames.
//             */
//            userNickname = mapper.load(DBUserNickname.class, nickname);
//            if (userNickname != null) {
//                Integer count = 0;
//                do {
//                    nickname = nickname + Integer.toString(count);
//                    userNickname = mapper.load(DBUserNickname.class, nickname);
//                    count++;
//                }
//                while (userNickname != null);
//            }
            // Set default user profile values
            userProfile.setFollowersCount(0);
            userProfile.setFollowingCount(0);
            userProfile.setFistbumpsCount(0);
            userProfile.setPostsCount(0);
            userProfile.setFavoriteTeam(null);
            userProfile.setFavoritePlayer(null);
            userProfile.setPepTalk(null);
            userProfile.setTrashTalk(null);
            userProfile.setNewUser(false);
            mapper.save(userProfile);
        }

        private void UpdateUserProfileParcel() {
            if (selfProfile) {
                userProfileParcel.setFirstname(userProfile.getFirstName());
                userProfileParcel.setLastname(userProfile.getLastName());
                userProfileParcel.setNickname(userProfile.getNickname());
                userProfileParcel.setFollowersCount(userProfile.getFollowersCount());
                userProfileParcel.setFollowingCount(userProfile.getFollowingCount());
                userProfileParcel.setFistbumpsCount(userProfile.getFistbumpsCount());
                userProfileParcel.setPostsCount(userProfile.getPostsCount());
                userProfileParcel.setFavoriteTeam(userProfile.getFavoriteTeam());
                userProfileParcel.setFavoritePlayer(userProfile.getFavoritePlayer());
                userProfileParcel.setPepTalk(userProfile.getPepTalk());
                userProfileParcel.setTrashTalk(userProfile.getTrashTalk());
                userProfileParcel.setIsVarsityPlayer(userProfile.getIsVarsityPlayer());
                userProfileParcel.setIsSelfProfile(true);
            }
            else {
                userProfileParcel.setFirstname(playerProfile.getFirstName());
                userProfileParcel.setLastname(playerProfile.getLastName());
                userProfileParcel.setNickname(null);
                userProfileParcel.setFollowersCount(0);
                userProfileParcel.setFollowingCount(0);
                userProfileParcel.setFistbumpsCount(0);
                userProfileParcel.setPostsCount(0);
                userProfileParcel.setFavoriteTeam(null);
                userProfileParcel.setFavoritePlayer(null);
                userProfileParcel.setPepTalk(null);
                userProfileParcel.setTrashTalk(null);
                userProfileParcel.setIsVarsityPlayer(true);
                userProfileParcel.setIsSelfProfile(false);
                // TODO: fix hardcoded true for IS_VARSITY_PLAYER
                // TODO: allow users to view non-player profiles
                userProfileParcel.setTeam(playerProfile.getTeam());
                userProfileParcel.setNumber(playerProfile.getNumber());
                userProfileParcel.setYear(playerProfile.getYear());
                userProfileParcel.setHeight(playerProfile.getHeight());
                userProfileParcel.setWeight(playerProfile.getWeight());
                userProfileParcel.setPosition(playerProfile.getPosition());
                userProfileParcel.setHometown(playerProfile.getHometown());
                userProfileParcel.setRosterImageURL(playerProfile.getImageURL());
                userProfileParcel.setHasUserProfile(playerProfile.getHasUserProfile());
            }
        }
    }

    private class PushProfileChangesToDBTask extends AsyncTask<Void, Void, Void> {
        private DBUserProfile DBUserProfile;
        @Override
        protected Void doInBackground(Void... params) {
            DBUserProfile = mapper.load(DBUserProfile.class, userProfileParcel.getNickname());
            pushUserProfileChanges();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
        }

        private void pushUserProfileChanges() {
            DBUserProfile.setFollowingCount(userProfileParcel.getFollowingCount());
            DBUserProfile.setFistbumpsCount(userProfileParcel.getFistbumpsCount());
            DBUserProfile.setNickname(userProfileParcel.getNickname());
            DBUserProfile.setFavoriteTeam(userProfileParcel.getFavoriteTeam());
            DBUserProfile.setFavoritePlayer(userProfileParcel.getFavoritePlayer());
            DBUserProfile.setPepTalk(userProfileParcel.getPepTalk());
            DBUserProfile.setTrashTalk(userProfileParcel.getTrashTalk());
            mapper.save(DBUserProfile);
        }
    }
}
