package com.peprally.jeremy.peprally.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;

public class AsyncHelpers {

    public static void launchUserProfileActivity(Context callingContext,
                                                 DynamoDBHelper dynamoDBHelper,
                                                 String profileUsername,
                                                 String curUsername) {
        new LaunchUserProfileActivityAsyncTask(callingContext, dynamoDBHelper).execute(profileUsername, curUsername);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private static class LaunchUserProfileActivityAsyncTask extends AsyncTask<String, Void, DBPlayerProfile> {

        private Context callingContext;
        private DynamoDBHelper dynamoDBHelper;
        private DBUserProfile userProfile;
        private String curUsername;

        private LaunchUserProfileActivityAsyncTask(Context callingContext, DynamoDBHelper dynamoDBHelper) {
            this.callingContext = callingContext;
            this.dynamoDBHelper = dynamoDBHelper;
        }

        @Override
        protected DBPlayerProfile doInBackground(String... strings) {
            String profileUsername = strings[0];
            curUsername = strings[1];

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
            ((Activity) callingContext).finish();
            Intent intent = new Intent(callingContext, ProfileActivity.class);
            UserProfileParcel parcel = new UserProfileParcel(ActivityEnum.PROFILE,
                    userProfile,
                    playerProfile);
            if (curUsername != null && !userProfile.getUsername().equals(curUsername)) {
                parcel.setIsSelfProfile(false);
                parcel.setCurUsername(curUsername);
            }
            intent.putExtra("USER_PROFILE_PARCEL", parcel);
            callingContext.startActivity(intent);
            ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }
}
