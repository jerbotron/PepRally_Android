package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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
    private ProfilePictureView profilePicture;
    private TextView fistBumpsTextView, followersTextView;
    private boolean following = false;

    private int numOfFistBumps = 152;
    private int numOfFollowers = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        AccessToken currentToken = AccessToken.getCurrentAccessToken();

        profilePicture = (ProfilePictureView) findViewById(R.id.profile_image);
        profilePicture.setProfileId(currentToken.getUserId());

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

    public void onLogoutButtonClick(View view) {
        finish();
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onTestButtonClick(View view) {
        Intent intent = new Intent(this, NavBarActivity.class);
        startActivity(intent);
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

        if (id == R.id.nav_trending) {

        } else if (id == R.id.nav_events) {

        } else if (id == R.id.nav_browse_teams) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {
            finish();
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
