package com.peprally.jeremy.peprally.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.peprally.jeremy.peprally.activities.HomeActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.TeamsCardAdapter;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Team;
import com.peprally.jeremy.peprally.db_models.DBSport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrowseTeamsFragment extends Fragment {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private List<Team> teams;
    private RecyclerView recyclerView;

    // AWS Variables
    private DynamoDBHelper dbHelper;

    // General Variables
    private static final String TAG = HomeActivity.class.getSimpleName();
    private boolean dataFetched = false;

    /***********************************************************************************************
     *************************************** FRAGMENT METHODS **************************************
     **********************************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DynamoDBHelper(getActivity());

        new FetchSportsTableTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_teams, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_browse_teams);
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        return view;
    }

    private void initializeData(PaginatedScanList<DBSport> result){
        teams = new ArrayList<>();
        for (DBSport DBSport : result) {
            final int drawableId = getResources().getIdentifier(DBSport.getIcon(), "drawable", this.getActivity().getPackageName());
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
                ((HomeActivity) getActivity()).launchBrowsePlayerActivity(teams.get(position).name);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "browse fragment resumed");
        if (dataFetched) {
            initializeAdapter();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "browse fragment paused");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "browse fragment destroyed");
    }

    private class FetchSportsTableTask extends AsyncTask<Void, Void, PaginatedScanList<DBSport>> {
        @Override
        protected PaginatedScanList<DBSport> doInBackground(Void... params) {
            return dbHelper.getMapper().scan(DBSport.class, new DynamoDBScanExpression());
        }

        @Override
        protected void onPostExecute(PaginatedScanList<DBSport> result) {
            initializeData(result);
            dataFetched = true;
            initializeAdapter();
        }
    }
}