package com.peprally.jeremy.peprally.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.ProfileViewPagerAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.fragments.BrowsePlayersFragment;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.Helpers;


public class BrowsePlayersActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private ViewPager viewPagerBrowsePlayers;
    private ProfileViewPagerAdapter viewPagerAdapter;

    // General Variables
    private String callingActivity;
    private String currentTeam;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_player);

        callingActivity = getIntent().getStringExtra("CALLING_ACTIVITY");
        currentTeam = getIntent().getStringExtra("TEAM");
        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(currentTeam);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        viewPagerBrowsePlayers = (ViewPager) findViewById(R.id.id_viewpager_browse_player);
        setupViewPager(viewPagerBrowsePlayers);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (callingActivity.equals("PROFILE_ACTIVITY")) {
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
        }
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***********************************************************************************************
     **************************************** GENERAL_METHODS **************************************
     **********************************************************************************************/
    public void playerCardOnClickHandler(int position) {
        DBPlayerProfile playerProfile = ((BrowsePlayersFragment) viewPagerAdapter.getItem(
                viewPagerBrowsePlayers.getCurrentItem())).getDBPlayerProfile(position);

        switch (callingActivity) {
            case "PROFILE_ACTIVITY": {
                Intent intent = new Intent();
                intent.putExtra("FAVORITE_PLAYER",
                        Helpers.getFavPlayerString(playerProfile.getFirstName(),
                        playerProfile.getLastName(),
                        playerProfile.getNumber(),
                        playerProfile.getTeam()));
                setResult(Activity.RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            }
            case "PROFILE_FRAGMENT":
            case "HOME_ACTIVITY": {
                String currentUsername = getIntent().getStringExtra("CURRENT_USERNAME");
                boolean isSelfProfile = playerProfile.getHasUserProfile() &&
                                        playerProfile.getUsername().equals(currentUsername);
                AsyncHelpers.launchProfileActivityWithVarsityPlayerInfo(
                        BrowsePlayersActivity.this,
                        currentUsername,
                        playerProfile.getFirstName(),
                        playerProfile.getTeam(),
                        playerProfile.getIndex(),
                        isSelfProfile);
                break;
            }
        }
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/

    private void setupViewPager(final ViewPager viewPager) {
        boolean isSingleGenderTeam;

        switch (currentTeam) {
            // Teams with only 1 gender
            case "Baseball":
            case "Football":
            case "Rowing":
            case "Soccer":
            case "Softball":
            case "Volleyball":
                isSingleGenderTeam = true;
                break;
            case "Basketball":
            case "Golf":
            case "Swimming and Diving":
            case "Tennis":
            case "Track and Field":
            default:
                isSingleGenderTeam = false;
                break;
        }

        viewPagerAdapter = new ProfileViewPagerAdapter(getSupportFragmentManager());
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.id_tablayout_browse_player);

        if (isSingleGenderTeam) {
            // remove tabLayout view
            final LinearLayout activityContainer = (LinearLayout) findViewById(R.id.id_activity_browse_player_container);
            activityContainer.removeView(tabLayout);

            final BrowsePlayersFragment rosterFragment = new BrowsePlayersFragment();
            Bundle rosterBundle = new Bundle();
            rosterBundle.putString("TEAM", currentTeam);
            rosterFragment.setArguments(rosterBundle);
            viewPagerAdapter.addFrag(rosterFragment, "Roster");
        } else {
            final BrowsePlayersFragment mensRosterFragment = new BrowsePlayersFragment();
            final BrowsePlayersFragment womensRosterFragment = new BrowsePlayersFragment();
            final Bundle mensRosterBundle = new Bundle();
            mensRosterBundle.putString("TEAM", currentTeam);
            mensRosterBundle.putString("GENDER", "M");
            mensRosterFragment.setArguments(mensRosterBundle);

            final Bundle womensRosterBundle = new Bundle();
            womensRosterBundle.putString("TEAM", currentTeam);
            womensRosterBundle.putString("GENDER", "F");
            womensRosterFragment.setArguments(womensRosterBundle);

            viewPagerAdapter.addFrag(mensRosterFragment, "Men's");
            viewPagerAdapter.addFrag(womensRosterFragment, "Women's");

            if (tabLayout != null) {
                tabLayout.setupWithViewPager(viewPager);
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        }

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(0);
    }
}
