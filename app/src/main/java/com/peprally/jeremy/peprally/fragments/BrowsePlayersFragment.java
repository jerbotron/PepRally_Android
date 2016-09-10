package com.peprally.jeremy.peprally.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.PlayersCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;

import java.util.HashMap;
import java.util.Map;

public class BrowsePlayersFragment extends Fragment {

    // Network Variables
    private DynamoDBHelper dynamoDBHelper;

    // UI Variables
    private RecyclerView recyclerView;

    // General Variables
    private String currentTeam;
    private String teamGender;
    private PaginatedQueryList<DBPlayerProfile> rosterList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dynamoDBHelper = new DynamoDBHelper(getContext().getApplicationContext());

        Bundle incomingBundle = getArguments();

        if (incomingBundle != null) {
            currentTeam = incomingBundle.getString("TEAM");
            teamGender = incomingBundle.getString("GENDER");
            // fetch team roster list
            new FetchTeamRosterAsyncTask(currentTeam).execute();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_players, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.id_recyclerview_browse_players);
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        return view;
    }

    /***********************************************************************************************
     **************************************** GENERAL_METHODS **************************************
     **********************************************************************************************/
    private void initializeAdapter(PaginatedQueryList<DBPlayerProfile> result) {
        rosterList = result;
        PlayersCardAdapter playersCardAdapter = new PlayersCardAdapter(getContext(), rosterList);
        if (recyclerView != null) {
            recyclerView.setAdapter(playersCardAdapter);
        }
    }

    public DBPlayerProfile getDBPlayerProfile(int position) {
        return rosterList.get(position);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/

    @SuppressWarnings("unchecked")
    private class FetchTeamRosterAsyncTask extends AsyncTask<Void, Void, PaginatedQueryList<DBPlayerProfile>> {

        private String team;

        private FetchTeamRosterAsyncTask(String team) {
            this.team = team;
        }

        @Override
        protected PaginatedQueryList<DBPlayerProfile> doInBackground(Void... params) {
            DBPlayerProfile playerProfile = new DBPlayerProfile();
            playerProfile.setTeam(team);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(playerProfile)
                    .withConsistentRead(false);

            if (teamGender != null) {
                Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
                expressionAttributeValues.put(":type", new AttributeValue().withS(String.valueOf(teamGender)));
                queryExpression
                        .withFilterExpression("MF = :type")
                        .withExpressionAttributeValues(expressionAttributeValues);
            }

            return dynamoDBHelper.getMapper().query(DBPlayerProfile.class, queryExpression);
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBPlayerProfile> result) {
            initializeAdapter(result);
        }
    }
}

