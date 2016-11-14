package com.peprally.jeremy.peprally.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.peprally.jeremy.peprally.activities.HomeActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.TeamsCardAdapter;
import com.peprally.jeremy.peprally.enums.TeamsEnum;
import com.peprally.jeremy.peprally.custom.Team;

import java.util.ArrayList;
import java.util.Collections;

public class BrowseTeamsFragment extends Fragment {

    private static final String TAG = "BrowseTeamsFragment";

    /***********************************************************************************************
     *************************************** FRAGMENT METHODS **************************************
     **********************************************************************************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_browse_teams, container, false);
        final ArrayList<Team> teams = new ArrayList<>();
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_browse_teams);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        initializeTeamsData(teams);
        initializeAdapter(teams, recyclerView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void initializeTeamsData(final ArrayList<Team> teams) {
        for (TeamsEnum team : TeamsEnum.values()) {
            final int drawableId = getResources().getIdentifier(team.getIconURI(), "drawable", this.getActivity().getPackageName());
            teams.add(new Team(team.getName(), drawableId));
        }
        // Sort teams
        Collections.sort(teams);
    }

    private void initializeAdapter(final ArrayList<Team> teams, final RecyclerView recyclerView) {
        final TeamsCardAdapter teamsCardAdapter = new TeamsCardAdapter(teams, new TeamsCardAdapter.AdapterOnClickCallback() {
            @Override
            public void onClick(int position) {
                teamsCardOnClickHandler(teams, position);
            }
        });
        recyclerView.setAdapter(teamsCardAdapter);
    }

    public void teamsCardOnClickHandler(final ArrayList<Team> teams, int position) {
        ((HomeActivity) getActivity()).launchBrowsePlayerActivity(teams.get(position).name);
    }
}