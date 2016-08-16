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
import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.custom.preferences.NotificationsPref;
import com.peprally.jeremy.peprally.db_models.DBUserFeedback;
import com.peprally.jeremy.peprally.custom.messaging.ChatMessage;
import com.peprally.jeremy.peprally.custom.messaging.Conversation;
import com.peprally.jeremy.peprally.db_models.DBUserConversation;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUsername;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
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

    // For AsyncTasks that need to have a callback function back in the activity once it finishes
    public interface AsyncTaskCallback {
        void onTaskDone();
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

    public DBUserProfile loadDBUserProfile(String postUsername) {
        return mapper.load(DBUserProfile.class, postUsername);
    }

    public DBPlayerProfile loadDBPlayerProfile(String playerTeam, Integer playerIndex) {
        return mapper.load(DBPlayerProfile.class, playerTeam, playerIndex);
    }

    public DBUserPost loadDBUserPost(String postUsername, Long timestampSeconds) {
        return mapper.load(DBUserPost.class, postUsername, timestampSeconds);
    }

    public DBUserPost loadDBUserPost(String PostId) {
        String postUsername = PostId.split("_")[0];
        Long timestampSeconds = Long.valueOf(PostId.split("_")[1]);
        return mapper.load(DBUserPost.class, postUsername, timestampSeconds);
    }

    public DBUsername loadDBUsername(String username) {
        return mapper.load(DBUsername.class, username);
    }

    public DBUserConversation loadDBUserConversation(String conversationID) {
        return mapper.load(DBUserConversation.class, conversationID);
    }

    public void updateFirebaseInstanceID(String newInstanceID) {
        new UpdateFirebaseInstanceIDAsyncTask().execute(newInstanceID);
    }

    // Database save methods

    public void updateUserEmailPreferences(String username, String email) {
        new UpdateDBUserProfileEmailAsyncTask().execute(username, email);
    }

    public void updateUserNotificationPreferences(String username,
                                                  boolean notifyDirectFistbumpPref,
                                                  boolean notifyPostFistbumpPref,
                                                  boolean notifyCommentFistbumpPref,
                                                  boolean notifyNewCommentPref,
                                                  boolean notifyDirectMessagePref) {
        new UpdateDBUserProfileNotificationPrefAsyncTask(username).execute(notifyDirectFistbumpPref,
                                                                    notifyPostFistbumpPref,
                                                                    notifyCommentFistbumpPref,
                                                                    notifyNewCommentPref,
                                                                    notifyDirectMessagePref);
    }

    public void incrementUserSentFistbumpsCount(String username) {
        new IncrementUserSentFistbumpsCountAsyncTask().execute(username);
    }

    public void decrementUserSentFistbumpsCount(String username) {
        new DecrementUserSentFistbumpsCountAsyncTask().execute(username);
    }

    public void incrementUserReceivedFistbumpsCount(String username) {
        new IncrementUserReceivedFistbumpsCountAsyncTask().execute(username);
    }

    public void decrementUserReceivedFistbumpsCount(String username) {
        new DecrementUserReceivedFistbumpsCountAsyncTask().execute(username);
    }

    // Database create methods
    public void addNewPostComment(Comment comment, AsyncTaskCallback taskCallback) {
        new AddNewPostCommentAsyncTask(taskCallback).execute(comment);
    }

    public void createNewNotification(Bundle bundle) {
        new CreateNewDBUserNotificationAsyncTask().execute(bundle);
    }

    public void createNewFeedback(Bundle bundle) {
        new CreateNewDBUserFeedbackAsyncTask().execute(bundle);
    }

    public void createNewConversation(String username1, String username2) {
        new CreateNewDBUserConversationAsyncTask().execute(username1, username2);
    }

    // Database delete methods
    public void deletePostComment(int position, DBUserPost userPost, AsyncTaskCallback taskCallback) {
        userPost.deleteCommentAt(position);
        new DeletePostCommentAsyncTask(taskCallback).execute(userPost);
    }

    public void deletePostFistbumpNotification(NotificationEnum notificationType, String PostId, String senderUsername) {
        Bundle bundle = new Bundle();
        bundle.putString("POST_ID", PostId);
        bundle.putString("SENDER_USERNAME", senderUsername);
        new DeleteDBUserNotificationAsyncTask(notificationType).execute(bundle);
    }

    public void deleteCommentFistbumpNotification(NotificationEnum notificationType, String commentID, String senderUsername) {
        Bundle bundle = new Bundle();
        bundle.putString("COMMENT_ID", commentID);
        bundle.putString("SENDER_USERNAME", senderUsername);
        new DeleteDBUserNotificationAsyncTask(notificationType).execute(bundle);
    }

    public void deletePostCommentNotification(NotificationEnum notificationType, Comment comment) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("COMMENT", comment);
        new DeleteDBUserNotificationAsyncTask(notificationType).execute(bundle);
    }

    public void deleteUserPost(DBUserPost userPost, AsyncTaskCallback taskCallback) {
        new DeleteDBUserPostAsyncTask(taskCallback).execute(userPost);
    }

    public void deleteUserAccount(UserProfileParcel userProfileParcel, AsyncTaskCallback taskCallback) {
        new DeleteDBUserProfileAsyncTask().execute(userProfileParcel);
        new DeleteDBUsernameAsyncTask().execute(userProfileParcel.getCurUsername());
        new BatchDeleteDBUserPostsAsyncTask(taskCallback).execute(userProfileParcel);
    }

    public void batchDeletePostNotifications(DBUserPost userPost) {
        new BatchDeletePostDBUserNotificationsAsyncTask().execute(userPost);
    }

    public void deleteConversation(Conversation conversation, AsyncTaskCallback taskCallback) {
        new DeleteDBUserConversationAsyncTask(taskCallback).execute(conversation);
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

    // UserProfile Tasks

    private class UpdateDBUserProfileEmailAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];
            String email = strings[1];
            DBUserProfile userProfile = loadDBUserProfile(username);
            if (userProfile != null) {
                userProfile.setEmail(email);
                saveDBObject(userProfile);
            }
            return null;
        }
    }

    private class UpdateDBUserProfileNotificationPrefAsyncTask extends AsyncTask<Boolean, Void, Void> {

        private String username;

        private UpdateDBUserProfileNotificationPrefAsyncTask(String username) {
            this.username = username;
        }

        @Override
        protected Void doInBackground(Boolean... notifyPrefs) {
            DBUserProfile userProfile = loadDBUserProfile(username);

            if (userProfile != null) {
                userProfile.setNotificationsPref(new NotificationsPref(
                        notifyPrefs[0],
                        notifyPrefs[1],
                        notifyPrefs[2],
                        notifyPrefs[3],
                        notifyPrefs[4]));
                mapper.save(userProfile);
            }

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private class DeleteDBUserProfileAsyncTask extends AsyncTask<UserProfileParcel, Void, Void> {
        @Override
        protected Void doInBackground(UserProfileParcel... userProfileParcels) {
            UserProfileParcel userProfileParcel = userProfileParcels[0];
            // First delete user profile and username
            DBUserProfile userProfile = loadDBUserProfile(userProfileParcel.getCurUsername());
            DBUsername username = loadDBUsername(userProfileParcel.getCurUsername());
            mapper.delete(username);
            mapper.delete(userProfile);

            // delete all posts
            if (userProfileParcel.getPostsCount() != null && userProfileParcel.getPostsCount() > 0) {
                DBUserPost userPost = new DBUserPost();
                userPost.setUsername(userProfileParcel.getCurUsername());
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserPost>()
                        .withHashKeyValues(userPost)
                        .withConsistentRead(true);

                PaginatedQueryList<DBUserPost> queryResults = mapper.query(DBUserPost.class, queryExpression);
                if (queryResults != null && queryResults.size() > 0) {
                    for (DBUserPost post : queryResults) {
                        mapper.delete(post);
                    }
                }
            }

            // delete all notification made to the deleted user
            DBUserNotification toUserNotification = new DBUserNotification();
            toUserNotification.setUsername(userProfileParcel.getCurUsername());
            DynamoDBQueryExpression queryExpressionToUserNotifications = new DynamoDBQueryExpression<DBUserNotification>()
                    .withHashKeyValues(toUserNotification)
                    .withConsistentRead(true);

            PaginatedQueryList<DBUserNotification> queryResultsToUser = mapper.query(DBUserNotification.class, queryExpressionToUserNotifications);
            if (queryResultsToUser != null && queryResultsToUser.size() > 0) {
                for (DBUserNotification notification : queryResultsToUser) {
                    mapper.delete(notification);
                }
            }

            // delete all notifications made by the deleted user
            DBUserNotification fromUserNotification = new DBUserNotification();
            fromUserNotification.setSenderUsername(userProfileParcel.getCurUsername());
            DynamoDBQueryExpression queryExpressionFromUserNotifications = new DynamoDBQueryExpression<DBUserNotification>()
                    .withHashKeyValues(fromUserNotification)
                    .withConsistentRead(false);

            PaginatedQueryList<DBUserNotification> queryResultsFromUser = mapper.query(DBUserNotification.class, queryExpressionFromUserNotifications);
            if (queryResultsFromUser != null && queryResultsFromUser.size() > 0) {
                for (DBUserNotification notification : queryResultsFromUser) {
                    mapper.delete(notification);
                }
            }

            return null;
        }
    }

    private class DeleteDBUsernameAsyncTask extends AsyncTask<String,  Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];
            DBUsername dbUsername = mapper.load(DBUsername.class, username);
            if (dbUsername != null)
                mapper.delete(dbUsername);
            return null;
        }
    }

    // Post/Comment Tasks
    private class IncrementUserSentFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... usernames) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, usernames[0]);
            userProfile.setSentFistbumpsCount(userProfile.getSentFistbumpsCount() + 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class DecrementUserSentFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... usernames) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, usernames[0]);
            userProfile.setSentFistbumpsCount(userProfile.getSentFistbumpsCount() - 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class IncrementUserReceivedFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... usernames) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, usernames[0]);
            userProfile.setReceivedFistbumpsCount(userProfile.getReceivedFistbumpsCount() + 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class DecrementUserReceivedFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... usernames) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, usernames[0]);
            userProfile.setReceivedFistbumpsCount(userProfile.getReceivedFistbumpsCount() - 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class AddNewPostCommentAsyncTask extends AsyncTask<Comment, Void, Void> {

        private AsyncTaskCallback taskCallback;

        private AddNewPostCommentAsyncTask(AsyncTaskCallback taskCallback) {
            this.taskCallback = taskCallback;
        }

        @Override
        protected Void doInBackground(Comment... comments) {
            Comment newComment = comments[0];
            DBUserPost parentPost = loadDBUserPost(newComment.getPostId());
            if (parentPost != null) {
                parentPost.addComment(newComment);
                mapper.save(parentPost);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            taskCallback.onTaskDone();
        }
    }

    private class DeletePostCommentAsyncTask extends AsyncTask<DBUserPost, Void, Void> {

        private AsyncTaskCallback taskCallback;

        private DeletePostCommentAsyncTask(AsyncTaskCallback taskCallback) {
            this.taskCallback = taskCallback;
        }

        @Override
        protected Void doInBackground(DBUserPost ... userPosts) {
            DBUserPost parentPost = userPosts[0];
            if (parentPost != null) {
                mapper.save(parentPost);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            taskCallback.onTaskDone();
        }
    }

    private class DeleteDBUserPostAsyncTask extends AsyncTask<DBUserPost, Void, Void> {

        private AsyncTaskCallback taskCallback;

        private DeleteDBUserPostAsyncTask(AsyncTaskCallback taskCallback) {
            this.taskCallback = taskCallback;
        }

        @Override
        protected Void doInBackground(DBUserPost... dbUserPosts) {
            DBUserPost userPost = dbUserPosts[0];
            mapper.delete(userPost);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            taskCallback.onTaskDone();
        }
    }

    @SuppressWarnings("unchecked")
    private class BatchDeleteDBUserPostsAsyncTask extends AsyncTask<UserProfileParcel, Void, Void> {

        private AsyncTaskCallback taskCallback;

        private BatchDeleteDBUserPostsAsyncTask(AsyncTaskCallback taskCallback) {
            this.taskCallback = taskCallback;
        }

        @Override
        protected Void doInBackground(UserProfileParcel... userProfileParcels) {
            UserProfileParcel userProfileParcel = userProfileParcels[0];
            if (userProfileParcel.getPostsCount() != null && userProfileParcel.getPostsCount() > 0) {
                String username = userProfileParcel.getCurUsername();
                DBUserPost userPost = new DBUserPost();
                userPost.setUsername(username);
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserPost>()
                        .withHashKeyValues(userPost)
                        .withConsistentRead(true);

                PaginatedQueryList<DBUserPost> queryResults = mapper.query(DBUserPost.class, queryExpression);
                if (queryResults != null && queryResults.size() > 0) {
                    for (DBUserPost post : queryResults) {
                        mapper.delete(post);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            taskCallback.onTaskDone();
        }
    }

    // Notification Tasks
    private class CreateNewDBUserNotificationAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            UserProfileParcel userProfileParcel = bundle.getParcelable("USER_PROFILE_PARCEL");
            DBUserNotification userNotification = new DBUserNotification();
            // getting time stamp
            userNotification.setTimeInSeconds(Helpers.getTimestampSeconds());
            // setting up new user notification
            userNotification.setUsername(bundle.getString("RECEIVER_USERNAME")); // who the notification is going to
            if (userProfileParcel != null) {
                userNotification.setSenderUsername(userProfileParcel.getCurUsername());
                userNotification.setFacebookIdSender(userProfileParcel.getFacebookID());
            }

            NotificationEnum notificationType = NotificationEnum.fromInt(bundle.getInt("NOTIFICATION_TYPE"));
            switch (notificationType) {
                case DIRECT_FISTBUMP:
                    userNotification.setNotificationType(notificationType.toInt());
                    userNotification.setSenderUsername(bundle.getString("SENDER_USERNAME"));
                    DBUserProfile senderProfile = loadDBUserProfile(bundle.getString("SENDER_USERNAME"));
                    userNotification.setFacebookIdSender(senderProfile.getFacebookId());
                    break;
                case POST_COMMENT:
                    userNotification.setNotificationType(notificationType.toInt());
                    userNotification.setPostId(bundle.getString("POST_ID"));
                    userNotification.setCommentId(bundle.getString("COMMENT_ID"));
                    userNotification.setComment(bundle.getString("COMMENT"));
                    break;
                case POST_FISTBUMP:
                    userNotification.setNotificationType(notificationType.toInt());
                    userNotification.setPostId(bundle.getString("POST_ID"));
                    break;
                case COMMENT_FISTBUMP:
                    userNotification.setNotificationType(notificationType.toInt());
                    userNotification.setPostId(bundle.getString("POST_ID"));
                    userNotification.setCommentId(bundle.getString("COMMENT_ID"));
                    break;
                default:
                    userNotification.setNotificationType(-1);   // invalid notification type
                    break;
            }

            // set receiver profile's newNotification flag to true
            DBUserProfile receiverUserProfile = loadDBUserProfile(bundle.getString("RECEIVER_USERNAME"));
            if (receiverUserProfile != null) {
                receiverUserProfile.setHasNewNotification(true);
                saveDBObject(receiverUserProfile);
            }

            saveDBObject(userNotification);
            return null;
        }
    }

    private class DeleteDBUserNotificationAsyncTask extends AsyncTask<Bundle, Void, Void> {
        private NotificationEnum notificationType;
        private  DeleteDBUserNotificationAsyncTask (NotificationEnum notificationType) {
            this.notificationType = notificationType;
        }
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            DBUserNotification userNotification = new DBUserNotification();
            DynamoDBQueryExpression<DBUserNotification> queryExpression = null;
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            switch (notificationType) {
                case POST_COMMENT: {
                    Comment postComment = bundle.getParcelable("COMMENT");
                    expressionAttributeValues.put(":type", new AttributeValue().withN(String.valueOf(notificationType.toInt())));
                    if (postComment != null) {
                        userNotification.setPostId(postComment.getPostId());
                        queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                                .withIndexName("PostId-CommentID-index")
                                .withHashKeyValues(userNotification)
                                .withRangeKeyCondition("CommentID", new Condition()
                                        .withComparisonOperator(ComparisonOperator.EQ)
                                        .withAttributeValueList(new AttributeValue().withS(postComment.getCommentId())))
                                .withFilterExpression("NotificationType = :type")
                                .withExpressionAttributeValues(expressionAttributeValues)
                                .withConsistentRead(false);
                    }
                    break;
                }
                case POST_FISTBUMP: {
                    userNotification.setPostId(bundle.getString("POST_ID"));
                    expressionAttributeValues.put(":type", new AttributeValue().withN("2"));
                    queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                            .withIndexName("PostId-SenderUsername-index")
                            .withHashKeyValues(userNotification)
                            .withRangeKeyCondition("SenderUsername", new Condition()
                                    .withComparisonOperator(ComparisonOperator.EQ)
                                    .withAttributeValueList(new AttributeValue().withS(bundle.getString("SENDER_USERNAME"))))
                            .withFilterExpression("NotificationType = :type")
                            .withExpressionAttributeValues(expressionAttributeValues)
                            .withConsistentRead(false);
                    break;
                }
                case COMMENT_FISTBUMP: {
                    userNotification.setCommentId(bundle.getString("COMMENT_ID"));
                    queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                            .withIndexName("CommentId-SenderUsername-index")
                            .withHashKeyValues(userNotification)
                            .withRangeKeyCondition("SenderUsername", new Condition()
                                    .withComparisonOperator(ComparisonOperator.EQ)
                                    .withAttributeValueList(new AttributeValue().withS(bundle.getString("SENDER_USERNAME"))))
                            .withConsistentRead(false);
                    break;
                }
            }

            if (queryExpression != null) {
                List<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);
                // make sure only 1 entry is found
                if (queryResults != null && queryResults.size() == 1) {
                    mapper.delete(queryResults.get(0));
                }
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private class BatchDeletePostDBUserNotificationsAsyncTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost post = params[0];
            if (post != null) {
                DBUserNotification userNotification = new DBUserNotification();
                userNotification.setPostId(post.getPostId());
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                        .withIndexName("PostId-index")
                        .withHashKeyValues(userNotification)
                        .withConsistentRead(false);

                PaginatedQueryList<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);
                if (queryResults != null && queryResults.size() > 0) {
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
        protected Void doInBackground(String... usernames) {
            DBUserProfile fistbumpedUserProfile1 = loadDBUserProfile(usernames[0]);
            DBUserProfile fistbumpedUserProfile2 = loadDBUserProfile(usernames[1]);
            if (fistbumpedUserProfile1 != null && fistbumpedUserProfile2 != null) {
                DBUserConversation newConversation = new DBUserConversation();
                String conversation_id = fistbumpedUserProfile1.getFacebookId() + "_" + fistbumpedUserProfile2.getFacebookId();
                newConversation.setConversationID(conversation_id);
                Long timeInSeconds = Helpers.getTimestampSeconds();
                newConversation.setTimeStampCreated(timeInSeconds);
                newConversation.setTimeStampLatest(timeInSeconds);
                Map<String, String> usernameFacebookIDMap = new HashMap<>();
                usernameFacebookIDMap.put(fistbumpedUserProfile1.getUsername(), fistbumpedUserProfile1.getFacebookId());
                usernameFacebookIDMap.put(fistbumpedUserProfile2.getUsername(), fistbumpedUserProfile2.getFacebookId());
                newConversation.setConversation(new Conversation(conversation_id, new ArrayList<ChatMessage>(), usernameFacebookIDMap));

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

    private class CreateNewDBUserFeedbackAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... bundles) {
            Bundle bundle = bundles[0];
            DBUserFeedback userFeedback = new DBUserFeedback(bundle.getString("USERNAME"), bundle.getLong("TIMESTAMP"));
            userFeedback.setFeedbackType(bundle.getInt("FEEDBACK_TYPE"));
            userFeedback.setFeedback(bundle.getString("FEEDBACK"));
            userFeedback.setPlatform("Android");
            mapper.save(userFeedback);
            return null;
        }
    }

    private class DeleteDBUserConversationAsyncTask extends AsyncTask<Conversation, Void, Void> {
        private AsyncTaskCallback taskCallback;
        private DeleteDBUserConversationAsyncTask(AsyncTaskCallback taskCallback) {
            this.taskCallback = taskCallback;
        }
        @Override
        protected Void doInBackground(Conversation... conversations) {
            Conversation conversation = conversations[0];
            DBUserConversation userConversation = loadDBUserConversation(conversation.getConversationID());
            if (userConversation != null) {
                for (String username : conversation.getUsernameFacebookIDMap().keySet()) {
                    DBUserProfile userProfile = loadDBUserProfile(username);
                    if (userProfile != null) {
                        userProfile.removeConversationId(conversation.getConversationID());
                        userProfile.removeUsersDirectFistbumpReceived(conversation.getRecipientUsername(userProfile.getUsername()));
                        userProfile.removeUsersDirectFistbumpSent(conversation.getRecipientUsername(userProfile.getUsername()));
                        mapper.save(userProfile);
                    }
                }
                mapper.delete(userConversation);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            taskCallback.onTaskDone();
        }
    }

    // Other Tasks
    private class UpdateFirebaseInstanceIDAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String newInstanceID = strings[0];
            // Query for userProfile using cognitoID
            try {
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
            } catch (Exception e) { e.printStackTrace(); }

            return null;
        }
    }
}
