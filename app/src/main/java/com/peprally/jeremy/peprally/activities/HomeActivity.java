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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.ProfileViewPagerAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.fragments.BrowseTeamsFragment;
import com.peprally.jeremy.peprally.fragments.TrendingFragment;
import com.peprally.jeremy.peprally.utils.ActivityEnum;
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private DynamoDBHelper dbHelper;

    // UI Variables
    private DrawerLayout drawer;
    private ViewPager viewPagerHome;

    // Fragment Variables
    private TrendingFragment trendingFragment;

    // General Variables
    private static final String TAG = HomeActivity.class.getSimpleName();
    private UserProfileParcel userProfileParcel;

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
        dbHelper = new DynamoDBHelper(this);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");

        if (userProfileParcel == null) {
            new FetchUserProfileParcelTask().execute(fbProfile);
        }
        else {
            viewPagerHome = (ViewPager) findViewById(R.id.id_viewpager_home);
            setupViewPager(viewPagerHome);
        }

        // Setup UI components
        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar_home);
        toolbar.setTitle("PepRally");
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_home);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.id_nav_view_home);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.id_item_chat:
                Toast.makeText(HomeActivity.this, "See chats", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.id_item_notifications:
                Intent intent = new Intent(this, NotificationsActivity.class);
                intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    @Override
    protected void onResume() {
        super.onResume();
        Helpers.checkGooglePlayServicesAvailable(this);

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

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/

    public void launchBrowsePlayerActivity(String team) {
        Intent intent = new Intent(HomeActivity.this, FavoritePlayerActivity.class);
        intent.putExtra("CALLING_ACTIVITY", "HomeActivity");
        intent.putExtra("CURRENT_USER_NICKNAME", userProfileParcel.getCurUserNickname());
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

        TabLayout tabLayout = (TabLayout) findViewById(R.id.id_tablayout_home);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPagerHome);
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
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchUserProfileParcelTask extends AsyncTask<Profile, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Profile... params) {
            DBUserProfile userProfile = new DBUserProfile();
            DBPlayerProfile playerProfile = new DBPlayerProfile();
            userProfile.setCognitoId(dbHelper.getIdentityID());
            DynamoDBQueryExpression<DBUserProfile> queryExpression = new DynamoDBQueryExpression<DBUserProfile>()
                    .withIndexName("CognitoID-index")
                    .withHashKeyValues(userProfile)
                    .withConsistentRead(false);

            List<DBUserProfile> results = dbHelper.getMapper().query(DBUserProfile.class, queryExpression);
            if (results == null || results.size() == 0) {
                Log.d(TAG, "CognitoID not found: current user does not exist in database.");
            }
            else{
                if (results.size() == 1) {
                    userProfile = results.get(0);
                    if (userProfile.getIsVarsityPlayer()) {
                        playerProfile = dbHelper.loadDBPlayerProfile(userProfile.getTeam(), userProfile.getPlayerIndex());
                    }
                    userProfileParcel = new UserProfileParcel(ActivityEnum.HOME, userProfile, playerProfile);
//                    nickname = userProfile.getNickname();
//                    Profile fbProfile = params[0];
//                    userProfileParcel = new UserProfileParcel(ActivityEnum.HOME,
//                                                              nickname,
//                                                              fbProfile.getFirstName(),
//                                                              fbProfile.getLastName(),
//                                                              nickname,
//                                                              fbProfile.getId(),
//                                                              true);  // user is viewing self profile
                    return true;
                }
                Log.d(TAG, "Query result should have only returned single user!");
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                viewPagerHome = (ViewPager) findViewById(R.id.id_viewpager_home);
                setupViewPager(viewPagerHome);
            }
        }
    }
}
