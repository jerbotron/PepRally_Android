package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private ViewPager viewPager;

    private Profile fbProfile;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    private Bundle userProfileBundle;

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        fbProfile = Profile.getCurrentProfile();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),                    // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        new LoadUserProfileFromDBTask().execute(credentialsProvider);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager_home);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout_home);
        tabLayout.setupWithViewPager(viewPager);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_home);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_home);
        navigationView.setNavigationItemSelectedListener(this);

        // Fetch FB profile photo and first name and display them in sidebar header
        View headerView = navigationView.getHeaderView(0);
        LinearLayout header = (LinearLayout) headerView.findViewById(R.id.sidebar_header);
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
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_home);
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
        viewPager = (ViewPager) findViewById(R.id.viewpager_home);
        if (id == R.id.nav_trending) {
            viewPager.setCurrentItem(0);
        } else if (id == R.id.nav_events) {
            viewPager.setCurrentItem(1);
        } else if (id == R.id.nav_browse_teams) {
            viewPager.setCurrentItem(2);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.right_out);
        } else if (id == R.id.nav_logout) {
            finish();
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_home);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onNavBarHeaderClick() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("USER_PROFILE_BUNDLE", userProfileBundle);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new TrendingFragment(), "Trending");
        adapter.addFrag(new EventsFragment(), "Events");
        adapter.addFrag(new BrowseTeamsFragment(), "Teams");
        viewPager.setAdapter(adapter);
    }

    private class LoadUserProfileFromDBTask extends AsyncTask<CognitoCachingCredentialsProvider, Void, Void> {
        private UserProfile userProfile;
        @Override
        protected Void doInBackground(CognitoCachingCredentialsProvider... params) {
            CognitoCachingCredentialsProvider credentialsProvider = params[0];
            userProfile = mapper.load(UserProfile.class, credentialsProvider.getIdentityId(), fbProfile.getFirstName());
            if (userProfile.getNewUser()) {
                SetupNewUserProfile();
            }
            else {
                Log.d(TAG, "default user already created, fetching user data");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            userProfileBundle = BundleUserProfileData();
            Log.d(TAG, "task done");
        }

        private void SetupNewUserProfile() {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            userProfile.setFollowers(0);
            userProfile.setFollowing(0);
            userProfile.setFistbumps(0);
            userProfile.setMotto(null);
            userProfile.setFavoriteTeam(null);
            userProfile.setFavoritePlayer(null);
            userProfile.setPepTalk(null);
            userProfile.setTrashTalk(null);
            userProfile.setDateJoined(df.format(c.getTime()));
            userProfile.setNewUser(false);
            mapper.save(userProfile);
        }

        private Bundle BundleUserProfileData() {
            Bundle userProfileBundle = new Bundle();
            userProfileBundle.putString("FIRST_NAME", userProfile.getFirstName());
            userProfileBundle.putString("LAST_NAME", userProfile.getLastName());
            userProfileBundle.putString("MOTTO", userProfile.getMotto());
            userProfileBundle.putInt("FOLLOWERS", userProfile.getFollowers());
            userProfileBundle.putInt("FOLLOWING", userProfile.getFollowing());
            userProfileBundle.putInt("FISTBUMPS", userProfile.getFistbumps());
            userProfileBundle.putString("FAVORITE_TEAM", userProfile.getFavoriteTeam());
            userProfileBundle.putString("FAVORITE_PLAYER", userProfile.getFavoritePlayer());
            userProfileBundle.putString("PEP_TALK", userProfile.getPepTalk());
            userProfileBundle.putString("TRASH_TALK", userProfile.getTrashTalk());
            return userProfileBundle;
        }
    }
}
