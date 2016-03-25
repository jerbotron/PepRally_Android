package com.peprally.jeremy.peprally;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrowseTeamsFragment extends Fragment {

    private List<Team> teams;
    private RecyclerView rv;
    private RVTeamsAdapter rvTeamsAdapter;
    private boolean dataFetched = false;

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "browse fragment created");
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                this.getActivity(),                       // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,   // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION      // Region
        );

        new FetchSportsTableTask().execute(credentialsProvider);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "browse fragment created view");
        View view = inflater.inflate(R.layout.fragment_browse_teams, container, false);
        rv = (RecyclerView) view.findViewById(R.id.rv_browse_teams);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this.getActivity());
        rv.setHasFixedSize(true);
        rv.setLayoutManager(rvLayoutManager);

        return view;
    }

    private void initializeData(PaginatedScanList<Sport> result){
        teams = new ArrayList<>();
        for (Sport sport : result) {
            final int drawableId = getResources().getIdentifier(sport.getIcon(), "drawable", this.getActivity().getPackageName());
            teams.add(new Team(sport.getName(), drawableId));
        }
        // Sort teams
        Collections.sort(teams);
    }

    private void initializeAdapter() {
        rvTeamsAdapter = new RVTeamsAdapter(teams);
        rv.setAdapter(rvTeamsAdapter);
        rvTeamsAdapter.setOnItemClickListener(new RVTeamsAdapter.TeamsAdapterClickListener() {
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

    private class FetchSportsTableTask extends AsyncTask<CognitoCachingCredentialsProvider, Void, PaginatedScanList<Sport>> {
        @Override
        protected PaginatedScanList<Sport> doInBackground(CognitoCachingCredentialsProvider... params) {
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(params[0]);
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