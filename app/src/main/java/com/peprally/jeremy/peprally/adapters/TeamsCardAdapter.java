package com.peprally.jeremy.peprally.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.custom.Team;

import java.util.List;

public class TeamsCardAdapter extends RecyclerView.Adapter<TeamsCardAdapter.TeamCardHolder>{

    private List<Team> teams;

    public interface AdapterOnClickCallback {
        void onClick(int position);
    }

    private AdapterOnClickCallback adapterOnClickCallback;

    static class TeamCardHolder extends RecyclerView.ViewHolder {
        LinearLayout clickableContainer;
        ImageView teamPhoto;
        TextView teamName;

        private TeamCardHolder(View itemView) {
            super(itemView);
            clickableContainer = (LinearLayout) itemView.findViewById(R.id.id_container_browse_teams_card_clickable);
            teamPhoto = (ImageView) itemView.findViewById(R.id.id_notification_card_profile_photo);
            teamName = (TextView) itemView.findViewById(R.id.id_team_card_team_name);
        }
    }

    public TeamsCardAdapter(List<Team> teams,
                            AdapterOnClickCallback adapterOnClickCallback) {
        this.teams = teams;
        this.adapterOnClickCallback = adapterOnClickCallback;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public TeamCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_team_card_container, parent, false);
        return new TeamCardHolder(view);
    }

    @Override
    public void onBindViewHolder(final TeamCardHolder teamCardHolder, int position) {
        teamCardHolder.teamName.setText(teams.get(position).name);
        teamCardHolder.teamPhoto.setImageResource(teams.get(position).photoId);
        teamCardHolder.clickableContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterOnClickCallback.onClick(teamCardHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }
}