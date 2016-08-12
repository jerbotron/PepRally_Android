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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.TeamsCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBSport;
import com.peprally.jeremy.peprally.network.AWSCredentialProvider;
import com.peprally.jeremy.peprally.custom.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoriteTeamActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private RecyclerView recyclerView;

    // General Variables
    private List<Team> teams;
    private boolean dataFetched = false;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_team);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.rv_browse_teams);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
            recyclerView.setAdapter(new EmptyAdapter());
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        }

        new FetchSportsTableTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataFetched) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            initializeAdapter();
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
    private void initializeData(PaginatedScanList<DBSport> result){
        teams = new ArrayList<>();
        for (DBSport DBSport : result) {
            final int drawableId = getResources().getIdentifier(DBSport.getIcon(), "drawable", getPackageName());
            teams.add(new Team(DBSport.getName(), drawableId));
        }
        // Sort teams
        Collections.sort(teams);
    }

    private void initializeAdapter() {
        TeamsCardAdapter teamsCardAdapter = new TeamsCardAdapter(teams);
        recyclerView.setAdapter(teamsCardAdapter);
        teamsCardAdapter.setOnItemClickListener(new TeamsCardAdapter.TeamsAdapterClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent();
                intent.putExtra("FAVORITE_TEAM", teams.get(position).name);
                setResult(Activity.RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private class FetchSportsTableTask extends AsyncTask<Void, Void, PaginatedScanList<DBSport>> {
        @Override
        protected PaginatedScanList<DBSport> doInBackground(Void... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),                  // Context
                    AWSCredentialProvider.IDENTITY_POOL_ID,   // Identity Pool ID
                    AWSCredentialProvider.COGNITO_REGION      // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            return mapper.scan(DBSport.class, new DynamoDBScanExpression());
        }

        @Override
        protected void onPostExecute(PaginatedScanList<DBSport> result) {
            initializeData(result);
            dataFetched = true;
            initializeAdapter();
        }
    }
}
