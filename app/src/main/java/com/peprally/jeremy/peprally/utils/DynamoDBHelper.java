package com.peprally.jeremy.peprally.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.db_models.DBUserNickname;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    /***********************************************************************************************
     *************************************** DATABASE METHODS **************************************
     **********************************************************************************************/
    public void saveDBObject(Object object) {
        mapper.save(object);
    }

    public void saveDBObjectAsync(Object object) {
        new SaveDBObjectAsyncTask().execute(object);
    }

    public void deleteDBObject(Object object) {
        mapper.delete(object);
    }

    // Database load Methods

    public DBUserProfile loadDBUserProfile(String postNickname) {
        return mapper.load(DBUserProfile.class, postNickname);
    }

    public DBPlayerProfile loadDBPlayerProfile(String playerTeam, Integer playerIndex) {
        return mapper.load(DBPlayerProfile.class, playerTeam, playerIndex);
    }

    public DBUserPost loadDBUserPost(String postNickname, Long timeStampInSeconds) {
        return mapper.load(DBUserPost.class, postNickname, timeStampInSeconds);
    }

    public DBUserComment loadDBUserComment(String postID) {
        return mapper.load(DBUserComment.class, postID);
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
        new IncrementUserSentFistbumpsCountAsyncTask().execute(userNickname);
    }

    public void decrementUserSentFistbumpsCount(String userNickname) {
        new DecrementUserSentFistbumpsCountAsyncTask().execute(userNickname);
    }

    public void incrementUserReceivedFistbumpsCount(String userNickname) {
        new IncrementUserReceivedFistbumpsCountAsyncTask().execute(userNickname);
    }

    public void decrementUserReceivedFistbumpsCount(String userNickname) {
        new DecrementUserReceivedFistbumpsCountAsyncTask().execute(userNickname);
    }

    public void incrementPostCommentsCount(DBUserPost userPost) {
        new IncrementPostCommentsCountAsyncTask().execute(userPost);
    }

    public void decrementPostCommentsCount(DBUserPost userPost) {
        new DecrementPostCommentsCountAsyncTask().execute(userPost);
    }

    public void makeNewNotification(Bundle bundle) {
        new MakeNewNotificationAsyncTask().execute(bundle);
    }

    public void deleteCommentFistbumpNotification(NotificationEnum notificationEnum, String commentID, String senderNickname) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("NOTIFICATION_ENUM", notificationEnum);
        bundle.putString("COMMENT_ID", commentID);
        bundle.putString("SENDER_NICKNAME", senderNickname);
        new DeleteNotificationAsyncTask().execute(bundle);
    }

    public void deletePostFistbumpNotification(NotificationEnum notificationEnum, String postID, String senderNickname) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("NOTIFICATION_ENUM", notificationEnum);
        bundle.putString("POST_ID", postID);
        bundle.putString("SENDER_NICKNAME", senderNickname);
        new DeleteNotificationAsyncTask().execute(bundle);
    }

    public void deletePostCommentNotification(NotificationEnum notificationEnum, DBUserComment userComment) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("NOTIFICATION_ENUM", notificationEnum);
        bundle.putParcelable("USER_COMMENT", userComment);
        new DeleteNotificationAsyncTask().execute(bundle);
    }

    public void batchDeleteCommentNotifications(DBUserComment userComment) {
        new BatchDeleteCommentNotifications().execute(userComment);
    }

    public void batchDeletePostNotifications(DBUserPost userPost) {
        new BatchDeletePostNotifications().execute(userPost);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class SaveDBObjectAsyncTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            mapper.save(params[0]);
            return null;
        }
    }

    private class IncrementUserSentFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... userNickname) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, userNickname[0]);
            userProfile.setSentFistbumpsCount(userProfile.getSentFistbumpsCount() + 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class DecrementUserSentFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... userNickname) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, userNickname[0]);
            userProfile.setSentFistbumpsCount(userProfile.getSentFistbumpsCount() - 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class IncrementUserReceivedFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... userNickname) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, userNickname[0]);
            userProfile.setReceivedFistbumpsCount(userProfile.getReceivedFistbumpsCount() + 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class DecrementUserReceivedFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... userNickname) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, userNickname[0]);
            userProfile.setReceivedFistbumpsCount(userProfile.getReceivedFistbumpsCount() - 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class IncrementPostCommentsCountAsyncTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost userPost = params[0];
            if (userPost != null) {
                userPost.setCommentsCount(userPost.getCommentsCount() + 1);
                mapper.save(userPost);
            }
            return null;
        }
    }

    private class DecrementPostCommentsCountAsyncTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost userPost = params[0];
            if (userPost != null) {
                userPost.setCommentsCount(userPost.getCommentsCount() - 1);
                mapper.save(userPost);
            }
            return null;
        }
    }

    private class MakeNewNotificationAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            UserProfileParcel userProfileParcel = bundle.getParcelable("USER_PROFILE_PARCEL");
            DBUserNotification userNotification = new DBUserNotification();
            // getting time stamp
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            userNotification.setTimeInSeconds(System.currentTimeMillis()/1000);
            userNotification.setTimeStamp(df.format(c.getTime()));
            // setting up new user notification
            userNotification.setNickname(bundle.getString("RECEIVER_NICKNAME")); // who the notification is going to
            if (userProfileParcel != null) {
                userNotification.setNicknameSender(userProfileParcel.getCurUserNickname());
                userNotification.setFacebookIDSender(userProfileParcel.getFacebookID());
            }

            switch (bundle.getInt("TYPE")) {
                case 0: // direct fistbump
                    userNotification.setType(0);
                    userNotification.setNicknameSender(bundle.getString("SENDER_NICKNAME"));
                    DBUserProfile senderProfile = loadDBUserProfile(bundle.getString("SENDER_NICKNAME"));
                    userNotification.setFacebookIDSender(senderProfile.getFacebookID());
                    break;
                case 1: // comment on post
                    userNotification.setType(1);
                    userNotification.setPostID(bundle.getString("POST_ID"));
                    userNotification.setCommentID(bundle.getString("COMMENT_ID"));
                    userNotification.setComment(bundle.getString("COMMENT"));
                    break;
                case 2: // fistbump on post
                    userNotification.setType(2);
                    userNotification.setPostID(bundle.getString("POST_ID"));
                    break;
                case 3: // fistbump on comment
                    userNotification.setType(3);
                    userNotification.setPostID(bundle.getString("POST_ID"));
                    userNotification.setCommentID(bundle.getString("COMMENT_ID"));
                    break;
                default:
                    userNotification.setType(-1);   // invalid notification type
                    break;
            }

            mapper.save(userNotification);
            return null;
        }
    }

    private class DeleteNotificationAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            NotificationEnum notificationEnum = (NotificationEnum) bundle.get("NOTIFICATION_ENUM");

            if (notificationEnum != null) {
                DBUserNotification userNotification = new DBUserNotification();
                DynamoDBQueryExpression<DBUserNotification> queryExpression = null;
                Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
                switch (notificationEnum) {
                    case POST_COMMENT:
                        DBUserComment postComment = bundle.getParcelable("USER_COMMENT");
                        expressionAttributeValues.put(":type", new AttributeValue().withN("1"));
                        if (postComment != null) {
                            userNotification.setPostID(postComment.getPostID());
                            queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                                    .withIndexName("PostID-CommentID-index")
                                    .withHashKeyValues(userNotification)
                                    .withRangeKeyCondition("CommentID", new Condition()
                                            .withComparisonOperator(ComparisonOperator.EQ)
                                            .withAttributeValueList(new AttributeValue().withS(postComment.getCommentID())))
                                    .withFilterExpression("NotificationType = :type")
                                    .withExpressionAttributeValues(expressionAttributeValues)
                                    .withConsistentRead(false);
                        }
                        break;
                    case POST_FISTBUMP:
                        userNotification.setPostID(bundle.getString("POST_ID"));
                        expressionAttributeValues.put(":type", new AttributeValue().withN("2"));
                        queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                                .withIndexName("PostID-SenderNickname-index")
                                .withHashKeyValues(userNotification)
                                .withRangeKeyCondition("SenderNickname", new Condition()
                                        .withComparisonOperator(ComparisonOperator.EQ)
                                        .withAttributeValueList(new AttributeValue().withS(bundle.getString("SENDER_NICKNAME"))))
                                .withFilterExpression("NotificationType = :type")
                                .withExpressionAttributeValues(expressionAttributeValues)
                                .withConsistentRead(false);
                        break;
                    case COMMENT_FISTBUMP:
                        userNotification.setCommentID(bundle.getString("COMMENT_ID"));
                        queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                                .withIndexName("CommentID-SenderNickname-index")
                                .withHashKeyValues(userNotification)
                                .withRangeKeyCondition("SenderNickname", new Condition()
                                        .withComparisonOperator(ComparisonOperator.EQ)
                                        .withAttributeValueList(new AttributeValue().withS(bundle.getString("SENDER_NICKNAME"))))
                                .withConsistentRead(false);
                        break;
                }

                if (queryExpression != null) {
                    List<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);
                    // make sure only 1 entry is found
                    if (queryResults != null && queryResults.size() == 1) {
                        mapper.delete(queryResults.get(0));
                    }
                }
            }
            return null;
        }
    }

    private class BatchDeletePostNotifications extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost post = params[0];
            if (post != null) {
                DBUserNotification userNotification = new DBUserNotification();
                userNotification.setPostID(post.getPostID());
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                        .withIndexName("PostID-index")
                        .withHashKeyValues(userNotification)
                        .withConsistentRead(false);

                PaginatedQueryList<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);
                if (queryResults != null) {
                    for (DBUserNotification notification : queryResults) {
                        mapper.delete(notification);
                    }
                }
            }
            return null;
        }
    }

    private class BatchDeleteCommentNotifications extends AsyncTask<DBUserComment, Void, Void> {
        @Override
        protected Void doInBackground(DBUserComment... params) {
            DBUserComment comment = params[0];
            if (comment != null) {
                DBUserNotification userNotification = new DBUserNotification();
                userNotification.setCommentID(comment.getCommentID());
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                        .withIndexName("CommentID-index")
                        .withHashKeyValues(userNotification)
                        .withConsistentRead(false);

                PaginatedQueryList<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);
                if (queryResults != null) {
                    for (DBUserNotification notification : queryResults) {
                        mapper.delete(notification);
                    }
                }
            }
            return null;
        }
    }
}
