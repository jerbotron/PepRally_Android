package com.peprally.jeremy.peprally.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;

public class AsyncHelpers {

    /***********************************************************************************************
     *********************************** PUBLIC ASYNC TASK WRAPPERS ********************************
     **********************************************************************************************/
    public static void launchExistingUserProfileActivity(Context callingContext,
                                                         String profileUsername,
                                                         String currentUsername,
                                                         DynamoDBHelper.AsyncTaskCallback taskCallback) {
        new LaunchUserProfileActivityAsyncTask(callingContext, taskCallback).execute(profileUsername, currentUsername);
    }

    public static void launchVarsityPlayerProfileActivity(Context callingContext,
                                                          String firstname,
                                                          String lastname,
                                                          String currentUsername) {
        new LaunchVarsityPlayerProfileActivityAsyncTask(callingContext).execute(firstname, lastname, currentUsername);
    }

    /***********************************************************************************************
     **************************************** GENERAL METHODS **************************************
     **********************************************************************************************/
    private static void launchProfileActivityWithUserPlayerProfile(Context callingContext,
                                                                   String currentUsername,
                                                                   DBUserProfile userProfile,
                                                                   DBPlayerProfile playerProfile)
    {
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

    public static void launchProfileActivityWithVarsityPlayerInfo(Context callingContext,
                                                                   String currentUsername,
                                                                   String firstname,
                                                                   String playerTeam,
                                                                   int playerIndex,
                                                                   boolean isSelfProfile)
    {
        Intent intent = new Intent(callingContext, ProfileActivity.class);
        UserProfileParcel userProfileParcel = new UserProfileParcel(ActivityEnum.PROFILE,
                                                                    currentUsername,
                                                                    firstname,
                                                                    playerTeam,
                                                                    playerIndex,
                                                                    isSelfProfile);

        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callingContext.startActivity(intent);
        ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }


    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private static class LaunchUserProfileActivityAsyncTask extends AsyncTask<String, Void, Boolean> {

        private Context callingContext;
        private DBUserProfile userProfile;
        private DBPlayerProfile playerProfile;
        private String currentUsername;
        private DynamoDBHelper.AsyncTaskCallback taskCallback;

        private LaunchUserProfileActivityAsyncTask(Context callingContext,
                                                   DynamoDBHelper.AsyncTaskCallback taskCallback) {
            this.callingContext = callingContext;
            this.taskCallback =  taskCallback;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String profileUsername = strings[0];
            currentUsername = strings[1];
            DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(callingContext);

            userProfile = dynamoDBHelper.loadDBUserProfile(profileUsername);

            if (userProfile == null) {
                return false;
            } else {
                if (userProfile.getIsVarsityPlayer()) {
                    playerProfile = dynamoDBHelper.loadDBPlayerProfile(userProfile.getTeam(), userProfile.getPlayerIndex());
                }
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean isUserFound) {
            if (isUserFound) {
                launchProfileActivityWithUserPlayerProfile(callingContext, currentUsername, userProfile, playerProfile);
            } else {
                if (taskCallback != null)
                    taskCallback.onTaskDone();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static class LaunchVarsityPlayerProfileActivityAsyncTask extends AsyncTask<String, Void, Boolean> {
        private Context callingContext;
        private DBUserProfile userProfile;
        private DBPlayerProfile playerProfile;
        private String currentUsername;

        private LaunchVarsityPlayerProfileActivityAsyncTask(Context callingContext) {
            this.callingContext = callingContext;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String firstname = strings[0];
            String lastname = strings[1];
            currentUsername = strings[2];
            DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(callingContext);

            playerProfile = new DBPlayerProfile();
            playerProfile.setFirstName(firstname);
            playerProfile.setLastName(lastname);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBPlayerProfile>()
                    .withHashKeyValues(playerProfile)
                    .withIndexName("FirstName-LastName-index")
                    .withRangeKeyCondition("LastName", new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue().withS(lastname)))
                    .withConsistentRead(false);
            PaginatedQueryList<DBPlayerProfile> queryResults = dynamoDBHelper.getMapper().query(DBPlayerProfile.class, queryExpression);
            if (queryResults != null && queryResults.size() == 1) {
                playerProfile = queryResults.get(0);
                if (playerProfile.getHasUserProfile()) {
                    userProfile = dynamoDBHelper.loadDBUserProfile(playerProfile.getUsername());
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean profileFound) {
            if (profileFound) {
                if (userProfile != null) {
                    launchProfileActivityWithUserPlayerProfile(
                            callingContext,
                            currentUsername,
                            userProfile,
                            playerProfile);
                } else {
                    launchProfileActivityWithVarsityPlayerInfo(
                            callingContext,
                            currentUsername,
                            playerProfile.getFirstName(),
                            playerProfile.getTeam(),
                            playerProfile.getIndex(),
                            false);
                }
            }
        }
    }
}
