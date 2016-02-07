package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.graphics.Color;
import android.media.audiofx.BassBoost;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;

public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ProfilePictureView profilePicture, profilePictureHeader;
    private TextView fistBumpsTextView, followersTextView;
    private Toolbar toolbar;

    private boolean following = false;
    private int numOfFistBumps = 152;
    private int numOfFollowers = 44;

    static boolean profileActivityActive = false;

    private static final String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onStart() {
        super.onStart();
        profileActivityActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        profileActivityActive = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_profile);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view_profile);
        navigationView.setNavigationItemSelectedListener(this);

        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        String userId = currentToken.getUserId();
        profilePicture = (ProfilePictureView) findViewById(R.id.profile_image_profile);
        profilePicture.setProfileId(userId);

        View headerView = navigationView.getHeaderView(0);
        profilePictureHeader = (ProfilePictureView) headerView.findViewById(R.id.profile_image_header);
        profilePictureHeader.setProfileId(userId);

        fistBumpsTextView = (TextView) findViewById(R.id.id_profile_tv_fist_bumps);
        followersTextView = (TextView) findViewById(R.id.id_profile_tv_followers);

        fistBumpsTextView.setTextColor(Color.BLACK);
        followersTextView.setTextColor(Color.BLACK);
        fistBumpsTextView.setText(Html.fromHtml("<b>" + Integer.toString(numOfFistBumps) + "</b> "
                + getString(R.string.profile_fist_bumps)));
        followersTextView.setText(Html.fromHtml("<b>" + Integer.toString(numOfFollowers) + "</b> "
                + getString(R.string.profile_followers)));

        setFollowButton();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onTestButtonClick(View view) {
//        Intent intent = new Intent(this, EventsActivity.class);
//        startActivity(intent);
    }

    public void onNavBarHeaderClick(View view) {
        Log.d(TAG, "----- Header clicked -----");
//        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_profile);
//        drawer.closeDrawer(GravityCompat.START);
    }

    public void onFollowButtonClick(View view) {
        setFollowButton();
    }

    private void setFollowButton() {
        Button followButton = (Button) findViewById(R.id.id_profile_btn_follow);
        if (following) {
            followButton.setText("Following");
            followButton.setBackgroundColor(Color.parseColor("#66BD2B"));
            following = false;
        }
        else {
            followButton.setText(R.string.profile_btn_follow);
            followButton.setBackgroundColor(Color.parseColor("#929292"));
            following = true;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        finish();
        if (id == R.id.nav_trending) {
            Intent intent = new Intent(this, TrendingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_events) {
            Intent intent = new Intent(this, EventsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_browse_teams) {
            Intent intent = new Intent(this, BrowseTeamsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_profile);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
