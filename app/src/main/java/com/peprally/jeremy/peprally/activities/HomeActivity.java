package com.peprally.jeremy.peprally.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.ProfileViewPagerAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.fragments.BrowseTeamsFragment;
import com.peprally.jeremy.peprally.fragments.TrendingFragment;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.ActivityEnum;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private DrawerLayout drawer;
    private ViewPager viewPagerHome;

    // Fragment Variables
    private TrendingFragment trendingFragment;

    // General Variables
    private static final String TAG = HomeActivity.class.getSimpleName();
    private UserProfileParcel userProfileParcel;
    private String nickname;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        Profile fbProfile = Profile.getCurrentProfile();

        // Set main activity content view
        setContentView(R.layout.activity_home);

        // Initialize member variables
        nickname = getIntent().getStringExtra("NICKNAME");
        if (nickname == null || nickname.isEmpty()) {
            new FetchUserNicknameDBTask().execute(fbProfile);
        }
        else {
            userProfileParcel = new UserProfileParcel(ActivityEnum.HOME,
                                                    nickname,
                                                    fbProfile.getFirstName(),
                                                    fbProfile.getLastName(),
                                                    nickname,
                                                    fbProfile.getId(),
                                                    true);  // user is viewing self profile
            viewPagerHome = (ViewPager) findViewById(R.id.viewpager_home);
            setupViewPager(viewPagerHome);
        }

        // Setup UI components
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_home);
        assert toolbar != null;
        toolbar.setTitle("Pep Rally");
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.id_tablayout_home);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPagerHome);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_home);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_home);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        // Fetch FB img_default_profile photo and first name and display them in sidebar header
        View headerView = navigationView.getHeaderView(0);
        LinearLayout header = (LinearLayout) headerView.findViewById(R.id.id_sidebar_header);
        ProfilePictureView profilePicture = (ProfilePictureView) headerView.findViewById(R.id.profile_image_header);
        profilePicture.setProfileId(currentToken.getUserId());
        TextView sidebar_name = (TextView) headerView.findViewById(R.id.sidebar_header_name);
        sidebar_name.setText(fbProfile.getFirstName());
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavBarHeaderClick();
            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPagerHome.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//        viewPagerHome = (ViewPager) findViewById(R.id.viewpager_home);
//        if (id == R.id.nav_trending) {
//            viewPagerHome.setCurrentItem(0);
//        } else if (id == R.id.nav_events) {
//            viewPagerHome.setCurrentItem(1);
//        } else if (id == R.id.nav_browse_teams) {
//            viewPagerHome.setCurrentItem(2);
        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        } else if (id == R.id.nav_logout) {
            finish();
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onNavBarHeaderClick() {
        finish();
        // Viewing self profile
        Intent intent = new Intent(this, ProfileActivity.class);
        userProfileParcel.setCurrentActivity(ActivityEnum.PROFILE);
        userProfileParcel.setIsSelfProfile(true);
        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Helpers.NEW_POST_REQUEST_CODE:
                    trendingFragment.addPostToAdapter(data.getStringExtra("NEW_POST_TEXT"));
                    break;
            }
        }
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/

    public void launchBrowsePlayerActivity(String team) {
        Intent intent = new Intent(HomeActivity.this, FavoritePlayerActivity.class);
        intent.putExtra("CALLING_ACTIVITY", "HomeActivity");
        intent.putExtra("CURRENT_USER_NICKNAME", nickname);
        intent.putExtra("TEAM", team);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public void launchNewPostActivity() {
        Intent intent = new Intent(HomeActivity.this, NewPostActivity.class);
        startActivityForResult(intent, Helpers.NEW_POST_REQUEST_CODE);
        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
    }

    public UserProfileParcel getUserProfileParcel() {
        return userProfileParcel;
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/

    private void setupViewPager(ViewPager viewPager) {
        trendingFragment = new TrendingFragment();
        BrowseTeamsFragment browseTeamsFragment = new BrowseTeamsFragment();
        ProfileViewPagerAdapter adapter = new ProfileViewPagerAdapter(getSupportFragmentManager());
//        adapter.addFrag(new EventsFragment(), "Events");
        adapter.addFrag(trendingFragment, "Trending");
        adapter.addFrag(browseTeamsFragment, "Teams");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchUserNicknameDBTask extends AsyncTask<Profile, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Profile... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    HomeActivity.this,                        // Context
                    AWSCredentialProvider.IDENTITY_POOL_ID,   // Identity Pool ID
                    AWSCredentialProvider.COGNITO_REGION      // Region
            );
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            ddbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            DBUserProfile userProfile = new DBUserProfile();
            userProfile.setCognitoId(credentialsProvider.getIdentityId());
            DynamoDBQueryExpression<DBUserProfile> queryExpression = new DynamoDBQueryExpression<DBUserProfile>()
                    .withIndexName("CognitoID-index")
                    .withHashKeyValues(userProfile)
                    .withConsistentRead(false);

            List<DBUserProfile> results = mapper.query(DBUserProfile.class, queryExpression);
            if (results == null || results.size() == 0) {
                Log.d(TAG, "CognitoID not found: current user does not exist in database.");
            }
            else{
                if (results.size() == 1) {
                    userProfile = results.get(0);
                    nickname = userProfile.getNickname();
                    Profile fbProfile = params[0];
                    userProfileParcel = new UserProfileParcel(ActivityEnum.HOME,
                                                            nickname,
                                                            fbProfile.getFirstName(),
                                                            fbProfile.getLastName(),
                                                            nickname,
                                                            fbProfile.getId(),
                                                            true);  // user is viewing self profile
                    return true;
                }
                Log.d(TAG, "Query result should have only returned single user!");
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                viewPagerHome = (ViewPager) findViewById(R.id.viewpager_home);
                setupViewPager(viewPagerHome);
            }
        }
    }
}
