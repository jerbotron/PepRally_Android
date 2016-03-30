package com.peprally.jeremy.peprally;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.http.HttpClient;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RVPlayersAdapter extends RecyclerView.Adapter<RVPlayersAdapter.PlayerCardHolder>{

    private  Context callingContext;
    private PaginatedQueryList<PlayerProfile> roster;
    private static PlayersAdapterClickListener myClickListener;
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private final String rootImageURL = "https://s3.amazonaws.com/rosterphotos/";

    public interface PlayersAdapterClickListener {
        void onItemClick(View v, int position);
    }

    public static class PlayerCardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cv;
        ImageView playerPhoto;
        TextView playerName;
        TextView playerInfo;

        public PlayerCardHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.rv_browse_players);
            playerPhoto = (ImageView) itemView.findViewById(R.id.player_card_photo);
            playerName = (TextView) itemView.findViewById(R.id.player_card_name);
            playerInfo = (TextView) itemView.findViewById(R.id.player_card_info);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public void setOnItemClickListener(PlayersAdapterClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public RVPlayersAdapter(Context callingContext, PaginatedQueryList<PlayerProfile> roster) {
        this.callingContext = callingContext;
        this.roster = roster;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PlayerCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_player_card_container, parent, false);
        PlayerCardHolder playerCardHolder = new PlayerCardHolder(view);
        return playerCardHolder;
    }

    @Override
    public void onBindViewHolder(PlayerCardHolder playerCardHolder, int position) {
        PlayerProfile curPlayer = roster.get(position);
        String extension = curPlayer.getTeam().replace(" ","+") + "/" + curPlayer.getImageURL();
        String url = rootImageURL + extension;
        Picasso.with(callingContext)
                .load(url)
                .placeholder(R.drawable.default_placeholder)
                .error(R.drawable.default_placeholder)
                .into(playerCardHolder.playerPhoto);
        String playerNameText = curPlayer.getFirstName() + " " + curPlayer.getLastName();
        String playerInfoText;
        switch (curPlayer.getTeam()) {
            case "Golf":
            case "Rowing":
            case "Swimming and Diving":
            case "Tennis":
            case "Track and Field":
                playerCardHolder.playerName.setText(Html.fromHtml("<b>"
                        + playerNameText + "</b>"));
                break;
            default:
                playerCardHolder.playerName.setText(Html.fromHtml("<b>#"
                        + String.valueOf(curPlayer.getNumber()) + " "
                        + playerNameText + "</b>"));
                break;
        }
        if (curPlayer.getPosition() == null) {
            playerInfoText = curPlayer.getHometown();
        }
        else {
            playerInfoText = curPlayer.getPosition() + " | " + roster.get(position).getHometown();
        }
        playerCardHolder.playerInfo.setText(playerInfoText);
    }

    @Override
    public int getItemCount() {
        return roster.size();
    }
}