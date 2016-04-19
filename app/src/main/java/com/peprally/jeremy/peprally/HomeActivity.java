package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private ViewPager viewPager;

    private Profile fbProfile;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        fbProfile = Profile.getCurrentProfile();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_home);
        toolbar.setTitle("Pep Rally");
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
//        viewPager = (ViewPager) findViewById(R.id.viewpager_home);
//        if (id == R.id.nav_trending) {
//            viewPager.setCurrentItem(0);
//        } else if (id == R.id.nav_events) {
//            viewPager.setCurrentItem(1);
//        } else if (id == R.id.nav_browse_teams) {
//            viewPager.setCurrentItem(2);
        if (id == R.id.nav_settings) {
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
        finish();
        Intent intent = new Intent(this, ProfileActivity.class);
        Bundle userProfileBundle = new Bundle();
        userProfileBundle.putString("FIRST_NAME", fbProfile.getFirstName());
        userProfileBundle.putString("LAST_NAME", fbProfile.getLastName());
        userProfileBundle.putString("NICKNAME", fbProfile.getFirstName().toLowerCase()
                                                + fbProfile.getLastName().toLowerCase());
        userProfileBundle.putString("FACEBOOK_ID", fbProfile.getId());
        intent.putExtra("USER_PROFILE_BUNDLE", userProfileBundle);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new EventsFragment(), "Events");
        adapter.addFrag(new TrendingFragment(), "Trending");
        adapter.addFrag(new BrowseTeamsFragment(), "Teams");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
    }

    public void launchBrowsePlayerActivity(String team) {
        Intent intent = new Intent(this, FavoritePlayerActivity.class);
        intent.putExtra("CALLING_ACTIVITY", "HomeActivity");
        intent.putExtra("TEAM", team);
        startActivity(intent);
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }
}
