package com.peprally.jeremy.peprally.adapters;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.List;

public class FistbumpedUserCardAdapter extends RecyclerView.Adapter<FistbumpedUserCardAdapter.FistbumpedUserCardHolder>{

    private Context callingContext;
    private DynamoDBHelper dynamoDBHelper;
    private List<DBUserProfile> fistbumpedUsers;
    private UserProfileParcel userProfileParcel;

    public FistbumpedUserCardAdapter(Context callingContext,
                                     List<DBUserProfile> fistbumpedUsers,
                                     UserProfileParcel userProfileParcel) {
        this.callingContext = callingContext;
        this.fistbumpedUsers = fistbumpedUsers;
        this.userProfileParcel = userProfileParcel;
        dynamoDBHelper = new DynamoDBHelper(callingContext);
    }

    static class FistbumpedUserCardHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        LinearLayout clickableContainer;
        TextView username;
        TextView firstname;

        private FistbumpedUserCardHolder(View itemView) {
            super(itemView);
            userImage = (ImageView) itemView.findViewById(R.id.id_fistbumped_users_card_profile_photo);
            clickableContainer = (LinearLayout) itemView.findViewById(R.id.id_recycler_view_container_fistbumped_users);
            username = (TextView) itemView.findViewById(R.id.id_fistbumped_users_username);
            firstname = (TextView) itemView.findViewById(R.id.id_fistbumped_users_name);
        }
    }

    @Override
    public FistbumpedUserCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_fistbumped_users, parent, false);
        return new FistbumpedUserCardHolder(view);
    }

    @Override
    public void onBindViewHolder(FistbumpedUserCardHolder fistbumpedUserCardHolder, int position) {
        final DBUserProfile userProfile = fistbumpedUsers.get(position);
        Helpers.setFacebookProfileImage(callingContext,
                fistbumpedUserCardHolder.userImage,
                userProfile.getFacebookId(),
                3,
                true);

        fistbumpedUserCardHolder.username.setText(userProfile.getUsername());
        fistbumpedUserCardHolder.firstname.setText(userProfile.getFirstname());

        fistbumpedUserCardHolder.clickableContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncHelpers.launchUserProfileActivity(callingContext, dynamoDBHelper, userProfile.getUsername(), userProfileParcel.getCurUsername());
            }
        });
    }

    @Override
    public int getItemCount() {
        return fistbumpedUsers.size();
    }
}
