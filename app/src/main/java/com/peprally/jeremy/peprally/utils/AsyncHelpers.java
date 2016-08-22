package com.peprally.jeremy.peprally.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;

public class AsyncHelpers {

    public static void launchExistingUserProfileActivity(Context callingContext,
                                                         String profileUsername,
                                                         String currentUsername) {
        new LaunchUserProfileActivityAsyncTask(callingContext).execute(profileUsername, currentUsername);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private static class LaunchUserProfileActivityAsyncTask extends AsyncTask<String, Void, DBPlayerProfile> {

        private Context callingContext;
        private DBUserProfile userProfile;
        private String currentUsername;

        private LaunchUserProfileActivityAsyncTask(Context callingContext) {
            this.callingContext = callingContext;
        }

        @Override
        protected DBPlayerProfile doInBackground(String... strings) {
            String profileUsername = strings[0];
            currentUsername = strings[1];
            DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(callingContext);

            userProfile = dynamoDBHelper.loadDBUserProfile(profileUsername);

            if (userProfile != null && userProfile.getIsVarsityPlayer()) {
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
            UserProfileParcel userProfileParcel = new UserProfileParcel(ActivityEnum.PROFILE,
                    userProfile,
                    playerProfile);
            if (currentUsername != null && !userProfile.getUsername().equals(currentUsername)) {
                userProfileParcel.setIsSelfProfile(false);
                userProfileParcel.setCurrentUsername(currentUsername);
            }
            intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callingContext.startActivity(intent);
            ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }
}
