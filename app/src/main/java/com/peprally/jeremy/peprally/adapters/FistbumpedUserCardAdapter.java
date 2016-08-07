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
import com.peprally.jeremy.peprally.utils.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
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
        TextView nicknameText;
        TextView firstnameText;

        private FistbumpedUserCardHolder(View itemView) {
            super(itemView);
            userImage = (ImageView) itemView.findViewById(R.id.id_fistbumped_users_card_profile_photo);
            clickableContainer = (LinearLayout) itemView.findViewById(R.id.id_recycler_view_container_fistbumped_users);
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
        final DBUserProfile userProfile = fistbumpedUsers.get(position);
        Helpers.setFacebookProfileImage(callingContext,
                fistbumpedUserCardHolder.userImage,
                userProfile.getFacebookId(),
                3);

        fistbumpedUserCardHolder.nicknameText.setText(userProfile.getNickname());
        fistbumpedUserCardHolder.firstnameText.setText(userProfile.getFirstName());

        fistbumpedUserCardHolder.clickableContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LaunchUserProfileActivityAsyncTask().execute(userProfile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fistbumpedUsers.size();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class LaunchUserProfileActivityAsyncTask extends AsyncTask<DBUserProfile, Void, DBPlayerProfile> {
        DBUserProfile userProfile;
        @Override
        protected DBPlayerProfile doInBackground(DBUserProfile... dbUserProfiles) {
            userProfile = dbUserProfiles[0];
            if (userProfile.getIsVarsityPlayer()) {
                DBPlayerProfile playerProfile = dynamoDBHelper.loadDBPlayerProfile(userProfile.getTeam(), userProfile.getPlayerIndex());
                if (playerProfile != null) {
                    return playerProfile;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(DBPlayerProfile playerProfile) {
            Intent intent = new Intent(callingContext, ProfileActivity.class);
            UserProfileParcel parcel = new UserProfileParcel(ActivityEnum.PROFILE,
                                                             userProfile,
                                                             playerProfile);
            if (!userProfile.getNickname().equals(userProfileParcel.getCurUserNickname()))
                parcel.setIsSelfProfile(false);
            intent.putExtra("USER_PROFILE_PARCEL", parcel);
            callingContext.startActivity(intent);
            ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }
}
