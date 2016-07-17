package com.peprally.jeremy.peprally.adapters;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.Helpers;

import java.util.List;

public class FistbumpedUserCardAdapter extends RecyclerView.Adapter<FistbumpedUserCardAdapter.FistbumpedUserCardHolder>{

    private Context callingContext;

    private List<DBUserProfile> fistbumpedUsers;

    public FistbumpedUserCardAdapter(Context callingContext,
                                   List<DBUserProfile> fistbumpedUsers) {
        this.callingContext = callingContext;
        this.fistbumpedUsers = fistbumpedUsers;
    }

    static class FistbumpedUserCardHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView userImage;
        LinearLayout clickableContainer;
        TextView nicknameText;
        TextView firstnameText;

        private FistbumpedUserCardHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_fistbumped_users);
            userImage = (ImageView) itemView.findViewById(R.id.id_fistbumped_users_card_profile_photo);
            clickableContainer = (LinearLayout) itemView.findViewById(R.id.id_fistbumped_users_card_clickable);
            nicknameText = (TextView) itemView.findViewById(R.id.id_fistbumped_users_nickname);
            firstnameText = (TextView) itemView.findViewById(R.id.id_fistbumped_users_name);
        }
    }

    @Override
    public FistbumpedUserCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_fistbumped_users, parent, false);
        return new FistbumpedUserCardHolder(view);
    }

    @Override
    public void onBindViewHolder(FistbumpedUserCardHolder fistbumpedUserCardHolder, int position) {
        DBUserProfile userProfile = fistbumpedUsers.get(position);
        Helpers.setFacebookProfileImage(callingContext,
                fistbumpedUserCardHolder.userImage,
                userProfile.getFacebookID(),
                3);

        fistbumpedUserCardHolder.nicknameText.setText(userProfile.getNickname());
        fistbumpedUserCardHolder.firstnameText.setText(userProfile.getFirstName());

        fistbumpedUserCardHolder.clickableContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FISTBUMPS ADAPTER", "go to user profile");
            }
        });
    }

    @Override
    public int getItemCount() {
        return fistbumpedUsers.size();
    }

}
