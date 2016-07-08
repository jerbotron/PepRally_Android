package com.peprally.jeremy.peprally.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserNickname;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;

import java.util.List;

public class DynamoDBHelper {

    // AWS Variables
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    public DynamoDBHelper(Context callingContext) {
        // Set up AWS members
        refresh(callingContext);
    }

    public void refresh(Context callingContext) {
        credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,                                         // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,                 // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION                    // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
    }

    public String getIdentityID() {
        return credentialsProvider.getIdentityId();
    }

    public DynamoDBMapper getMapper() {
        return mapper;
    }

    public void saveDBObject(Object object) {
        mapper.save(object);
    }

    public void saveDBObjectAsync(Object object) {
        new saveDBObjectAsyncTask().execute(new asyncTaskObjectDefault(object, mapper));
    }

    public void deleteDBObject(Object object) {
        mapper.delete(object);
    }

    // Database load Methods

    public DBUserPost loadDBPost(String postNickname, Long timeStampInSeconds) {
        return mapper.load(DBUserPost.class, postNickname, timeStampInSeconds);
    }

    public DBUserProfile loadDBUserProfile(String postNickname) {
        return mapper.load(DBUserProfile.class, postNickname);
    }

    public DBPlayerProfile loadDBPlayerProfile(String playerTeam, Integer playerIndex) {
        return mapper.load(DBPlayerProfile.class, playerTeam, playerIndex);
    }

    public DBUserNickname loadDBNickname(String nickname) {
        return mapper.load(DBUserNickname.class, nickname);
    }

    public DBUserProfile queryDBUserProfileWithCognitoID() {
        DBUserProfile userProfile = new DBUserProfile();
        userProfile.setCognitoId(credentialsProvider.getIdentityId());
        DynamoDBQueryExpression<DBUserProfile> queryExpression = new DynamoDBQueryExpression<DBUserProfile>()
                .withIndexName("CognitoID-index")
                .withHashKeyValues(userProfile)
                .withConsistentRead(false);
        List<DBUserProfile> results = mapper.query(DBUserProfile.class, queryExpression);
        if (results == null || results.size() == 0) {
            return null;
        }
        else{
            if (results.size() == 1) {
                userProfile = results.get(0);
                return userProfile;
            }
            else{
                Log.d("DynamoDBHelper: ", "Query result should have only returned single user!");
                return null;
            }
        }
    }

    // Database save methods

    public void incrementUserSentFistbumpsCount(String userNickname) {
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userNickname);
        new incrementUserSentFistbumpsCountAsyncTask().execute(new asyncTaskObjectBundle(bundle, mapper));
    }

    public void decrementUserSentFistbumpsCount(String userNickname) {
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userNickname);
        new decrementUserSentFistbumpsCountAsyncTask().execute(new asyncTaskObjectBundle(bundle, mapper));
    }

    public void incrementUserReceivedFistbumpsCount(String userNickname) {
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userNickname);
        new incrementUserReceivedFistbumpsCountAsyncTask().execute(new asyncTaskObjectBundle(bundle, mapper));
    }

    public void decrementUserReceivedFistbumpsCount(String userNickname) {
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userNickname);
        new decrementUserReceivedFistbumpsCountAsyncTask().execute(new asyncTaskObjectBundle(bundle, mapper));
    }

    /********************************** AsyncTasks **********************************/

    private static class asyncTaskObjectDefault {
        Object object;
        DynamoDBMapper mapper;
        asyncTaskObjectDefault(Object object,
                               DynamoDBMapper mapper) {
            this.object = object;
            this.mapper = mapper;
        }
    }

    private static class asyncTaskObjectUserProfileParcel {
        UserProfileParcel userProfileParcel;
        DynamoDBMapper mapper;
        asyncTaskObjectUserProfileParcel(UserProfileParcel userProfileParcel,
                                         DynamoDBMapper mapper) {
            this.userProfileParcel = userProfileParcel;
            this.mapper = mapper;
        }
    }

    private static class asyncTaskObjectBundle {
        Bundle bundle;
        DynamoDBMapper mapper;
        asyncTaskObjectBundle(Bundle bundle,
                                         DynamoDBMapper mapper) {
            this.bundle = bundle;
            this.mapper = mapper;
        }
    }

    private static class saveDBObjectAsyncTask extends AsyncTask<asyncTaskObjectDefault, Void, Void> {
        @Override
        protected Void doInBackground(asyncTaskObjectDefault... params) {
            params[0].mapper.save(params[0].object);
            return null;
        }
    }

    private static class incrementUserSentFistbumpsCountAsyncTask extends AsyncTask<asyncTaskObjectBundle, Void, Void> {
        @Override
        protected Void doInBackground(asyncTaskObjectBundle... params) {
            DynamoDBMapper mapper = params[0].mapper;
            Bundle bundle = params[0].bundle;
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, bundle.getString("NICKNAME"));
            userProfile.setSentFistbumpsCount(userProfile.getSentFistbumpsCount() + 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private static class decrementUserSentFistbumpsCountAsyncTask extends AsyncTask<asyncTaskObjectBundle, Void, Void> {
        @Override
        protected Void doInBackground(asyncTaskObjectBundle... params) {
            DynamoDBMapper mapper = params[0].mapper;
            Bundle bundle = params[0].bundle;
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, bundle.getString("NICKNAME"));
            userProfile.setSentFistbumpsCount(userProfile.getSentFistbumpsCount() - 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private static class incrementUserReceivedFistbumpsCountAsyncTask extends AsyncTask<asyncTaskObjectBundle, Void, Void> {
        @Override
        protected Void doInBackground(asyncTaskObjectBundle... params) {
            DynamoDBMapper mapper = params[0].mapper;
            Bundle bundle = params[0].bundle;
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, bundle.getString("NICKNAME"));
            userProfile.setReceivedFistbumpsCount(userProfile.getReceivedFistbumpsCount() + 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private static class decrementUserReceivedFistbumpsCountAsyncTask extends AsyncTask<asyncTaskObjectBundle, Void, Void> {
        @Override
        protected Void doInBackground(asyncTaskObjectBundle... params) {
            DynamoDBMapper mapper = params[0].mapper;
            Bundle bundle = params[0].bundle;
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, bundle.getString("NICKNAME"));
            userProfile.setReceivedFistbumpsCount(userProfile.getReceivedFistbumpsCount() - 1);
            mapper.save(userProfile);
            return null;
        }
    }
}
