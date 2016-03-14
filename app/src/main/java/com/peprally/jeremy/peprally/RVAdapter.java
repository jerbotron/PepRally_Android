package com.peprally.jeremy.peprally;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.TeamViewHolder>{

    private List<FavoriteTeamActivity.Team> teams;
    private static MyClickListener myClickListener;
    private static final String TAG = ProfileActivity.class.getSimpleName();

    public interface MyClickListener {
        void onItemClick(View v, int position);
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView teamName;
        ImageView teamPhoto;

        public TeamViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.rv_browse_teams);
            teamName = (TextView)itemView.findViewById(R.id.browse_name);
            teamPhoto = (ImageView)itemView.findViewById(R.id.browse_photo);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public RVAdapter(List<FavoriteTeamActivity.Team> teams) {
        this.teams = teams;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public TeamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_card_container, parent, false);
        TeamViewHolder tvh = new TeamViewHolder(view);
        return tvh;
    }

    @Override
    public void onBindViewHolder(TeamViewHolder teamViewHolder, int position) {
        teamViewHolder.teamName.setText(teams.get(position).name);
        teamViewHolder.teamPhoto.setImageResource(teams.get(position).photoId);
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }
}