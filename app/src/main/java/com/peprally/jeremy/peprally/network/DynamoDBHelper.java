package com.peprally.jeremy.peprally.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.peprally.jeremy.peprally.messaging.ChatMessage;
import com.peprally.jeremy.peprally.messaging.Conversation;
import com.peprally.jeremy.peprally.db_models.DBUserConversation;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.db_models.DBUserNickname;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.NotificationEnum;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public DBUserConversation loadDBUserConversation(String conversationID) {
        return mapper.load(DBUserConversation.class, conversationID);
    }

    public void updateFirebaseInstanceID(String newInstanceID) {
        new UpdateFirebaseInstanceIDAsyncTask().execute(newInstanceID);
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
        new MakeNewDBUserNotificationAsyncTask().execute(bundle);
    }

    public void deleteCommentFistbumpNotification(NotificationEnum notificationEnum, String commentID, String senderNickname) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("NOTIFICATION_ENUM", notificationEnum);
        bundle.putString("COMMENT_ID", commentID);
        bundle.putString("SENDER_NICKNAME", senderNickname);
        new DeleteDBUserNotificationAsyncTask().execute(bundle);
    }

    public void deletePostFistbumpNotification(NotificationEnum notificationEnum, String postID, String senderNickname) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("NOTIFICATION_ENUM", notificationEnum);
        bundle.putString("POST_ID", postID);
        bundle.putString("SENDER_NICKNAME", senderNickname);
        new DeleteDBUserNotificationAsyncTask().execute(bundle);
    }

    public void deletePostCommentNotification(NotificationEnum notificationEnum, DBUserComment userComment) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("NOTIFICATION_ENUM", notificationEnum);
        bundle.putParcelable("USER_COMMENT", userComment);
        new DeleteDBUserNotificationAsyncTask().execute(bundle);
    }

    // Database create methods

    public void createNewConversation(String nickname1, String nickname2) {
        new CreateNewDBUserConversationAsyncTask().execute(nickname1, nickname2);
    }

    // Database delete methods

    public void batchDeleteCommentNotifications(DBUserComment userComment) {
        new BatchDeleteCommentDBUserNotificationsAsyncTask().execute(userComment);
    }

    public void batchDeletePostNotifications(DBUserPost userPost) {
        new BatchDeletePostDBUserNotificationsAsyncTask().execute(userPost);
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

    // Post/Comment Tasks
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

    // Notification Tasks
    private class MakeNewDBUserNotificationAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            UserProfileParcel userProfileParcel = bundle.getParcelable("USER_PROFILE_PARCEL");
            DBUserNotification userNotification = new DBUserNotification();
            // getting time stamp
            userNotification.setTimeInSeconds(Helpers.getTimestampMiliseconds());
            userNotification.setTimeStamp(Helpers.getTimestampString());
            // setting up new user notification
            userNotification.setNickname(bundle.getString("RECEIVER_NICKNAME")); // who the notification is going to
            if (userProfileParcel != null) {
                userNotification.setNicknameSender(userProfileParcel.getCurUserNickname());
                userNotification.setFacebookIDSender(userProfileParcel.getFacebookID());
            }

            NotificationEnum notificationType = NotificationEnum.fromInt(bundle.getInt("NOTIFICATION_TYPE"));
            if (notificationType != null) {
                switch (notificationType) {
                    case DIRECT_FISTBUMP:
                        userNotification.setNotificationType(notificationType.toInt());
                        userNotification.setNicknameSender(bundle.getString("SENDER_NICKNAME"));
                        DBUserProfile senderProfile = loadDBUserProfile(bundle.getString("SENDER_NICKNAME"));
                        userNotification.setFacebookIDSender(senderProfile.getFacebookId());
                        break;
                    case POST_COMMENT:
                        userNotification.setNotificationType(notificationType.toInt());
                        userNotification.setPostID(bundle.getString("POST_ID"));
                        userNotification.setCommentID(bundle.getString("COMMENT_ID"));
                        userNotification.setComment(bundle.getString("COMMENT"));
                        break;
                    case POST_FISTBUMP:
                        userNotification.setNotificationType(notificationType.toInt());
                        userNotification.setPostID(bundle.getString("POST_ID"));
                        break;
                    case COMMENT_FISTBUMP:
                        userNotification.setNotificationType(notificationType.toInt());
                        userNotification.setPostID(bundle.getString("POST_ID"));
                        userNotification.setCommentID(bundle.getString("COMMENT_ID"));
                        break;
                    default:
                        userNotification.setNotificationType(-1);   // invalid notification type
                        break;
                }
            }

            // set receiver profile's newNotification flag to true
            DBUserProfile receiverUserProfile = loadDBUserProfile(bundle.getString("RECEIVER_NICKNAME"));
            if (receiverUserProfile != null) {
                receiverUserProfile.setHasNewNotification(true);
                saveDBObject(receiverUserProfile);
            }

            saveDBObject(userNotification);
            return null;
        }
    }

    private class DeleteDBUserNotificationAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            NotificationEnum notificationType = (NotificationEnum) bundle.get("NOTIFICATION_ENUM");

            if (notificationType != null) {
                DBUserNotification userNotification = new DBUserNotification();
                DynamoDBQueryExpression<DBUserNotification> queryExpression = null;
                Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
                switch (notificationType) {
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
                                            .withAttributeValueList(new AttributeValue().withS(postComment.getCommentId())))
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

    private class BatchDeletePostDBUserNotificationsAsyncTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost post = params[0];
            if (post != null) {
                DBUserNotification userNotification = new DBUserNotification();
                userNotification.setPostID(post.getPostId());
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

    private class BatchDeleteCommentDBUserNotificationsAsyncTask extends AsyncTask<DBUserComment, Void, Void> {
        @Override
        protected Void doInBackground(DBUserComment... params) {
            DBUserComment comment = params[0];
            if (comment != null) {
                DBUserNotification userNotification = new DBUserNotification();
                userNotification.setCommentID(comment.getCommentId());
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

    // Messaging Tasks
    private class CreateNewDBUserConversationAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... nicknames) {
            DBUserProfile fistbumpedUserProfile1 = loadDBUserProfile(nicknames[0]);
            DBUserProfile fistbumpedUserProfile2 = loadDBUserProfile(nicknames[1]);
            if (fistbumpedUserProfile1 != null && fistbumpedUserProfile2 != null) {
                DBUserConversation newConversation = new DBUserConversation();
                String conversation_id = fistbumpedUserProfile1.getFacebookId() + "_" + fistbumpedUserProfile2.getFacebookId();
                newConversation.setConversationID(conversation_id);
                Long timeInSeconds = Helpers.getTimestampMiliseconds();
                newConversation.setTimeStampCreated(timeInSeconds);
                newConversation.setTimeStampLatest(timeInSeconds);
                Map<String, String> nicknameFacebookIDMap = new HashMap<>();
                nicknameFacebookIDMap.put(fistbumpedUserProfile1.getNickname(), fistbumpedUserProfile1.getFacebookId());
                nicknameFacebookIDMap.put(fistbumpedUserProfile2.getNickname(), fistbumpedUserProfile2.getFacebookId());
                newConversation.setConversation(new Conversation(conversation_id, new ArrayList<ChatMessage>(), nicknameFacebookIDMap));

                // append conversation_id to each user
                mapper.save(newConversation);
                fistbumpedUserProfile1.addConversationId(conversation_id);
                fistbumpedUserProfile2.addConversationId(conversation_id);
                mapper.save(fistbumpedUserProfile1);
                mapper.save(fistbumpedUserProfile2);
            }
            return null;
        }
    }

    // Other Tasks
    private class UpdateFirebaseInstanceIDAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String newInstanceID = strings[0];
            // Query for userProfile using cognitoID
            DBUserProfile userProfile = new DBUserProfile();
            userProfile.setCognitoId(credentialsProvider.getIdentityId());
            DynamoDBQueryExpression<DBUserProfile> queryExpression = new DynamoDBQueryExpression<DBUserProfile>()
                    .withIndexName("CognitoId-index")
                    .withHashKeyValues(userProfile)
                    .withConsistentRead(false);
            List<DBUserProfile> results = mapper.query(DBUserProfile.class, queryExpression);
            if (results != null && results.size() == 1) {
                userProfile = results.get(0);
                userProfile.setFCMInstanceId(newInstanceID);
            }
            return null;
        }
    }
}
