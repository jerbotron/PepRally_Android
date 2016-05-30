package com.peprally.jeremy.peprally.activities;

import android.content.Intent;
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

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.ProfileViewPagerAdapter;
import com.peprally.jeremy.peprally.fragments.BrowseTeamsFragment;
import com.peprally.jeremy.peprally.fragments.TrendingFragment;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/

    // UI Variables
    private DrawerLayout drawer;
    private ViewPager viewPagerHome;

    // FB Variables
    private Profile fbProfile;

    // General Variables
    private static final String TAG = HomeActivity.class.getSimpleName();
    private String nickname;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        fbProfile = Profile.getCurrentProfile();

        // Set up UI elements first
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_home);
        assert toolbar != null;
        toolbar.setTitle("Pep Rally");
        setSupportActionBar(toolbar);

        viewPagerHome = (ViewPager) findViewById(R.id.viewpager_home);
        setupViewPager(viewPagerHome);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout_home);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPagerHome);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_home);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_home);
        assert navigationView != null;
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
                viewPagerHome.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Initialize other member variables
        nickname = getIntent().getStringExtra("NICKNAME");
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
        UserProfileParcel parcel = new UserProfileParcel(fbProfile.getFirstName(),
                                                         fbProfile.getLastName(),
                                                         nickname,
                                                         fbProfile.getId(),
                                                         true);  // user is viewing self profile

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("USER_PROFILE_PARCEL", parcel);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/

    public void launchBrowsePlayerActivity(String team) {
        Intent intent = new Intent(this, FavoritePlayerActivity.class);
        intent.putExtra("CALLING_ACTIVITY", "HomeActivity");
        intent.putExtra("TEAM", team);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/

    private void setupViewPager(ViewPager viewPager) {
        ProfileViewPagerAdapter adapter = new ProfileViewPagerAdapter(getSupportFragmentManager());
//        adapter.addFrag(new EventsFragment(), "Events");
        adapter.addFrag(new TrendingFragment(), "Trending");
        adapter.addFrag(new BrowseTeamsFragment(), "Teams");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }
}
