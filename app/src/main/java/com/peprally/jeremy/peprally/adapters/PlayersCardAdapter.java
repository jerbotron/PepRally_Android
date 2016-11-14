package com.peprally.jeremy.peprally.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.BrowsePlayersActivity;
import com.peprally.jeremy.peprally.custom.ui.CircleImageTransformation;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.utils.Constants;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.squareup.picasso.Picasso;

public class PlayersCardAdapter extends RecyclerView.Adapter<PlayersCardAdapter.PlayerCardHolder>{

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/

    // General Variables
    private Context callingContext;
    private PaginatedQueryList<DBPlayerProfile> roster;

    /***********************************************************************************************
     **************************************** ADAPTER METHODS **************************************
     **********************************************************************************************/
    static class PlayerCardHolder extends RecyclerView.ViewHolder {
        LinearLayout cardContainer;
        ImageView playerPhoto;
        TextView playerName;
        TextView playerInfo;

        private PlayerCardHolder(View itemView) {
            super(itemView);
            cardContainer = (LinearLayout) itemView.findViewById(R.id.id_recycler_view_player_card_container);
            playerPhoto = (ImageView) itemView.findViewById(R.id.id_player_card_profile_photo);
            playerName = (TextView) itemView.findViewById(R.id.id_notification_card_content);
            playerInfo = (TextView) itemView.findViewById(R.id.id_player_card_info);
        }
    }

    public PlayersCardAdapter(Context callingContext, PaginatedQueryList<DBPlayerProfile> roster) {
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
        return new PlayerCardHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlayerCardHolder playerCardHolder, int position) {
        final DBPlayerProfile curPlayer = roster.get(position);
        String extension = curPlayer.getTeam().replace(" ","+") + "/" + curPlayer.getImageURL();
        final String url = Constants.S3_ROSTER_PHOTOS_2016_URL + extension;
        Picasso.with(callingContext)
                .load(url)
                .placeholder(R.drawable.img_default_ut_placeholder)
                .error(R.drawable.img_default_ut_placeholder)
                .transform(new CircleImageTransformation())
                .into(playerCardHolder.playerPhoto);
        String playerNameText = curPlayer.getFirstName() + " " + curPlayer.getLastName();
        switch (curPlayer.getTeam()) {
            case "Golf":
            case "Rowing":
            case "Swimming and Diving":
            case "Tennis":
            case "Track and Field":
                playerCardHolder.playerName.setText(Helpers.getAPICompatHtml("<b>" + playerNameText + "</b>"));
                break;
            default:
                if (curPlayer.getNumber() >= 0) {
                    playerCardHolder.playerName.setText(Helpers.getAPICompatHtml("<b>#"
                            + String.valueOf(curPlayer.getNumber()) + " "
                            + playerNameText + "</b>"));
                } else {
                    playerCardHolder.playerName.setText(Helpers.getAPICompatHtml("<b> "
                            + playerNameText + "</b>"));
                }
                break;
        }
        String playerInfoText = "";
        if (curPlayer.getPosition() != null)
            playerInfoText = curPlayer.getPosition();
        if (curPlayer.getHometown() != null){
            if (playerInfoText.isEmpty())
                playerInfoText = curPlayer.getHometown();
            else
                playerInfoText = playerInfoText + " | " + curPlayer.getHometown();
        }
        playerCardHolder.playerInfo.setText(playerInfoText);

        playerCardHolder.cardContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BrowsePlayersActivity) callingContext).playerCardOnClickHandler(playerCardHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return roster.size();
    }
}