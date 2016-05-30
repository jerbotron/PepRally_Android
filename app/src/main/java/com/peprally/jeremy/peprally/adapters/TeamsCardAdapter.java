package com.peprally.jeremy.peprally.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.utils.Team;

import java.util.List;

public class TeamsCardAdapter extends RecyclerView.Adapter<TeamsCardAdapter.TeamCardHolder>{

    private List<Team> teams;
    private static TeamsAdapterClickListener myClickListener;

    public interface TeamsAdapterClickListener {
        void onItemClick(View v, int position);
    }

    public static class TeamCardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cv;
        ImageView teamPhoto;
        TextView teamName;

        public TeamCardHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.rv_browse_teams);
            teamPhoto = (ImageView)itemView.findViewById(R.id.team_card_photo);
            teamName = (TextView)itemView.findViewById(R.id.team_card_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public void setOnItemClickListener(TeamsAdapterClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public TeamsCardAdapter(List<Team> teams) {
        this.teams = teams;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public TeamCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_team_card_container, parent, false);
        TeamCardHolder teamCardHolder = new TeamCardHolder(view);
        return teamCardHolder;
    }

    @Override
    public void onBindViewHolder(TeamCardHolder teamCardHolder, int position) {
        teamCardHolder.teamName.setText(teams.get(position).name);
        teamCardHolder.teamPhoto.setImageResource(teams.get(position).photoId);
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }
}