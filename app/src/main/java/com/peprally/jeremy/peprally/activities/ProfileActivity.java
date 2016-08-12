package com.peprally.jeremy.peprally.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMappingException;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.ProfileViewPagerAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.IntentRequestEnum;
import com.peprally.jeremy.peprally.fragments.ProfileEditFragment;
import com.peprally.jeremy.peprally.fragments.ProfilePostsFragment;
import com.peprally.jeremy.peprally.fragments.ProfileInfoFragment;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.custom.ProfileViewPager;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS/HTTP Variables
    private DynamoDBHelper dbHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // UI Variables
    private ActionBar supportActionBar;
    private AppBarLayout appBarLayout;
    private ProfileInfoFragment infoFragment;
    private ProfilePostsFragment postsFragment;
    private ProfileEditFragment editFragment;
    private TabLayout tabLayout;
    private ViewPager viewPagerProfile;
    private ProfileViewPagerAdapter adapter;

    // General Variables
    private static UserProfileParcel userProfileParcel;
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private boolean editMode = false;
    private boolean didCurrentUserFistbumpProfileUserAlready = false;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DynamoDBHelper(this);
        httpRequestsHelper = new HTTPRequestsHelper(this);

        userProfileParcel = getIntent().getExtras().getParcelable("USER_PROFILE_PARCEL");

        // 3 Profile Activity cases currently:
        // - view/edit your own profile as a fan
        // - view/edit your own profile as a player
        // - view a varsity player profile
        setContentView(R.layout.activity_profile);
        new FetchUserProfileFromDBTask().execute();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar_profile);
        setSupportActionBar(toolbar);
        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        fixProfileHeaderMarginTop();

        appBarLayout = (AppBarLayout) findViewById(R.id.id_profile_appbar_layout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (!editMode) {
                    if (verticalOffset == 0 && supportActionBar.getTitle() != null) {
                        supportActionBar.setTitle(userProfileParcel.getProfileNickname());
                    }
                    else if(verticalOffset <= -375 && supportActionBar.getTitle() != null) {
                        supportActionBar.setTitle(userProfileParcel.getFirstname());
                    }
                }
            }
        });

        // Follow Button and FAB
        final LinearLayout buttonEditProfile = (LinearLayout) findViewById(R.id.id_button_edit_profile_container);
        final TextView buttonEditProfileContent = (TextView) findViewById(R.id.id_button_edit_profile_content);
        final FloatingActionButton actionFAB = (FloatingActionButton) findViewById(R.id.fab_profile_action);
        if (buttonEditProfile != null && buttonEditProfileContent != null) {
            // If user is viewing their own profile
            if (userProfileParcel.getIsSelfProfile()) {
                buttonEditProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!editMode) {
                            // Switch Fragment to editFragment
                            appBarLayout.setExpanded(false, false);
                            tabLayout.setVisibility(View.GONE);
                            actionFAB.setVisibility(View.INVISIBLE);
                            adapter.addFrag(editFragment, "Edit Profile");
                            adapter.attachFrag(2);
                            adapter.notifyDataSetChanged();
                            viewPagerProfile.setCurrentItem(2);
                            ((ProfileViewPager) viewPagerProfile).setAllowedSwipeDirection(ProfileViewPager.SwipeDirection.none);

                            // Change Actionbar title
                            supportActionBar.setTitle("Edit Profile");

                            editMode = true;
                        }
                    }
                });

                // launch new post activity
                actionFAB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
                        startActivityForResult(intent, IntentRequestEnum.NEW_POST_REQUEST.toInt());
                        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                handleBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (IntentRequestEnum.fromInt(requestCode)) {
                case FAV_TEAM_REQUEST:
                    String favoriteTeam = data.getStringExtra("FAVORITE_TEAM");
                    userProfileParcel.setFavoriteTeam(favoriteTeam);
                    editFragment.setFavTeam(favoriteTeam);
                    break;
                case FAV_PLAYER_REQUEST:
                    String favoritePlayer = data.getStringExtra("FAVORITE_PLAYER");
                    userProfileParcel.setFavoritePlayer(favoritePlayer);
                    editFragment.setFavPlayer(favoritePlayer);
                    break;
                case NEW_POST_REQUEST:
                    postsFragment.addPostToAdapter(data.getStringExtra("NEW_POST_TEXT"));
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    public UserProfileParcel getUserProfileParcel() {
        return userProfileParcel;
    }

    public void editFavoriteTeam() {
        Intent intent = new Intent(this, FavoriteTeamActivity.class);
        startActivityForResult(intent, IntentRequestEnum.FAV_TEAM_REQUEST.toInt());
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public void editFavoritePlayer() {
        String favTeam = editFragment.getFavTeam();
        if (favTeam.isEmpty()) {
            Toast.makeText(ProfileActivity.this, "Pick a favorite team first!", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this, FavoritePlayerActivity.class);
            intent.putExtra("CALLING_ACTIVITY", "ProfileActivity");
            intent.putExtra("TEAM", favTeam);
            startActivityForResult(intent, IntentRequestEnum.FAV_PLAYER_REQUEST.toInt());
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }

    private Bundle makeNotificationDirectFistbumpBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.DIRECT_FISTBUMP.toInt());
        bundle.putString("RECEIVER_NICKNAME", userProfileParcel.getProfileNickname());  // who the notification is going to
        bundle.putString("SENDER_NICKNAME", userProfileParcel.getCurUserNickname());    // who the notification is from
        return bundle;
    }

    private void setDidCurrentUserFistbumpProfileUserAlready(boolean setDidCurrentUserFistbumpProfileUserAlready) {
        this.didCurrentUserFistbumpProfileUserAlready = setDidCurrentUserFistbumpProfileUserAlready;
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void createView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        infoFragment = new ProfileInfoFragment();
        postsFragment = new ProfilePostsFragment();
        editFragment = new ProfileEditFragment();

        viewPagerProfile = (ProfileViewPager) findViewById(R.id.id_viewpager_profile);
        adapter = new ProfileViewPagerAdapter(fragmentManager);
        adapter.addFrag(infoFragment, "Info");
        adapter.addFrag(postsFragment, "Posts");
        viewPagerProfile.setAdapter(adapter);
        viewPagerProfile.setCurrentItem(0);

        tabLayout = (TabLayout) findViewById(R.id.tablayout_profile);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPagerProfile);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPagerProfile.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        final ImageView imageView_profilePicture = (ImageView) findViewById(R.id.id_image_view_profile_image);
        final TextView textView_postsCount = (TextView) findViewById(R.id.id_profile_posts_count);
        final TextView textView_sentFistbumpsCount = (TextView) findViewById(R.id.id_fistbumps_sent);
        final TextView textView_receivedFistbumpsCount = (TextView) findViewById(R.id.id_fistbumps_received);

        if (imageView_profilePicture != null && textView_postsCount != null
                && textView_sentFistbumpsCount != null && textView_receivedFistbumpsCount != null) {
            final String imageURL;
            // Profile Image Setup
            if (userProfileParcel.getIsVarsityPlayer()) {
                String rootImageURL = "https://s3.amazonaws.com/rosterphotos/";
                String team = userProfileParcel.getTeam();
                String extension = team.replace(" ", "+") + "/" + userProfileParcel.getRosterImageURL();
                imageURL = rootImageURL + extension;
                Picasso.with(ProfileActivity.this)
                        .load(imageURL)
                        .placeholder(R.drawable.img_default_ut_placeholder)
                        .into(imageView_profilePicture);
            }
            else {
                imageURL = "https://graph.facebook.com/" + userProfileParcel.getFacebookID() + "/picture?width=9999";
                Helpers.setFacebookProfileImage(this,
                        imageView_profilePicture,
                        userProfileParcel.getFacebookID(),
                        3);
            }

            imageView_profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchProfileImageDialog(imageURL);
                }
            });

            textView_sentFistbumpsCount.setText(Html.fromHtml("<b>"
                    + Integer.toString(userProfileParcel.getSentFistbumpsCount())
                    + "</b> " + getString(R.string.fistbumps_sent)));
            textView_receivedFistbumpsCount.setText(Html.fromHtml("<b>"
                    + Integer.toString(userProfileParcel.getReceivedFistbumpsCount())
                    + "</b> " + getString(R.string.fistbumps_received)));
            textView_postsCount.setText(Html.fromHtml("<b>"
                    + Integer.toString(userProfileParcel.getPostsCount())
                    + "</b> " + getString(R.string.profile_posts)));
        }

        // update user fistbump and edit profile button
        if (!userProfileParcel.getIsSelfProfile())
            updateDirectFistbumpButtons(didCurrentUserFistbumpProfileUserAlready);
    }

    /**
     *  This method is used to set the proper margins on the custom implemented toolbar in profile
     *  layout.
     */
    private void fixProfileHeaderMarginTop() {
        final LinearLayout profileHeaderContainer = (LinearLayout) findViewById(R.id.id_container_profile_header);
        CollapsingToolbarLayout.LayoutParams headerParams = (CollapsingToolbarLayout.LayoutParams) profileHeaderContainer.getLayoutParams();
        TypedValue typedValue = new TypedValue();
        int[] actionbarAttr = new int[] {android.R.attr.actionBarSize};
        TypedArray a = this.obtainStyledAttributes(typedValue.resourceId, actionbarAttr);
        int actionbarSize = a.getDimensionPixelSize(0, -1);
        a.recycle();
        headerParams.setMargins(0, actionbarSize + getStatusBarHeight(), 0, 0);
        profileHeaderContainer.setLayoutParams(headerParams);
    }

    private Integer getStatusBarHeight() {
        Integer result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void handleBackPressed() {
        if (editMode) {
            // Push profile changes to DB
            new PushProfileChangesToDBTask().execute();

            // Switch fragment back to infoFragment
            final FloatingActionButton actionFAB = (FloatingActionButton) findViewById(R.id.fab_profile_action);

            appBarLayout.setExpanded(true, false);
            tabLayout.setVisibility(View.VISIBLE);
            actionFAB.setVisibility(View.VISIBLE);
            adapter.detachFrag(2);
            adapter.removeFrag(2);
            adapter.notifyDataSetChanged();
            editFragment.onPause();
            viewPagerProfile.setCurrentItem(0);
            infoFragment.onResume();
            ((ProfileViewPager) viewPagerProfile).setAllowedSwipeDirection(ProfileViewPager.SwipeDirection.all);

            editMode = false;

            // Change back Actionbar title
            supportActionBar.setTitle(userProfileParcel.getProfileNickname());
        }
        else {
            if (userProfileParcel.getIsSelfProfile()) {
                finish();
                Intent intent = new Intent(this, HomeActivity.class);
                userProfileParcel.setCurrentActivity(ActivityEnum.HOME);
                intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
            else {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        }
    }

    private void launchProfileImageDialog(String imageURL) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_profile_image, null);
        dialogBuilder.setView(dialogView);

        ImageView profileImage = (ImageView) dialogView.findViewById(R.id.id_image_view_dialog_profile_image);

        Picasso.with(this)
                .load(imageURL)
                .placeholder(R.drawable.img_default_profile)
                .error(R.drawable.img_default_profile)
                .into(profileImage);

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showDirectFistbumpSnackbarConfirmation(boolean fistbumpSent) {
        final Snackbar snackbar;
        if (fistbumpSent) {
            snackbar = Snackbar.make(findViewById(R.id.id_activity_profile),
                        getResources().getString(R.string.profile_fistbump_sent) + " " + userProfileParcel.getProfileNickname(),
                        Snackbar.LENGTH_LONG);
        }
        else {
            snackbar = Snackbar.make(findViewById(R.id.id_activity_profile),
                        userProfileParcel.getFirstname() + " has not made a profile yet, fistbump not sent.",
                        Snackbar.LENGTH_LONG);
        }
        snackbar.setAction("OKAY", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }});
        snackbar.show();
    }

    private void launchVerifySendDirectFistbumpDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_verify_direct_fistbump, null);
        dialogBuilder.setView(dialogView);

        final ImageButton fistbumpButton = (ImageButton) dialogView.findViewById(R.id.id_dialog_button_fistbump);

        dialogBuilder.setTitle("Send fistbump to " + userProfileParcel.getFirstname() + " ?");
        final AlertDialog b = dialogBuilder.create();
        fistbumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // increase appropriate fistbump counts
                dbHelper.incrementUserSentFistbumpsCount(userProfileParcel.getCurUserNickname());
                dbHelper.incrementUserReceivedFistbumpsCount(userProfileParcel.getProfileNickname());
                // add users to respective sent/received fistbump lists
                new HandleCurUserDirectFistbumpToProfileUserTask().execute();
                // make push notifications
                dbHelper.makeNewNotification(makeNotificationDirectFistbumpBundle());
                httpRequestsHelper.makePushNotificationRequest(makeNotificationDirectFistbumpBundle());
                // update UI
                final TextView buttonEditProfileContent = (TextView) findViewById(R.id.id_button_edit_profile_content);
                final FloatingActionButton actionFAB = (FloatingActionButton) findViewById(R.id.fab_profile_action);
                buttonEditProfileContent.setText(getResources().getString(R.string.profile_fistbumped_text));
                actionFAB.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fistbump_filled_50));
                actionFAB.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimaryLight));
                // show confirmation snackbar msg
                showDirectFistbumpSnackbarConfirmation(true);
                b.dismiss();
            }
        });
        b.show();
    }

    private void showDirectFistbumpSnackbarFistbumpAlreadySentConfirmation() {
        final Snackbar snackbar = Snackbar.make(findViewById(R.id.id_activity_profile),
                                    getResources().getString(R.string.profile_already_fistbumped) + " " + userProfileParcel.getProfileNickname(),
                                    Snackbar.LENGTH_LONG);
        snackbar.setAction("Okay", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private void updateDirectFistbumpButtons(boolean fistbumped) {
        final LinearLayout buttonEditProfile = (LinearLayout) findViewById(R.id.id_button_edit_profile_container);
        final TextView buttonEditProfileContent = (TextView) findViewById(R.id.id_button_edit_profile_content);
        final FloatingActionButton actionFAB = (FloatingActionButton) findViewById(R.id.fab_profile_action);
        // if current user already fistbumped profile user
        if (fistbumped) {
            buttonEditProfileContent.setText(getResources().getString(R.string.profile_fistbumped_text));
            buttonEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDirectFistbumpSnackbarFistbumpAlreadySentConfirmation();
                }
            });
            actionFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fistbump_filled_50));
            actionFAB.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimaryLight));
            actionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDirectFistbumpSnackbarFistbumpAlreadySentConfirmation();
                }
            });
        }
        // if current user has not fistbumped profile user
        else {
            buttonEditProfileContent.setText(getResources().getString(R.string.profile_send_fistbump_text));
            buttonEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check if profileUser is a varsity player who hasn't created a profile yet
                    if (userProfileParcel.getIsVarsityPlayer() && !userProfileParcel.getHasUserProfile()) {
                        showDirectFistbumpSnackbarConfirmation(false);
                    }
                    else {
                        launchVerifySendDirectFistbumpDialog();
                    }
                }
            });

            // direct fistbump feature
            actionFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fistbump_50_white));
            actionFAB.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorAccent));
            actionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check if profileUser is a varsity player who hasn't created a profile yet
                    if (userProfileParcel.getIsVarsityPlayer() && !userProfileParcel.getHasUserProfile()) {
                        showDirectFistbumpSnackbarConfirmation(false);
                    }
                    else {
                        launchVerifySendDirectFistbumpDialog();
                    }
                }
            });
        }
        // remove edit icon drawable
        buttonEditProfileContent.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
    }

    private void launchNewFistbumpMatchDialog() {
        Helpers.vibrateDeviceNotification(this);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_fistbump_match, null);
        dialogBuilder.setView(dialogView);

        final ImageView leftUserProfileImage = (ImageView) dialogView.findViewById(R.id.id_dialog_fistbumped_user_profile_left);
        final ImageView rightUserProfileImage = (ImageView) dialogView.findViewById(R.id.id_dialog_fistbumped_user_profile_right);
        final LinearLayout sendMessageButton = (LinearLayout) dialogView.findViewById(R.id.id_dialog_fistbumped_button_send_message);
        final LinearLayout goBackButton = (LinearLayout) dialogView.findViewById(R.id.id_dialog_fistbumped_button_back_to_profile);

        new SetFistbumpMatchedUsersProfileImagesAsyncTask().execute(leftUserProfileImage, rightUserProfileImage);

        dialogBuilder.setTitle("It's a Fistbump!");
        dialogBuilder.setMessage("You and " + userProfileParcel.getFirstname() + " have fistbumped each other.");
        final AlertDialog b = dialogBuilder.create();

        // button on click handlers
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.dismiss();
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.dismiss();
            }
        });

        b.show();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class SetFistbumpMatchedUsersProfileImagesAsyncTask extends AsyncTask<ImageView, Void, String> {
        ImageView leftUserProfileImage, rightUserProfileImage;
        @Override
        protected String doInBackground(ImageView... params) {
            DBUserProfile curUserProfile = dbHelper.loadDBUserProfile(userProfileParcel.getCurUserNickname());
            if (curUserProfile != null) {
                leftUserProfileImage = params[0];
                rightUserProfileImage = params[1];
                return curUserProfile.getFacebookId();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String curUserFacebookID) {
            if (curUserFacebookID != null) {
                Helpers.setFacebookProfileImage(getApplicationContext(), leftUserProfileImage, curUserFacebookID, 3);
                Helpers.setFacebookProfileImage(getApplicationContext(), rightUserProfileImage, userProfileParcel.getFacebookID(), 3);
            }
        }
    }

    private class FetchUserProfileFromDBTask extends AsyncTask<Void, Void, Void> {
        private DBUserProfile userProfile;
        private DBPlayerProfile playerProfile;
        @Override
        protected Void doInBackground(Void... params) {
            // 3 Cases:
            // 1) Load general user's profile
            // 2) Load varsity player's profile
            // 3) Load varsity player's profile who also has a general profile
            try {
                userProfile = dbHelper.loadDBUserProfile(userProfileParcel.getProfileNickname());
            }
            catch (DynamoDBMappingException e) {
                userProfile = null;
            }

            String playerTeam;
            int playerIndex;
            if (userProfile != null && userProfileParcel.getTeam() == null && userProfileParcel.getIndex().equals(Helpers.INTEGER_INVALID))
            {
                playerTeam = userProfile.getTeam();
                playerIndex = userProfile.getPlayerIndex();
            }
            else {
                playerTeam = userProfileParcel.getTeam();
                playerIndex = userProfileParcel.getIndex();
            }

            try {
                playerProfile = dbHelper.loadDBPlayerProfile(playerTeam, playerIndex);
            }
            catch (DynamoDBMappingException e) {
                playerProfile = null;
            }

            if (userProfile != null) {
                if (userProfile.getNewUser())
                    SetupNewUserProfile();
                if (!userProfileParcel.getIsSelfProfile()) {
                    // check if current user has fistbumped profile user
                    DBUserProfile curUser = dbHelper.loadDBUserProfile(userProfileParcel.getCurUserNickname());
                    if (curUser.getUsersDirectFistbumpSent().contains(userProfileParcel.getProfileNickname())) {
                        setDidCurrentUserFistbumpProfileUserAlready(true);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            UpdateUserProfileParcel();
            createView();
        }

        private void SetupNewUserProfile() {
            // Set default user profile values
            userProfile.setFollowersCount(0);
            userProfile.setFollowingCount(0);
            userProfile.setSentFistbumpsCount(0);
            userProfile.setReceivedFistbumpsCount(0);
            userProfile.setPostsCount(0);
            userProfile.setFavoriteTeam(null);
            userProfile.setFavoritePlayer(null);
            userProfile.setPepTalk(null);
            userProfile.setTrashTalk(null);
            userProfile.setNewUser(false);
            if (playerProfile != null) {
                userProfile.setIsVarsityPlayer(true);
                userProfile.setTeam(playerProfile.getTeam());
                userProfile.setPlayerIndex(playerProfile.getIndex());
                dbHelper.saveDBObject(playerProfile);
            }
            else {
                userProfile.setIsVarsityPlayer(false);
                userProfile.setPlayerIndex(Helpers.INTEGER_INVALID);
            }
            dbHelper.saveDBObject(userProfile);
        }

        private void UpdateUserProfileParcel() {
            if (userProfile != null) {
                userProfileParcel.setFirstname(userProfile.getFirstName());
                userProfileParcel.setLastname(userProfile.getLastName());
                userProfileParcel.setProfileNickname(userProfile.getNickname());
                userProfileParcel.setFollowersCount(userProfile.getFollowersCount());
                userProfileParcel.setFollowingCount(userProfile.getFollowingCount());
                userProfileParcel.setSentFistbumpsCount(userProfile.getSentFistbumpsCount());
                userProfileParcel.setReceivedFistbumpsCount(userProfile.getReceivedFistbumpsCount());
                userProfileParcel.setPostsCount(userProfile.getPostsCount());
                userProfileParcel.setFavoriteTeam(userProfile.getFavoriteTeam());
                userProfileParcel.setFavoritePlayer(userProfile.getFavoritePlayer());
                userProfileParcel.setPepTalk(userProfile.getPepTalk());
                userProfileParcel.setTrashTalk(userProfile.getTrashTalk());
                userProfileParcel.setIsVarsityPlayer(userProfile.getIsVarsityPlayer());
            }
            if (playerProfile != null) {
                userProfileParcel.setIsVarsityPlayer(true);
                userProfileParcel.setFirstname(playerProfile.getFirstName());
                userProfileParcel.setLastname(playerProfile.getLastName());
                userProfileParcel.setTeam(playerProfile.getTeam());
                userProfileParcel.setNumber(playerProfile.getNumber());
                userProfileParcel.setYear(playerProfile.getYear());
                userProfileParcel.setHeight(playerProfile.getHeight());
                userProfileParcel.setWeight(playerProfile.getWeight());
                userProfileParcel.setPosition(playerProfile.getPosition());
                userProfileParcel.setHometown(playerProfile.getHometown());
                userProfileParcel.setRosterImageURL(playerProfile.getImageURL());
                userProfileParcel.setHasUserProfile(playerProfile.getHasUserProfile());
            }
        }
    }

    private class PushProfileChangesToDBTask extends AsyncTask<Void, Void, Void> {
        private DBUserProfile DBUserProfile;
        @Override
        protected Void doInBackground(Void... params) {
            DBUserProfile = dbHelper.loadDBUserProfile(userProfileParcel.getProfileNickname());
            pushUserProfileChanges();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
        }

        private void pushUserProfileChanges() {
            DBUserProfile.setFavoriteTeam(userProfileParcel.getFavoriteTeam());
            DBUserProfile.setFavoritePlayer(userProfileParcel.getFavoritePlayer());
            DBUserProfile.setPepTalk(userProfileParcel.getPepTalk());
            DBUserProfile.setTrashTalk(userProfileParcel.getTrashTalk());
            dbHelper.saveDBObject(DBUserProfile);
        }
    }

    private class HandleCurUserDirectFistbumpToProfileUserTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            DBUserProfile profileUser = dbHelper.loadDBUserProfile(userProfileParcel.getProfileNickname());
            DBUserProfile curUser = dbHelper.loadDBUserProfile(userProfileParcel.getCurUserNickname());
            if (profileUser != null && curUser != null) {
                curUser.addUsersDirectFistbumpSent(userProfileParcel.getProfileNickname());
                profileUser.addUsersDirectFistbumpReceived(userProfileParcel.getCurUserNickname());
                dbHelper.saveDBObject(curUser);
                dbHelper.saveDBObject(profileUser);
                // if the profile user also sent a direct fistbump to current user
                if (profileUser.getUsersDirectFistbumpSent().contains(userProfileParcel.getCurUserNickname())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean launchChatWindow) {
            if (launchChatWindow) {
                dbHelper.createNewConversation(userProfileParcel.getCurUserNickname(), userProfileParcel.getProfileNickname());
                launchNewFistbumpMatchDialog();
            }
        }
    }
}