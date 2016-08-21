package com.peprally.jeremy.peprally.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.PlayersCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;


public class FavoritePlayerActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/

    // UI Variables
    private RecyclerView recyclerView;

    // General Variables
//    private static final String TAG = FavoriteTeamActivity.class.getSimpleName();
    private PaginatedQueryList<DBPlayerProfile> roster;
    private String callingActivity;
    private String curUsername;
    private boolean dataFetched = false;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_player);

        callingActivity = getIntent().getStringExtra("CALLING_ACTIVITY");
        curUsername = getIntent().getStringExtra("CURRENT_USERNAME");
        String team = getIntent().getStringExtra("TEAM");
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle(team);
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.rv_browse_players);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
            recyclerView.setAdapter(new EmptyAdapter());
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        }

        new FetchTeamRosterTask().execute(team);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataFetched) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            initializeAdapter(roster);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    public void playerCardOnClickHandler(int position) {
        DBPlayerProfile playerProfile = roster.get(position);
        if (callingActivity.equals("ProfileActivity")) {
            Intent intent = new Intent();
            intent.putExtra("FAVORITE_PLAYER", Helpers.getFavPlayerText(playerProfile.getFirstName(),
                    playerProfile.getLastName(),
                    playerProfile.getNumber(),
                    playerProfile.getTeam()));
            setResult(Activity.RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        } else if (callingActivity.equals("HomeActivity")) {
            UserProfileParcel userProfileParcel = new UserProfileParcel(ActivityEnum.PROFILE,
                    curUsername,
                    playerProfile.getFirstName(),
                    playerProfile.getTeam(),
                    playerProfile.getIndex(),
                    false); // assume user not viewing self profile
            // Check if current user is a varsity player viewing his/her own profile
            if (playerProfile.getHasUserProfile() &&
                    playerProfile.getUsername().equals(curUsername)) {
                userProfileParcel.setIsSelfProfile(true);
            }
            Intent intent = new Intent(FavoritePlayerActivity.this, ProfileActivity.class);
            intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }

    private void initializeAdapter(PaginatedQueryList<DBPlayerProfile> result) {
        this.roster = result;
        PlayersCardAdapter playersCardAdapter = new PlayersCardAdapter(FavoritePlayerActivity.this, roster);
        recyclerView.setAdapter(playersCardAdapter);
    }
    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/

    @SuppressWarnings("unchecked")
    private class FetchTeamRosterTask extends AsyncTask<String, Void, PaginatedQueryList<DBPlayerProfile>> {
        @Override
        protected PaginatedQueryList<DBPlayerProfile> doInBackground(String... teams) {
            String team = teams[0];
            DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(getApplicationContext());

            DBPlayerProfile playerProfile = new DBPlayerProfile();
            playerProfile.setTeam(team);
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(playerProfile)
                    .withConsistentRead(false);

            return dynamoDBHelper.getMapper().query(DBPlayerProfile.class, queryExpression);
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBPlayerProfile> result) {
            dataFetched = true;
            initializeAdapter(result);
        }
    }
}
