package com.peprally.jeremy.peprally.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.ProfileViewPagerAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.IntentRequestEnum;
import com.peprally.jeremy.peprally.fragments.BrowseTeamsFragment;
import com.peprally.jeremy.peprally.fragments.TrendingFragment;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.List;

import static com.peprally.jeremy.peprally.utils.Helpers.getAPICompatVectorDrawable;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private DynamoDBHelper dynamoDBHelper;

    // UI Variables
    private DrawerLayout drawer;
    private MenuItem menuChatItem, menuNotificationItem;
    private ProgressDialog progressDialogDeleteProfile;
    private ViewPager viewPagerHome;

    // Fragment Variables
    private TrendingFragment trendingFragment;

    // General Variables
    private static final String TAG = HomeActivity.class.getSimpleName();
    private UserProfileParcel userProfileParcel;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set main activity content view
        setContentView(R.layout.activity_home);

        // Initialize member variables
        dynamoDBHelper = new DynamoDBHelper(this);

        // initialize incoming activity data
        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        userProfileParcel.setCurrentActivity(ActivityEnum.HOME);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (userProfileParcel == null) {
            new FetchUserProfileParcelTask().execute();
        }
        else {
            viewPagerHome = (ViewPager) findViewById(R.id.id_viewpager_home);
            setupViewPager(viewPagerHome);
        }

        // Setup UI components
        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar_home);
        toolbar.setTitle("");
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.id_text_view_toolbar_title);
        Typeface customTf = Typeface.createFromAsset(getAssets(), "fonts/Sketch 3D.otf");
        toolbarTitle.setTypeface(customTf);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_home);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.id_nav_view_home);
        navigationView.setNavigationItemSelectedListener(this);

        // Fetch FB img_default_profile photo and first name and display them in sidebar header
        View headerView = navigationView.getHeaderView(0);
        LinearLayout header = (LinearLayout) headerView.findViewById(R.id.id_sidebar_header);
        ImageView profilePicture = (ImageView) headerView.findViewById(R.id.profile_image_header);
        Helpers.setFacebookProfileImage(HomeActivity.this,
                profilePicture,
                sharedPreferences.getString("CURRENT_FACEBOOK_ID", ""),
                3,
                true);
        TextView sidebar_name = (TextView) headerView.findViewById(R.id.sidebar_header_name);
        sidebar_name.setText(sharedPreferences.getString("CURRENT_FIRSTNAME", ""));
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavBarHeaderClick();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        menuChatItem = menu.findItem(R.id.id_item_chat);
        menuNotificationItem = menu.findItem(R.id.id_item_notifications);
        if (userProfileParcel != null && userProfileParcel.hasNewMessage()) {
            menuChatItem.setIcon(getAPICompatVectorDrawable(HomeActivity.this, R.drawable.ic_chat_bubble_notify));
        }
        if (userProfileParcel != null && userProfileParcel.hasNewNotification()) {
            menuNotificationItem.setIcon(getAPICompatVectorDrawable(HomeActivity.this, R.drawable.ic_notifications_notify));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.id_item_chat:
                intent = new Intent(this, ConversationsActivity.class);
                intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                return true;
            case R.id.id_item_notifications:
                intent = new Intent(this, NotificationsActivity.class);
                intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
            startActivityForResult(intent, IntentRequestEnum.SETTINGS_REQUEST.toInt());
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
        else if (id == R.id.nav_logout) {
            logOutAccount();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onNavBarHeaderClick() {
        // Viewing self profile
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        userProfileParcel.setIsSelfProfile(true);
        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (IntentRequestEnum.fromInt(requestCode)) {
                case NEW_POST_REQUEST:
                    trendingFragment.addPostToAdapter(data.getStringExtra("NEW_POST_TEXT"));
                    trendingFragment.toggleTrendingModeIsHottestView(false);
                    break;
                case SETTINGS_REQUEST:
                    if (data.getBooleanExtra("DELETE_PROFILE", false)) {
                        toggleDeletingPostLoadingDialog(true);
                        dynamoDBHelper.deleteUserAccount(userProfileParcel, new DynamoDBHelper.AsyncTaskCallback() {
                            @Override
                            public void onTaskDone() {
                                toggleDeletingPostLoadingDialog(false);
                                logOutAccount();
                                Toast.makeText(HomeActivity.this, "Account deleted!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Helpers.checkGooglePlayServicesAvailable(this);
        new UpdateUserNotificationAlertsAsyncTask().execute();
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

    /***********************************************************************************************
     *********************************** GENERAL_METHODS ********************************
     **********************************************************************************************/

    private void toggleDeletingPostLoadingDialog(boolean show) {
        if (show)
            progressDialogDeleteProfile = ProgressDialog.show(HomeActivity.this, "Delete Profile", "Deleting ... ", true);
        else
            progressDialogDeleteProfile.dismiss();
    }

    public void logOutAccount() {
        finish();
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void launchBrowsePlayerActivity(String team) {
        Intent intent = new Intent(HomeActivity.this, FavoritePlayerActivity.class);
        intent.putExtra("CALLING_ACTIVITY", "HOME_ACTIVITY");
        intent.putExtra("CURRENT_USERNAME", userProfileParcel.getCurrentUsername());
        intent.putExtra("TEAM", team);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public void launchNewPostActivity() {
        Intent intent = new Intent(HomeActivity.this, NewPostActivity.class);
        startActivityForResult(intent, IntentRequestEnum.NEW_POST_REQUEST.toInt());
        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
    }

    public UserProfileParcel getUserProfileParcel() {
        return userProfileParcel;
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/

    private void setupViewPager(ViewPager viewPager) {
        trendingFragment = new TrendingFragment();
        BrowseTeamsFragment browseTeamsFragment = new BrowseTeamsFragment();
        ProfileViewPagerAdapter adapter = new ProfileViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(trendingFragment, "Trending");
        adapter.addFrag(browseTeamsFragment, "Teams");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.id_tablayout_home);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPagerHome);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        }
    }

    private void updateMenuItemsNotificationAlerts(boolean chatAlert, boolean notificationAlert) {
        if (menuChatItem != null && menuNotificationItem != null) {
            if (chatAlert)
                menuChatItem.setIcon(getAPICompatVectorDrawable(HomeActivity.this, R.drawable.ic_chat_bubble_notify));
            else
                menuChatItem.setIcon(getAPICompatVectorDrawable(HomeActivity.this, R.drawable.ic_chat_bubble));

            if (notificationAlert)
                menuNotificationItem.setIcon(getAPICompatVectorDrawable(HomeActivity.this, R.drawable.ic_notifications_notify));
            else
                menuNotificationItem.setIcon(getAPICompatVectorDrawable(HomeActivity.this, R.drawable.ic_notifications));
        }
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchUserProfileParcelTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            DBUserProfile userProfile = new DBUserProfile();
            DBPlayerProfile playerProfile = new DBPlayerProfile();
            userProfile.setCognitoId(dynamoDBHelper.getIdentityID());
            DynamoDBQueryExpression<DBUserProfile> queryExpression = new DynamoDBQueryExpression<DBUserProfile>()
                    .withIndexName("CognitoId-index")
                    .withHashKeyValues(userProfile)
                    .withConsistentRead(false);

            List<DBUserProfile> results = dynamoDBHelper.getMapper().query(DBUserProfile.class, queryExpression);
            if (results != null && results.size() == 1) {
                userProfile = results.get(0);
                if (userProfile.getIsVarsityPlayer()) {
                    playerProfile = dynamoDBHelper.loadDBPlayerProfile(userProfile.getTeam(), userProfile.getPlayerIndex());
                }
                userProfileParcel = new UserProfileParcel(ActivityEnum.HOME, userProfile, playerProfile);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                viewPagerHome = (ViewPager) findViewById(R.id.id_viewpager_home);
                setupViewPager(viewPagerHome);
            }
        }
    }

    private class UpdateUserNotificationAlertsAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            DBUserProfile userProfile = dynamoDBHelper.loadDBUserProfile(userProfileParcel.getCurrentUsername());
            if (userProfile != null) {
                userProfileParcel.setHasNewMessage(userProfile.getHasNewMessage());
                userProfileParcel.setHasNewNotification(userProfile.getHasNewNotification());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateMenuItemsNotificationAlerts(userProfileParcel.hasNewMessage(),
                                              userProfileParcel.hasNewNotification());
        }
    }
}
