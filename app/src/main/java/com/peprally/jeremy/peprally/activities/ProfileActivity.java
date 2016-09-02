package com.peprally.jeremy.peprally.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
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
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.ProfileViewPagerAdapter;
import com.peprally.jeremy.peprally.custom.ProfileActivityStackSingleton;
import com.peprally.jeremy.peprally.custom.messaging.Conversation;
import com.peprally.jeremy.peprally.custom.ui.CircleImageTransformation;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.IntentRequestEnum;
import com.peprally.jeremy.peprally.fragments.ProfileEditFragment;
import com.peprally.jeremy.peprally.fragments.ProfilePostsFragment;
import com.peprally.jeremy.peprally.fragments.ProfileInfoFragment;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.interfaces.PostContainerInterface;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.utils.Constants;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.custom.ui.ProfileViewPager;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;
import com.squareup.picasso.Picasso;

import static com.peprally.jeremy.peprally.utils.Constants.INTEGER_INVALID;

public class ProfileActivity extends AppCompatActivity implements PostContainerInterface {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS/HTTP Variables
    private DynamoDBHelper dynamoDBHelper;
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
    private boolean profileEditMode;
    private boolean didCurrentUserFistbumpProfileUserAlready = false;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dynamoDBHelper = new DynamoDBHelper(this);
        httpRequestsHelper = new HTTPRequestsHelper(this);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        ProfileActivityStackSingleton.getInstance().push(userProfileParcel);
        userProfileParcel.setCurrentActivity(ActivityEnum.PROFILE);
        profileEditMode = false;

        // 3 Profile Activity cases currently:
        // - view/edit your own profile as a fan
        // - view/edit your own profile as a player
        // - view a varsity player profile
        setContentView(R.layout.activity_profile);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar_profile);
        setSupportActionBar(toolbar);
        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        fixProfileHeaderMarginTop();

