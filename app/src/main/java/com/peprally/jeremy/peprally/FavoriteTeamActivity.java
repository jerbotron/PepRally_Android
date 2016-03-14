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
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoriteTeamActivity extends AppCompatActivity {

    class Team implements Comparable<Team>{
        String name;
        int photoId;

        Team(String name, int photoId) {
            this.name = name;
            this.photoId = photoId;
        }

        @Override
        public int compareTo(Team another) {
            return name.compareTo(another.name);
        }
    }

    private List<Team> teams;
    private RecyclerView rv;
    private RVAdapter rvAdapter;
    private boolean dataFetched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_team);

        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setTitle("Pick a favorite team");

        rv = (RecyclerView) findViewById(R.id.rv_browse_teams);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setHasFixedSize(true);
        rv.setLayoutManager(rvLayoutManager);

        new FetchSportsTableTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataFetched) {
            initializeAdapter();
        }
    }

    private void initializeData(PaginatedScanList<Sport> result){
        teams = new ArrayList<>();
        for (Sport sport : result) {
            final int drawableId = getResources().getIdentifier(sport.getIcon(), "drawable", getPackageName());
            teams.add(new Team(sport.getName(), drawableId));
        }
        // Sort teams
        Collections.sort(teams);
    }

    private void initializeAdapter() {
        rvAdapter = new RVAdapter(teams);
        rv.setAdapter(rvAdapter);
        rvAdapter.setOnItemClickListener(new RVAdapter.MyClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent();
                intent.putExtra("FAVORITE_TEAM", teams.get(position).name);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private class FetchSportsTableTask extends AsyncTask<Void, Void, PaginatedScanList<Sport>> {
        @Override
        protected PaginatedScanList<Sport> doInBackground(Void... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),                  // Context
                    AWSCredentialProvider.IDENTITY_POOL_ID,   // Identity Pool ID
                    AWSCredentialProvider.COGNITO_REGION      // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            PaginatedScanList<Sport> result = mapper.scan(Sport.class, scanExpression);
            return result;
        }

        @Override
        protected void onPostExecute(PaginatedScanList<Sport> result) {
            initializeData(result);
            dataFetched = true;
            initializeAdapter();
        }
    }
}
