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
import android.view.View;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.PlayersCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;


public class FavoritePlayerActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/

    // UI Variables
    private RecyclerView recyclerView;

    // General Variables
    private static final String TAG = FavoriteTeamActivity.class.getSimpleName();
    private PaginatedQueryList<DBPlayerProfile> roster;
    private String callingActivity;
    private boolean dataFetched = false;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_player);

        callingActivity = getIntent().getStringExtra("CALLING_ACTIVITY");
        String team = getIntent().getStringExtra("TEAM");
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle(team);
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.rv_browse_players);
        assert (recyclerView != null);
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

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
    private void initializeAdapter(PaginatedQueryList<DBPlayerProfile> result) {
        this.roster = result;
        PlayersCardAdapter playersCardAdapter = new PlayersCardAdapter(FavoritePlayerActivity.this, roster);
        recyclerView.setAdapter(playersCardAdapter);
        playersCardAdapter.setOnItemClickListener(new PlayersCardAdapter.PlayersAdapterClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                DBPlayerProfile playerProfile = roster.get(position);
                if (callingActivity.equals("ProfileActivity")) {
                    Intent intent = new Intent();
                    intent.putExtra("FAVORITE_PLAYER", playerProfile.getFavPlayerText());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                } else if (callingActivity.equals("HomeActivity")) {
                    UserProfileParcel parcel = new UserProfileParcel(playerProfile.getFirstName(),
                                                                     playerProfile.getTeam(),
                                                                     playerProfile.getIndex(),
                                                                     false); // user not viewing self profile
                    Intent intent = new Intent(FavoritePlayerActivity.this, ProfileActivity.class);
                    intent.putExtra("USER_PROFILE_PARCEL", parcel);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
//                    finish();
                }
            }
        });
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/

    private class FetchTeamRosterTask extends AsyncTask<String, Void, PaginatedQueryList<DBPlayerProfile>> {
        @Override
        protected PaginatedQueryList<DBPlayerProfile> doInBackground(String... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),                  // Context
                    AWSCredentialProvider.IDENTITY_POOL_ID,   // Identity Pool ID
                    AWSCredentialProvider.COGNITO_REGION      // Region
            );
            String team = params[0];
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            s3.setRegion(Region.getRegion(Regions.US_EAST_1));

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            DBPlayerProfile playerProfile = new DBPlayerProfile();
            playerProfile.setTeam(team);
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(playerProfile)
                    .withConsistentRead(false);
            return mapper.query(DBPlayerProfile.class, queryExpression);
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBPlayerProfile> result) {
            dataFetched = true;
            initializeAdapter(result);
        }
    }
}
