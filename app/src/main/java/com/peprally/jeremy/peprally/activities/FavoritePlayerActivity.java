package com.peprally.jeremy.peprally.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.PlayersCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;


public class FavoritePlayerActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/

    // UI Variables
    private RecyclerView recyclerView;

    // General Variables
    private PaginatedQueryList<DBPlayerProfile> roster;
    private String callingActivity;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_player);

        callingActivity = getIntent().getStringExtra("CALLING_ACTIVITY");
        final String team = getIntent().getStringExtra("TEAM");
        final ActionBar supportActionBar = getSupportActionBar();
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

        new FetchTeamRosterTask(team).execute();
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
        DBPlayerProfile playerProfile = roster.get(position);
        switch (callingActivity) {
            case "PROFILE_ACTIVITY": {
                Intent intent = new Intent();
                intent.putExtra("FAVORITE_PLAYER", Helpers.getFavPlayerString(playerProfile.getFirstName(),
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
                boolean isSelfProfile = playerProfile.getHasUserProfile() && playerProfile.getUsername().equals(currentUsername);
                AsyncHelpers.launchProfileActivityWithVarsityPlayerInfo(
                        FavoritePlayerActivity.this,
                        currentUsername,
                        playerProfile.getFirstName(),
                        playerProfile.getTeam(),
                        playerProfile.getIndex(),
                        isSelfProfile);
                break;
            }
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
    private class FetchTeamRosterTask extends AsyncTask<Void, Void, PaginatedQueryList<DBPlayerProfile>> {

        private String team;

        private FetchTeamRosterTask(String team) {
            this.team = team;
        }

        @Override
        protected PaginatedQueryList<DBPlayerProfile> doInBackground(Void... params) {
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
            initializeAdapter(result);
        }
    }
}
