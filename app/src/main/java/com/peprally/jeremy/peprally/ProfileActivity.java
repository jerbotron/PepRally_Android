package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;

public class ProfileActivity extends AppCompatActivity {

    private ActionBar supportActionBar;
    private Fragment viewFragment, editFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private MenuItem editMenuItem;

    private Boolean editMode = false;
    private Boolean following = false;
    private Integer numFistBumps = 0;
    private Integer numFollowers = 0;
    private Integer numFollowing = 0;

    private String userFirstName;
    private Integer userAge;

    private String motto, favoriteTeam, favoritePlayer, pepTalk, trashTalk;
    private static final String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        // Facebook related setups
        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        String userId = currentToken.getUserId();
        Profile fb_profile = Profile.getCurrentProfile();
        userFirstName = fb_profile.getFirstName();
        userAge = 23; // REPLACE WITH FB GET FIRST NAME
        ProfilePictureView profilePicture = (ProfilePictureView) findViewById(R.id.profile_image_profile);
        profilePicture.setProfileId(userId);
        Bundle bundleUserInfo = new Bundle();
        bundleUserInfo.putString("firstName", userFirstName);
        bundleUserInfo.putInt("age", userAge);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        viewFragment = new ProfileViewFragment();
        viewFragment.setArguments(bundleUserInfo);
        editFragment = new ProfileEditFragment();
        editFragment.setArguments(bundleUserInfo);
        fragmentTransaction.replace(R.id.profile_fragment_container, viewFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        TextView fistBumpsTextView = (TextView) findViewById(R.id.profile_fist_bumps);
        TextView followersTextView = (TextView) findViewById(R.id.profile_followers);
        TextView followingTextView = (TextView) findViewById(R.id.profile_following);

        fistBumpsTextView.setText(Html.fromHtml("<b>" + Integer.toString(numFistBumps) + "</b> "
                + getString(R.string.profile_fist_bumps)));
        followersTextView.setText(Html.fromHtml("<b>" + Integer.toString(numFollowers) + "</b> "
                + getString(R.string.profile_followers)));
        followingTextView.setText(Html.fromHtml("<b>" + Integer.toString(numFollowing) + "</b> "
                + getString(R.string.profile_following)));

        // Follow Button Action
        final LinearLayout followButton = (LinearLayout) findViewById(R.id.button_follow_wrapper);
        final TextView followButtonContent = (TextView) findViewById(R.id.button_follow_content);
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

        // Fistbump FAB Action
        FloatingActionButton fistbumpFab = (FloatingActionButton) findViewById(R.id.profile_firstbump_fab);
        fistbumpFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "FAB CLICKED");
                Toast.makeText(ProfileActivity.this, "FIST BUMP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                handleEditButton();
                return true;
            case R.id.edit_profile_item:
                if (!editMode) {
                    // Switch Fragment to editFragment
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.profile_fragment_container, editFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
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
    public void onBackPressed() {
        handleEditButton();
    }

    private void handleEditButton() {
        if (editMode) {
            // Switch Fragment back to viewFragment
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.profile_fragment_container, viewFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            // Re-enable Edit Icon
            editMenuItem.setEnabled(true);
            editMenuItem.setVisible(true);
            editMode = false;

            // Change back Actionbar title
            supportActionBar.setTitle(R.string.app_name);

            // Show Toast to let user know changes are saved
            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
        }
        else {
            finish();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.left_in, R.anim.left_out);
        }
    }
}
