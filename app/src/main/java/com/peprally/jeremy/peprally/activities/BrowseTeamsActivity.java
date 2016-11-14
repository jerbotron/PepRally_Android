package com.peprally.jeremy.peprally.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.TeamsCardAdapter;
import com.peprally.jeremy.peprally.custom.Team;
import com.peprally.jeremy.peprally.enums.TeamsEnum;

import java.util.ArrayList;
import java.util.Collections;

public class BrowseTeamsActivity extends AppCompatActivity {
    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_team);
        final ArrayList<Team> teams = new ArrayList<>();

        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_browse_teams);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(BrowseTeamsActivity.this));
        }

        initializeTeamsData(teams);
        initializeAdapter(teams, recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
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
    /**********************************************************************************************
     *************************************** GENERAL_METHODS **************************************
     **********************************************************************************************/
    public void initializeTeamsData(final ArrayList<Team> teams) {
        for (TeamsEnum team : TeamsEnum.values()) {
            final int drawableId = getResources().getIdentifier(team.getIconURI(), "drawable", getPackageName());
            teams.add(new Team(team.getName(), drawableId));
        }
        // Sort teams
        Collections.sort(teams);
    }

    private void initializeAdapter(final ArrayList<Team> teams, final RecyclerView recyclerView) {
        final TeamsCardAdapter teamsCardAdapter = new TeamsCardAdapter(teams, new TeamsCardAdapter.AdapterOnClickCallback() {
            @Override
            public void onClick(int position) {
                teamCardOnClickHandler(teams, position);
            }
        });
        recyclerView.setAdapter(teamsCardAdapter);
    }

    public void teamCardOnClickHandler(final ArrayList<Team> teams, int position) {
        Intent intent = new Intent();
        intent.putExtra("FAVORITE_TEAM", teams.get(position).name);
        setResult(Activity.RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
