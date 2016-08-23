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

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.TeamsCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBSport;
import com.peprally.jeremy.peprally.custom.Team;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;

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
            recyclerView.setLayoutManager(new LinearLayoutManager(FavoriteTeamActivity.this));
        }

        new FetchSportsTableTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataFetched) {
            recyclerView.setLayoutManager(new LinearLayoutManager(FavoriteTeamActivity.this));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_unselect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.id_item_unselect:
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /***********************************************************************************************
     *********************************** GENERAL_METHODS ********************************
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
        TeamsCardAdapter teamsCardAdapter = new TeamsCardAdapter(teams, new TeamsCardAdapter.AdapterOnClickCallback() {
            @Override
            public void onClick(int position) {
                teamCardOnClickHandler(position);
            }
        });
        recyclerView.setAdapter(teamsCardAdapter);
    }

    public void teamCardOnClickHandler(int position) {
        Intent intent = new Intent();
        intent.putExtra("FAVORITE_TEAM", teams.get(position).name);
        setResult(Activity.RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private class FetchSportsTableTask extends AsyncTask<Void, Void, PaginatedScanList<DBSport>> {
        @Override
        protected PaginatedScanList<DBSport> doInBackground(Void... params) {
            DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(getApplicationContext());
            return dynamoDBHelper.getMapper().scan(DBSport.class, new DynamoDBScanExpression());
        }

        @Override
        protected void onPostExecute(PaginatedScanList<DBSport> result) {
            initializeData(result);
            dataFetched = true;
            initializeAdapter();
        }
    }
}