//        appBarLayout = (AppBarLayout) findViewById(R.id.id_profile_appbar_layout);
//        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                if (!profileEditMode) {
//                    if (verticalOffset == 0 && supportActionBar.getTitle() != null) {
//                        supportActionBar.setTitle(userProfileParcel.getProfileUsername());
//                    }
//                    else if(verticalOffset <= -375 && supportActionBar.getTitle() != null) {
//                        supportActionBar.setTitle(userProfileParcel.getFirstname());
//                    }
//                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!profileEditMode) {
            userProfileParcel = ProfileActivityStackSingleton.getInstance().peek();
            new FetchUserProfileFromDBTask().execute();
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
                    viewPagerProfile.setCurrentItem(1);
                    postsFragment.addPostToAdapter(data.getStringExtra("NEW_POST_TEXT"));
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

    /***********************************************************************************************
     **************************************** GENERAL_METHODS **************************************
     **********************************************************************************************/
    public UserProfileParcel getUserProfileParcel() {
        return userProfileParcel;
    }

    public void launchFavoriteTeamActivity() {
        Intent intent = new Intent(ProfileActivity.this, FavoriteTeamActivity.class);
        startActivityForResult(intent, IntentRequestEnum.FAV_TEAM_REQUEST.toInt());
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public void launchFavoritePlayerActivity() {
        String favTeam = editFragment.getFavTeam();
        if (favTeam.isEmpty()) {
            Toast.makeText(ProfileActivity.this, "Pick a favorite team first!", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(ProfileActivity.this, FavoritePlayerActivity.class);
            intent.putExtra("CALLING_ACTIVITY", "PROFILE_ACTIVITY");
            intent.putExtra("TEAM", favTeam);
            startActivityForResult(intent, IntentRequestEnum.FAV_PLAYER_REQUEST.toInt());
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }

    public void refreshPosts() {
        viewPagerProfile.setCurrentItem(1);
        postsFragment.refreshAdapter();
    }

    private Bundle makeDBNotificationBundleDirectFistbump() {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.DIRECT_FISTBUMP.toInt());
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putString("RECEIVER_USERNAME", userProfileParcel.getProfileUsername());
        return bundle;
    }

    private Bundle makePushNotificationDirectFistbumpBundle(NotificationEnum notificationType) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", notificationType.toInt());
        bundle.putString("SENDER_USERNAME", userProfileParcel.getCurrentUsername());
        bundle.putString("RECEIVER_USERNAME", userProfileParcel.getProfileUsername());
        return bundle;
    }

    private void setDidCurrentUserFistbumpProfileUserAlready(boolean setDidCurrentUserFistbumpProfileUserAlready) {
        this.didCurrentUserFistbumpProfileUserAlready = setDidCurrentUserFistbumpProfileUserAlready;
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void createView() {
        supportActionBar.setTitle(userProfileParcel.getFirstname());

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

        final ImageView imageViewProfilePicture = (ImageView) findViewById(R.id.id_image_view_profile_image);
        final TextView textViewPostsCount = (TextView) findViewById(R.id.id_profile_posts_count);
        final TextView textViewSentFistbumpsCount = (TextView) findViewById(R.id.id_fistbumps_sent);
        final TextView textViewReceivedFistbumpsCount = (TextView) findViewById(R.id.id_fistbumps_received);

        if (imageViewProfilePicture != null && textViewPostsCount != null
                && textViewSentFistbumpsCount != null && textViewReceivedFistbumpsCount != null) {
            final String imageURL;
            // Profile Image Setup
            if (userProfileParcel.isVarsityPlayer()) {
                String team = userProfileParcel.getTeam();
                String extension = team.replace(" ", "+") + "/" + userProfileParcel.getRosterImageURL();
                imageURL = Constants.S3_ROSTER_PHOTOS_2016_URL + extension;
                Picasso.with(ProfileActivity.this)
                        .load(imageURL)
                        .placeholder(R.drawable.img_default_ut_placeholder)
                        .transform(new CircleImageTransformation())
                        .into(imageViewProfilePicture);
            }
            else {
                //"https://graph.facebook.com/" + userProfileParcel.getFacebookID() + "/picture?width=9999";
                imageURL = Helpers.getFacebookProfilePictureURL(userProfileParcel.getFacebookID(), 5);
                Helpers.setFacebookProfileImage(this,
                        imageViewProfilePicture,
                        userProfileParcel.getFacebookID(),
                        3,
                        true);
            }

            imageViewProfilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchProfileImageDialog(imageURL);
                }
            });

            // set text view texts
            textViewSentFistbumpsCount.setText(Helpers.getAPICompatHtml("<b>"
                    + Integer.toString(userProfileParcel.getSentFistbumpsCount())
                    + "</b> " + getString(R.string.fistbumps_sent)));
            textViewReceivedFistbumpsCount.setText(Helpers.getAPICompatHtml("<b>"
                    + Integer.toString(userProfileParcel.getReceivedFistbumpsCount())
                    + "</b> " + getString(R.string.fistbumps_received)));
            textViewPostsCount.setText(Helpers.getAPICompatHtml("<b>"
                    + Integer.toString(userProfileParcel.getPostsCount())
                    + "</b> " + getString(R.string.profile_posts)));

            // set text view drawables safely to avoid vector drawable compatibility issues
            textViewPostsCount.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, Helpers.getAPICompatVectorDrawable(getApplicationContext(), R.drawable.ic_new_post), null);
        }

        // update user fistbump and edit profile button
        updateProfileButtonAndFABBehavior(userProfileParcel.isSelfProfile(), didCurrentUserFistbumpProfileUserAlready);
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
        if (profileEditMode) {
            profileEditMode = false;
            // Push profile changes to DB
            new PushProfileChangesToDBTask().execute();

            // Switch fragment back to infoFragment
            final FloatingActionButton actionFAB = (FloatingActionButton) findViewById(R.id.fab_profile_action);

//            appBarLayout.setExpanded(true, false);
            tabLayout.setVisibility(View.VISIBLE);
            actionFAB.setVisibility(View.VISIBLE);
            adapter.detachFrag(2);
            adapter.removeFrag(2);
            adapter.notifyDataSetChanged();
            editFragment.onPause();
            viewPagerProfile.setCurrentItem(0);
            infoFragment.onResume();
            ((ProfileViewPager) viewPagerProfile).setAllowedSwipeDirection(ProfileViewPager.SwipeDirection.all);

            // Change back Actionbar title
            supportActionBar.setTitle(userProfileParcel.getProfileUsername());

            // change button UI content
            final TextView buttonEditProfileContent = (TextView) findViewById(R.id.id_button_edit_profile_content);
            buttonEditProfileContent.setText(getString(R.string.placeholder_edit_profile));
            buttonEditProfileContent.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, Helpers.getAPICompatVectorDrawable(getApplicationContext(), R.drawable.ic_edit), null);
        }
        else {
            if (userProfileParcel.isSelfProfile()) {
                finish();
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
            else {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
            // pop current profile's userProfileParcel from stack
            ProfileActivityStackSingleton.getInstance().pop();
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
                        getResources().getString(R.string.profile_fistbump_sent) + " " + userProfileParcel.getProfileUsername() + "!",
                        Snackbar.LENGTH_LONG);
        }
        else {
            snackbar = Snackbar.make(findViewById(R.id.id_activity_profile),
                        userProfileParcel.getFirstname() + " has not made a profile yet :(",
                        Snackbar.LENGTH_LONG);
        }
        snackbar.setAction("OKAY", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }});
        snackbar.show();
    }

    private void launchConfirmSendDirectFistbumpDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_confirm_direct_fistbump, null);
        dialogBuilder.setView(dialogView);

        final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.id_dialog_confirm_send_fistbump_title);
        final ImageButton fistbumpButton = (ImageButton) dialogView.findViewById(R.id.id_dialog_confirm_send_fistbump_button_fistbump);

        Typeface customTf = Typeface.createFromAsset(getAssets(), "fonts/Sketch 3D.otf");
        dialogTitle.setTypeface(customTf);
        dialogTitle.setText("Send fistbump to " + userProfileParcel.getFirstname() + " ?");
        final AlertDialog b = dialogBuilder.create();
        fistbumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add users to respective sent/received fistbump lists and increment fistbump counts
                new SendDirectFistbumpToProfileUserAsyncTask().execute();
                // update UI
                final TextView buttonEditProfileContent = (TextView) findViewById(R.id.id_button_edit_profile_content);
                final FloatingActionButton actionFAB = (FloatingActionButton) findViewById(R.id.fab_profile_action);
                buttonEditProfileContent.setText(getResources().getString(R.string.profile_fistbumped_text));
                actionFAB.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fistbump_filled_50));
                actionFAB.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimaryLight));
                // show confirmation snackbar msg
                showDirectFistbumpSnackbarConfirmation(true);
                // update new UI button behaviors
                setDidCurrentUserFistbumpProfileUserAlready(true);
                updateProfileButtonAndFABBehavior(false, true);
                b.dismiss();
            }
        });
        b.show();
    }

    private void showDirectFistbumpSnackbarFistbumpAlreadySentConfirmation() {
        final Snackbar snackbar = Snackbar.make(findViewById(R.id.id_activity_profile),
                                    getResources().getString(R.string.profile_already_fistbumped) + " " + userProfileParcel.getProfileUsername(),
                                    Snackbar.LENGTH_LONG);
        snackbar.setAction("Okay", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private void updateProfileButtonAndFABBehavior(boolean isSelfProfile, boolean alreadyFistbumped) {
        final LinearLayout buttonEditProfile = (LinearLayout) findViewById(R.id.id_button_edit_profile_container);
        final TextView buttonEditProfileContent = (TextView) findViewById(R.id.id_button_edit_profile_content);
        final FloatingActionButton actionFAB = (FloatingActionButton) findViewById(R.id.fab_profile_action);

        // is user is looking at his/her own profile
        if (isSelfProfile) {
            // set vector drawable safely to avoid compatibility issues
            buttonEditProfileContent.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, Helpers.getAPICompatVectorDrawable(getApplicationContext(), R.drawable.ic_edit), null);

            buttonEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!profileEditMode) {
                        profileEditMode = true;
                        // Switch Fragment to editFragment
//                            appBarLayout.setExpanded(false, false);
                        tabLayout.setVisibility(View.GONE);
                        actionFAB.setVisibility(View.INVISIBLE);
                        adapter.addFrag(editFragment, "Edit Profile");
                        adapter.attachFrag(2);
                        adapter.notifyDataSetChanged();
                        viewPagerProfile.setCurrentItem(2);
                        ((ProfileViewPager) viewPagerProfile).setAllowedSwipeDirection(ProfileViewPager.SwipeDirection.none);

                        // Change Actionbar title
                        supportActionBar.setTitle("Edit Profile");

                        // change button UI content
                        buttonEditProfileContent.setText(getString(R.string.placeholder_editing_profile));
                        buttonEditProfileContent.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, null, null);
                    }
                }
            });

            // launch new post activity
            actionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, NewPostActivity.class);
                    startActivityForResult(intent, IntentRequestEnum.NEW_POST_REQUEST.toInt());
                    overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
                }
            });
        }
        else {
            // if current user already fistbumped profile user
            if (alreadyFistbumped) {
                buttonEditProfileContent.setText(getResources().getString(R.string.profile_fistbumped_text));
                buttonEditProfileContent.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.colorWhite));
                buttonEditProfile.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.button_default_clicked));
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
                buttonEditProfileContent.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.colorPrimary));
                buttonEditProfile.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.button_default));
                buttonEditProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // check if profileUser is a varsity player who hasn't created a profile yet
                        if (userProfileParcel.isVarsityPlayer() && !userProfileParcel.getHasUserProfile()) {
                            showDirectFistbumpSnackbarConfirmation(false);
                        }
                        else {
                            launchConfirmSendDirectFistbumpDialog();
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
                        if (userProfileParcel.isVarsityPlayer() && !userProfileParcel.getHasUserProfile()) {
                            showDirectFistbumpSnackbarConfirmation(false);
                        }
                        else {
                            launchConfirmSendDirectFistbumpDialog();
                        }
                    }
                });
            }
            // remove edit icon drawable
            buttonEditProfileContent.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        }
    }

    private void launchFistbumpMatchDialog(final Conversation conversation, final String currentUserFacebookId) {
        Helpers.vibrateDeviceNotification(this);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_fistbump_match, null);
        dialogBuilder.setView(dialogView);

        final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.id_dialog_fistbumped_title);
        final TextView dialogMessage = (TextView) dialogView.findViewById(R.id.id_dialog_fistbumped_message);
        final ImageView leftUserProfileImage = (ImageView) dialogView.findViewById(R.id.id_dialog_fistbumped_user_profile_left);
        final ImageView rightUserProfileImage = (ImageView) dialogView.findViewById(R.id.id_dialog_fistbumped_user_profile_right);
        final LinearLayout sendMessageButton = (LinearLayout) dialogView.findViewById(R.id.id_dialog_fistbumped_button_send_message);
        final LinearLayout goBackButton = (LinearLayout) dialogView.findViewById(R.id.id_dialog_fistbumped_button_back_to_profile);

        new SetFistbumpMatchedUsersProfileImagesAsyncTask().execute(leftUserProfileImage, rightUserProfileImage);

        Typeface customTf = Typeface.createFromAsset(getAssets(), "fonts/Sketch 3D.otf");
        dialogTitle.setTypeface(customTf);
        dialogTitle.setText(getResources().getString(R.string.placeholder_fistbump_match_title));
        dialogMessage.setText("You and " + userProfileParcel.getFirstname() + " have fistbumped each other.");

        final AlertDialog b = dialogBuilder.create();
        // button on click handlers
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.dismiss();
                finish();
                Intent intent = new Intent(ProfileActivity.this, MessagingActivity.class);
                intent.putExtra("CONVERSATION", conversation);
                intent.putExtra("CURRENT_USERNAME", userProfileParcel.getCurrentUsername());
                intent.putExtra("CURRENT_USER_FACEBOOK_ID", currentUserFacebookId);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
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
            DBUserProfile curUserProfile = dynamoDBHelper.loadDBUserProfile(userProfileParcel.getCurrentUsername());
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
                Helpers.setFacebookProfileImage(ProfileActivity.this, leftUserProfileImage, curUserFacebookID, 3, true);
                Helpers.setFacebookProfileImage(ProfileActivity.this, rightUserProfileImage, userProfileParcel.getFacebookID(), 3, true);
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
            userProfile = dynamoDBHelper.loadDBUserProfile(userProfileParcel.getProfileUsername());

            if (userProfile == null) {
                playerProfile = dynamoDBHelper.loadDBPlayerProfile(userProfileParcel.getTeam(), userProfileParcel.getPlayerIndex());
            } else {
                if (userProfile.getNewUser())
                    SetupNewUserProfile();
                if (userProfile.getIsVarsityPlayer())
                    playerProfile = dynamoDBHelper.loadDBPlayerProfile(userProfile.getTeam(), userProfile.getPlayerIndex());
                if (!userProfileParcel.isSelfProfile()) {
                    // check if current user has fistbumped profile user to correctly set UI components
                    DBUserProfile curUser = dynamoDBHelper.loadDBUserProfile(userProfileParcel.getCurrentUsername());
                    if (curUser.getUsersDirectFistbumpSent().contains(userProfileParcel.getProfileUsername())) {
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
                dynamoDBHelper.saveDBObject(playerProfile);
            }
            else {
                userProfile.setIsVarsityPlayer(false);
                userProfile.setPlayerIndex(INTEGER_INVALID);
            }
            dynamoDBHelper.saveDBObject(userProfile);
        }

        private void UpdateUserProfileParcel() {
            if (userProfile != null) {
                userProfileParcel.setFirstname(userProfile.getFirstname());
                userProfileParcel.setLastname(userProfile.getLastname());
                userProfileParcel.setProfileUsername(userProfile.getUsername());
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

        @SuppressWarnings("unchecked")
        private DBPlayerProfile queryDBPlayerProfileWithUsername(String username) {
            DBPlayerProfile playerProfile = new DBPlayerProfile();
            playerProfile.setUsername(username);
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBPlayerProfile>()
                    .withIndexName("Username-index")
                    .withHashKeyValues(playerProfile)
                    .withConsistentRead(false);
            PaginatedQueryList<DBPlayerProfile> queryResults = dynamoDBHelper.getMapper().query(DBPlayerProfile.class, queryExpression);
            // make sure we only get 1 result back
            if (queryResults != null && queryResults.size() == 1) {
                return queryResults.get(0);
            }
            return null;
        }
    }

    private class PushProfileChangesToDBTask extends AsyncTask<Void, Void, Void> {
        private DBUserProfile DBUserProfile;
        @Override
        protected Void doInBackground(Void... params) {
            DBUserProfile = dynamoDBHelper.loadDBUserProfile(userProfileParcel.getProfileUsername());
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
            dynamoDBHelper.saveDBObject(DBUserProfile);
        }
    }

    private class SendDirectFistbumpToProfileUserAsyncTask extends AsyncTask<Void, Void, Void> {
        DBUserProfile profileUser;
        DBUserProfile currentUser;
        @Override
        protected Void doInBackground(Void... voids) {
            boolean isFistbumpMatch = false;
            profileUser = dynamoDBHelper.loadDBUserProfile(userProfileParcel.getProfileUsername());
            currentUser = dynamoDBHelper.loadDBUserProfile(userProfileParcel.getCurrentUsername());
            if (profileUser != null && currentUser != null) {
                // increment fistbump counts to sender and receiver respectively
                dynamoDBHelper.updateFistbumpsCount(currentUser, profileUser, true);
                currentUser.addUsersDirectFistbumpSent(userProfileParcel.getProfileUsername());
                profileUser.addUsersDirectFistbumpReceived(userProfileParcel.getCurrentUsername());
                // check if fistbump match
                if (profileUser.getUsersDirectFistbumpSent().contains(userProfileParcel.getCurrentUsername())) {
                    // create new conversation if it is a match
                    dynamoDBHelper.createNewConversation(userProfileParcel, new DynamoDBHelper.AsyncTaskCallbackWithReturnObject() {
                        @Override
                        public void onTaskDone(Object object) {
                            launchFistbumpMatchDialog((Conversation) object, currentUser.getFacebookId());
                        }
                    });

                    currentUser.setHasNewMessage(true);
                    profileUser.setHasNewMessage(true);
                    isFistbumpMatch = true;
                }
                dynamoDBHelper.saveDBObject(currentUser);
                dynamoDBHelper.saveDBObject(profileUser);
            }

            final NotificationEnum notificationType = isFistbumpMatch ? NotificationEnum.DIRECT_FISTBUMP_MATCH : NotificationEnum.DIRECT_FISTBUMP;
            // create new user notification
            dynamoDBHelper.createNewNotification(makeDBNotificationBundleDirectFistbump(),
                    new DynamoDBHelper.AsyncTaskCallbackWithReturnObject() {
                        @Override
                        public void onTaskDone(Object isNotificationCreated) {
                            if ((boolean) isNotificationCreated) {
                                // send push notification to receiver that they got a direct fistbump/direct fistbump match
                                httpRequestsHelper.makePushNotificationRequest(makePushNotificationDirectFistbumpBundle(notificationType));
                            }
                        }
                    });
            return null;
        }
    }
}