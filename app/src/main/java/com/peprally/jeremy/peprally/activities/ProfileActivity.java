package com.peprally.jeremy.peprally.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.ProfileViewPagerAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.fragments.ProfileEditFragment;
import com.peprally.jeremy.peprally.fragments.ProfilePostsFragment;
import com.peprally.jeremy.peprally.fragments.ProfileInfoFragment;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.ProfileViewPager;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;
import com.squareup.picasso.Picasso;

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

        // 3 Profile Activity cases currently:
        // - view/edit your own img_default_profile as a fan
        // - view/edit your own img_default_profile as a player
        // - view a varsity player img_default_profile
        setContentView(R.layout.activity_profile);
        new LoadUserProfileFromDBTask().execute();

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
        // If user is viewing their own img_default_profile
        if (userProfileParcel.getIsSelfProfile()) {
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
        // If user is viewing another user's img_default_profile
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

            actionFAB.setImageDrawable(getResources().getDrawable(R.drawable.ic_fist_bump));
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
        if (!userProfileParcel.getIsSelfProfile()) {
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
//        Log.d(TAG, "img_default_profile activity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.d(TAG, "img_default_profile activity paused");
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.d(TAG, "img_default_profile activity started");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.d(TAG, "img_default_profile activity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d(TAG, "img_default_profile activity destroyed");
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

        final ImageView imageView_profilePicture = (ImageView) findViewById(R.id.id_image_view_profile_image);
        final TextView textView_fistbumps = (TextView) findViewById(R.id.profile_fist_bumps);
        final TextView textView_followers = (TextView) findViewById(R.id.profile_followers);
        final TextView textView_following = (TextView) findViewById(R.id.profile_following);
        assert imageView_profilePicture != null && textView_fistbumps != null && textView_followers != null && textView_following != null;

        final String imageURL;
        // Profile Image Setup
        if (userProfileParcel.getIsVarsityPlayer()) {
            String rootImageURL = "https://s3.amazonaws.com/rosterphotos/";
            String team = userProfileParcel.getTeam();
            String extension = team.replace(" ", "+") + "/" + userProfileParcel.getRosterImageURL();
            imageURL = rootImageURL + extension;
            Picasso.with(ProfileActivity.this)
                    .load(imageURL)
                    .placeholder(R.drawable.img_default_ut_placeholder)
                    .into(imageView_profilePicture);
        }
        else {
            imageURL = "https://graph.facebook.com/" + userProfileParcel.getFacebookID() + "/picture?width=9999";
            Helpers.setFacebookProfileImage(this,
                    imageView_profilePicture,
                    userProfileParcel.getFacebookID(),
                    3);
        }
        imageView_profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileImageDialog(imageURL);
            }
        });

        textView_fistbumps.setText(Html.fromHtml("<b>"
                + Integer.toString(userProfileParcel.getFistbumpsCount())
                + "</b> " + getString(R.string.profile_fist_bumps)));
        textView_followers.setText(Html.fromHtml("<b>"
                + Integer.toString(userProfileParcel.getFollowersCount())
                + "</b> " + getString(R.string.profile_followers)));
        textView_following.setText(Html.fromHtml("<b>"
                + Integer.toString(userProfileParcel.getFollowingCount())
                + "</b> " + getString(R.string.profile_following)));
    }

    private void handleBackPressed() {
        if (editMode) {
            // Push img_default_profile changes to DB
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
            if (userProfileParcel.getIsSelfProfile()) {
                finish();
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("NICKNAME", userProfileParcel.getProfileNickname());
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
            else {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        }
    }

    private void showProfileImageDialog(String imageURL) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_profile_image, null);
        dialogBuilder.setView(dialogView);

        ImageView profileImage = (ImageView) dialogView.findViewById(R.id.id_image_view_dialog_profile_image);

        Picasso.with(this)
                .load(imageURL)
                .placeholder(R.drawable.img_default_profile)
                .error(R.drawable.img_default_profile)
                .into(profileImage);

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class LoadUserProfileFromDBTask extends AsyncTask<Void, Void, Void> {
        private DBUserProfile userProfile;
        private DBPlayerProfile playerProfile;
        @Override
        protected Void doInBackground(Void... params) {
            // 3 Cases:
            // 1) Load general user's profile
            // 2) Load varsity player's profile
            // 3) Load varsity player's profile who also has a general profile
            try {
                userProfile = mapper.load(DBUserProfile.class, userProfileParcel.getProfileNickname());
            }
            catch (DynamoDBMappingException e) {
                userProfile = null;
            }

            String playerTeam;
            int playerIndex;
            if (userProfile != null && userProfileParcel.getTeam() == null && userProfileParcel.getIndex().equals(Helpers.INTEGER_INVALID))
            {
                playerTeam = userProfile.getTeam();
                playerIndex = userProfile.getPlayerIndex();
            }
            else {
                playerTeam = userProfileParcel.getTeam();
                playerIndex = userProfileParcel.getIndex();
            }

            try {
                playerProfile = mapper.load(DBPlayerProfile.class, playerTeam, playerIndex);
            }
            catch (DynamoDBMappingException e) {
                playerProfile = null;
            }

            if (userProfile != null && playerProfile == null) {
                if (userProfile.getNewUser())
                    SetupNewUserProfile();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            UpdateUserProfileParcel();
            createView();
        }

        private void SetupNewUserProfile() {
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
            if (playerProfile != null) {
                userProfile.setIsVarsityPlayer(true);
                userProfile.setTeam(playerProfile.getTeam());
                userProfile.setPlayerIndex(playerProfile.getIndex());
            }
            else {
                userProfile.setIsVarsityPlayer(false);
                userProfile.setPlayerIndex(Helpers.INTEGER_INVALID);
                mapper.save(playerProfile);
            }
            mapper.save(userProfile);
        }

        private void UpdateUserProfileParcel() {
            if (userProfile != null) {
                userProfileParcel.setFirstname(userProfile.getFirstName());
                userProfileParcel.setLastname(userProfile.getLastName());
                userProfileParcel.setProfileNickname(userProfile.getNickname());
                userProfileParcel.setFollowersCount(userProfile.getFollowersCount());
                userProfileParcel.setFollowingCount(userProfile.getFollowingCount());
                userProfileParcel.setFistbumpsCount(userProfile.getFistbumpsCount());
                userProfileParcel.setPostsCount(userProfile.getPostsCount());
                userProfileParcel.setFavoriteTeam(userProfile.getFavoriteTeam());
                userProfileParcel.setFavoritePlayer(userProfile.getFavoritePlayer());
                userProfileParcel.setPepTalk(userProfile.getPepTalk());
                userProfileParcel.setTrashTalk(userProfile.getTrashTalk());
                userProfileParcel.setIsVarsityPlayer(userProfile.getIsVarsityPlayer());
            }
            if (playerProfile != null) {
                userProfileParcel.setIsVarsityPlayer(true);
                userProfileParcel.setFirstname(playerProfile.getFirstName());
                userProfileParcel.setLastname(playerProfile.getLastName());
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
            DBUserProfile = mapper.load(DBUserProfile.class, userProfileParcel.getProfileNickname());
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
            DBUserProfile.setNickname(userProfileParcel.getProfileNickname());
            DBUserProfile.setFavoriteTeam(userProfileParcel.getFavoriteTeam());
            DBUserProfile.setFavoritePlayer(userProfileParcel.getFavoritePlayer());
            DBUserProfile.setPepTalk(userProfileParcel.getPepTalk());
            DBUserProfile.setTrashTalk(userProfileParcel.getTrashTalk());
            mapper.save(DBUserProfile);
        }
    }
}
