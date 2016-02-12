package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.ProfilePictureView;

public class ProfileActivity extends AppCompatActivity {

    private ProfilePictureView profilePicture;
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

        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        String userId = currentToken.getUserId();
        profilePicture = (ProfilePictureView) findViewById(R.id.profile_image_profile);
        profilePicture.setProfileId(userId);

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
        finish();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }

    private void onTestButtonClick(View view) {
//        Intent intent = new Intent(this, EventsFragment.class);
//        startActivity(intent);
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

    public void onFollowButtonClick(View view) {
        setFollowButton();
    }
}
