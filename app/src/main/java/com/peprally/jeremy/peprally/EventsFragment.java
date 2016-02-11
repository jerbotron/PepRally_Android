package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;

public class EventsFragment extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ProfilePictureView profilePicture;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_events);
        toolbar = (Toolbar) findViewById(R.id.toolbar_events);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_events);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view_events);
        navigationView.setNavigationItemSelectedListener(this);

        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        View headerView = navigationView.getHeaderView(0);
        LinearLayout header = (LinearLayout) headerView.findViewById(R.id.sidebar_header);
        profilePicture = (ProfilePictureView) headerView.findViewById(R.id.profile_image_header);
        profilePicture.setProfileId(currentToken.getUserId());
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavBarHeaderClick();
            }
        });
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_events);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        finish();
        if (id == R.id.nav_trending) {
            Intent intent = new Intent(this, TrendingFragment.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.right_out);
        } else if (id == R.id.nav_events) {
            Intent intent = new Intent(this, EventsFragment.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.right_out);
        } else if (id == R.id.nav_browse_teams) {
            Intent intent = new Intent(this, BrowseTeamsFragment.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.right_out);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.right_out);
        } else if (id == R.id.nav_logout) {
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_events);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onNavBarHeaderClick() {
        finish();
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}
