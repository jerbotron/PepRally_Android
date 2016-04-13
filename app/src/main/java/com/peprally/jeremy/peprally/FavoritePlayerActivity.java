package com.peprally.jeremy.peprally;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class FavoritePlayerActivity extends AppCompatActivity {

    private PaginatedQueryList<DBPlayerProfile> roster;
    private RecyclerView rv;
    private RVPlayersAdapter rvPlayersAdapter;
    private String callingActivity;
    private boolean dataFetched = false;

    private static final String TAG = FavoriteTeamActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_player);

        callingActivity = getIntent().getStringExtra("CALLING_ACTIVITY");
        String team = getIntent().getStringExtra("TEAM");
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setTitle(team);

        rv = (RecyclerView) findViewById(R.id.rv_browse_players);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setHasFixedSize(true);
        rv.setLayoutManager(rvLayoutManager);

//        Log.d(TAG, "loading players for: " + team);
        new FetchTeamRosterTask().execute(team);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataFetched) {
            initializeAdapter(roster);
        }
    }

    private void initializeAdapter(PaginatedQueryList<DBPlayerProfile> result) {
        roster = result;
        rvPlayersAdapter = new RVPlayersAdapter(FavoritePlayerActivity.this, roster);
        rv.setAdapter(rvPlayersAdapter);
        rvPlayersAdapter.setOnItemClickListener(new RVPlayersAdapter.PlayersAdapterClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                if (callingActivity.equals("ProfileActivity")) {
                    Intent intent = new Intent();
                    intent.putExtra("FAVORITE_PLAYER", roster.get(position).getFavPlayerText());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                else if (callingActivity.equals("HomeActivity")) {
                    Intent intent = new Intent(FavoritePlayerActivity.this, ProfileActivity.class);
                    Bundle userProfileBundle = new Bundle();
                    userProfileBundle.putString("FIRST_NAME", roster.get(position).getFirstName());
                    userProfileBundle.putString("PLAYER_TEAM", roster.get(position).getTeam());
                    userProfileBundle.putInt("PLAYER_INDEX", roster.get(position).getIndex());
                    userProfileBundle.putString("FACEBOOK_ID", "NULL");
                    intent.putExtra("USER_PROFILE_BUNDLE", userProfileBundle);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

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
            PaginatedQueryList<DBPlayerProfile> result = mapper.query(DBPlayerProfile.class, queryExpression);
            return result;
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBPlayerProfile> result) {
            dataFetched = true;
            initializeAdapter(result);
        }
    }
}
