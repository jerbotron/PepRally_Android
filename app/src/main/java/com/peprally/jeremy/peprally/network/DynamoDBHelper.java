package com.peprally.jeremy.peprally.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.custom.Feedback;
import com.peprally.jeremy.peprally.custom.preferences.NotificationsPref;
import com.peprally.jeremy.peprally.custom.messaging.ChatMessage;
import com.peprally.jeremy.peprally.custom.messaging.Conversation;
import com.peprally.jeremy.peprally.db_models.DBUserConversation;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUsername;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.FeedbackEnum;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peprally.jeremy.peprally.utils.Constants.COGNITO_REGION;
import static com.peprally.jeremy.peprally.utils.Constants.IDENTITY_POOL_ID;

public class DynamoDBHelper {

    // AWS Variables
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    public DynamoDBHelper(Context callingContext) {
        refresh(callingContext);
    }

    public void refresh(Context callingContext) {
        credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,                                         // Context
                IDENTITY_POOL_ID,                 // Identity Pool ID
                COGNITO_REGION                    // Region
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

    // For AsyncTasks that need to have a callback function once it finishes
    public interface AsyncTaskCallback {
        void onTaskDone();
    }

    // For AsyncTasks that need to have a callback function once it finishes and is also expecting
    // a return paramter
    public interface AsyncTaskCallbackWithReturnObject {
        void onTaskDone(Object object);
    }

    /***********************************************************************************************
     *************************************** DATABASE METHODS **************************************
     **********************************************************************************************/

    /**
     * Database save methods
     */
    public void saveDBObject(Object object) {
        mapper.save(object);
    }

    public void saveDBObjectAsync(Object object) {
        new SaveDBObjectAsyncTask().execute(object);
    }

    public void saveIfDBUserPostExists(DBUserPost userPost) {
        new SaveDBUserPostIfExistsAsyncTask().execute(userPost);
    }

    public void saveIfDBUserPostAndCommentExists(DBUserPost userPost, Comment comment, AsyncTaskCallback taskCallback) {
        new SaveDBUserPostAndCommentIfExistsAsyncTask(userPost, comment, taskCallback).execute();
    }

    public void doActionIfDBUserPostAndCommentExists(DBUserPost userPost, Comment comment, AsyncTaskCallbackWithReturnObject taskCallback) {
        new DoActionIfDBUserPostAndCommentIfExistsAsyncTask(userPost, comment, taskCallback).execute();
    }

    /**
     * Database load methods
     */
    public DBUserProfile loadDBUserProfile(String username) {
        if (username == null || username.isEmpty())
            return null;
        return mapper.load(DBUserProfile.class, username);
    }

    public DBPlayerProfile loadDBPlayerProfile(String playerTeam, Integer playerIndex) {
        return mapper.load(DBPlayerProfile.class, playerTeam, playerIndex);
    }

    public DBUserPost loadDBUserPost(String postUsername, Long timestampSeconds) {
        return mapper.load(DBUserPost.class, postUsername, timestampSeconds);
    }

    public DBUserPost loadDBUserPost(String PostId) {
        String [] splitArray = PostId.split("_");
        String postUsername = TextUtils.join("_", Arrays.copyOfRange(splitArray, 0, splitArray.length - 1));
        Long timestampSeconds = Long.valueOf(splitArray[splitArray.length - 1]);
        return mapper.load(DBUserPost.class, postUsername, timestampSeconds);
    }

    public DBUsername loadDBUsername(String username) {
        return mapper.load(DBUsername.class, username);
    }

    public DBUserConversation loadDBUserConversation(String conversationID) {
        return mapper.load(DBUserConversation.class, conversationID);
    }

    /**
     * Database update methods
     */
    public void updateFirebaseInstanceID(String newInstanceID) {
        new UpdateFirebaseInstanceIdAsyncTask().execute(newInstanceID);
    }

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

    private void updateUserLastLoggedInDate(String username) {
        new UpdateUserLastLoggedInDateAsyncTask().execute(username);
    }

    /**
     * Database create new database entry methods
     */
    public void createNewPostComment(Bundle bundle, AsyncTaskCallback taskCallback) {
        new CreateNewPostCommentAsyncTask(taskCallback).execute(bundle);
    }

    public void createNewNotification(Bundle bundle, AsyncTaskCallbackWithReturnObject taskCallback) {
        new CreateNewDBUserNotificationAsyncTask(NotificationEnum.fromInt(bundle.getInt("NOTIFICATION_TYPE")), taskCallback).execute(bundle);
    }

    public void createNewConversation(UserProfileParcel userProfileParcel, AsyncTaskCallbackWithReturnObject callbackWithReturnObject) {
        new CreateNewDBUserConversationAsyncTask(callbackWithReturnObject).execute(userProfileParcel.getCurrentUsername(), userProfileParcel.getProfileUsername());
    }

    public void createNewFeedback(Feedback feedback) {
        new CreateNewDBUserFeedbackAsyncTask(feedback).execute();
    }


    /**
     * Databse delete methods
     */
    public void deletePostComment(int position, DBUserPost userPost, AsyncTaskCallback taskCallback) {
        userPost.deleteCommentAt(position);
        new DeletePostCommentAsyncTask(taskCallback).execute(userPost);
    }

    public void deleteUserPost(DBUserPost userPost, AsyncTaskCallback taskCallback) {
        new DeleteDBUserPostAsyncTask(taskCallback).execute(userPost);
    }

    public void deleteConversation(Conversation conversation, AsyncTaskCallback taskCallback) {
        for (String username : conversation.getUsernameFacebookIdMap().keySet()) {
            Bundle bundle = new Bundle();
            bundle.putString("SENDER_USERNAME", username);
            // don't need a task call back if conversation doesn't exist anymore (taskCallBack = null)
            new DeleteDBUserNotificationAsyncTask(NotificationEnum.DIRECT_FISTBUMP, null).execute(bundle);
        }
        new DeleteDBUserConversationAsyncTask(taskCallback).execute(conversation);
    }

    public void deletePostFistbumpNotification(String PostId,
                                               String senderUsername,
                                               String receiverUsername,
                                               AsyncTaskCallbackWithReturnObject taskCallback) {
        Bundle bundle = new Bundle();
        bundle.putString("POST_ID", PostId);
        bundle.putString("SENDER_USERNAME", senderUsername);
        bundle.putString("RECEIVER_USERNAME", receiverUsername);
        new DeleteDBUserNotificationAsyncTask(NotificationEnum.POST_FISTBUMP, taskCallback).execute(bundle);
    }

    public void deleteCommentFistbumpNotification(String postId,
                                                  String commentId,
                                                  String senderUsername,
                                                  String receiverUsername,
                                                  AsyncTaskCallbackWithReturnObject taskCallback) {
        Bundle bundle = new Bundle();
        bundle.putString("POST_ID", postId);
        bundle.putString("COMMENT_ID", commentId);
        bundle.putString("SENDER_USERNAME", senderUsername);
        bundle.putString("RECEIVER_USERNAME", receiverUsername);
        new DeleteDBUserNotificationAsyncTask(NotificationEnum.COMMENT_FISTBUMP, taskCallback).execute(bundle);
    }

    public void deletePostCommentNotification(String postId,
                                              String commentId,
                                              AsyncTaskCallbackWithReturnObject taskCallback) {
        Bundle bundle = new Bundle();
        bundle.putString("POST_ID", postId);
        bundle.putString("COMMENT_ID", commentId);
        new DeleteDBUserNotificationAsyncTask(NotificationEnum.POST_COMMENT, taskCallback).execute(bundle);
    }

    public void deleteUserAccount(UserProfileParcel userProfileParcel, AsyncTaskCallback taskCallback) {
        if (userProfileParcel.getPostsCount() != null && userProfileParcel.getPostsCount() > 0) {
            new BatchDeleteDBUserPostsAsyncTask().execute(userProfileParcel.getCurrentUsername());
        }
        // delete profile last, along with username, notifications and conversations
        new DeleteDBUserProfileAsyncTask(taskCallback).execute(userProfileParcel);
    }

    /**
     * Database batch delete methods
     */
    public void batchDeletePostNotifications(DBUserPost userPost) {
        new BatchDeletePostDBUserNotificationsAsyncTask().execute(userPost);
    }

    public void batchDeleteCommentFistbumpNotifications(NotificationEnum notificationType, Comment userComment) {
        new BatchDeleteCommentDBUserNotificationsAsyncTask(notificationType).execute(userComment);
    }

    /**
     * DynamoDB helper methods
     */
    public void updateFistbumpsCount(DBUserProfile senderProfile, DBUserProfile receiverProfile, boolean fistbumpSent) {
        if (fistbumpSent) {
            senderProfile.setSentFistbumpsCount(senderProfile.getSentFistbumpsCount() + 1);
            receiverProfile.setReceivedFistbumpsCount(receiverProfile.getReceivedFistbumpsCount() + 1);
        } else {
            senderProfile.setSentFistbumpsCount(senderProfile.getSentFistbumpsCount() - 1);
            receiverProfile.setReceivedFistbumpsCount(receiverProfile.getReceivedFistbumpsCount() - 1);
        }
        mapper.save(senderProfile);
        mapper.save(receiverProfile);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/

    /**
     * Dynamodb helper async tasks
     */
    private class SaveDBObjectAsyncTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            mapper.save(params[0]);
            return null;
        }
    }

    /**
     * User profile async tasks
     */
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

    private class UpdateUserLastLoggedInDateAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];
            DBUserProfile userProfile = loadDBUserProfile(username);
            userProfile.setDateLastLoggedIn(Helpers.getTimestampString());
            userProfile.setTimestampLastLoggedIn(Helpers.getTimestampSeconds());
            mapper.save(userProfile);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private class DeleteDBUserProfileAsyncTask extends AsyncTask<UserProfileParcel, Void, Void> {

        private AsyncTaskCallback taskCallback;

        private DeleteDBUserProfileAsyncTask(AsyncTaskCallback taskCallback) {
            this.taskCallback = taskCallback;
        }

        @Override
        protected Void doInBackground(UserProfileParcel... userProfileParcels) {
            UserProfileParcel userProfileParcel = userProfileParcels[0];
            // First delete user profile and username
            DBUserProfile userProfile = loadDBUserProfile(userProfileParcel.getCurrentUsername());

            if (userProfile != null) {
                DBUsername username = loadDBUsername(userProfileParcel.getCurrentUsername());
                if (username != null)
                    mapper.delete(username);

                // delete all notification made to the deleted user
                DBUserNotification toUserNotification = new DBUserNotification();
                toUserNotification.setUsername(userProfileParcel.getCurrentUsername());
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
                fromUserNotification.setSenderUsername(userProfileParcel.getCurrentUsername());
                DynamoDBQueryExpression queryExpressionFromUserNotifications = new DynamoDBQueryExpression<DBUserNotification>()
                        .withHashKeyValues(fromUserNotification)
                        .withConsistentRead(false);

                // Delete all user conversations:
                if (userProfile.getConversationIds() != null) {
                    for (String convoId : userProfile.getConversationIds()) {
                        DBUserConversation userConversation = loadDBUserConversation(convoId);
                        if (userConversation != null) {
                            mapper.delete(userConversation);
                        }
                    }
                }

                PaginatedQueryList<DBUserNotification> queryResultsFromUser = mapper.query(DBUserNotification.class, queryExpressionFromUserNotifications);
                if (queryResultsFromUser != null && queryResultsFromUser.size() > 0) {
                    for (DBUserNotification notification : queryResultsFromUser) {
                        mapper.delete(notification);
                    }
                }

                // delete profile last
                mapper.delete(userProfile);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            taskCallback.onTaskDone();
        }
    }

    /**
     * User post/comments async tasks
     */
    private class SaveDBUserPostIfExistsAsyncTask extends AsyncTask<DBUserPost, Void, Boolean> {
        private AsyncTaskCallback postDeletedTaskCallback;

        private SaveDBUserPostIfExistsAsyncTask() {
            this.postDeletedTaskCallback = null;
        }

        private SaveDBUserPostIfExistsAsyncTask(AsyncTaskCallback taskCallback) {
            this.postDeletedTaskCallback = taskCallback;
        }

        @Override
        protected Boolean doInBackground(DBUserPost... dbUserPosts) {
            DBUserPost userPost = dbUserPosts[0];
            // save the input userPost if we can query it
            DBUserPost userPostExisting = loadDBUserPost(userPost.getPostId());
            if (userPostExisting != null) {
                mapper.save(userPost);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean saveSuccess) {
            super.onPostExecute(saveSuccess);
            if (postDeletedTaskCallback != null && !saveSuccess) {
                postDeletedTaskCallback.onTaskDone();   // if post was not found
            }
        }
    }

    private class SaveDBUserPostAndCommentIfExistsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private AsyncTaskCallback taskCallback;
        private DBUserPost userPost;
        private Comment comment;

        private SaveDBUserPostAndCommentIfExistsAsyncTask(DBUserPost userPost,
                                                Comment comment,
                                                AsyncTaskCallback taskCallback) {
            this.userPost = userPost;
            this.comment = comment;
            this.taskCallback = taskCallback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // save the input userPost if we can query it
            DBUserPost userPostExisting = loadDBUserPost(userPost.getPostId());
            if (userPostExisting != null) {
                if (userPostExisting.hasComment(comment.getCommentId())) {
                    mapper.save(userPost);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean saveSuccess) {
            super.onPostExecute(saveSuccess);
            if (taskCallback != null && !saveSuccess) {
                taskCallback.onTaskDone();   // if post was not found
            }
        }
    }

    private class DoActionIfDBUserPostAndCommentIfExistsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private AsyncTaskCallbackWithReturnObject taskCallback;
        private DBUserPost userPost;
        private Comment comment;
        private boolean isPostDeleted;

        private DoActionIfDBUserPostAndCommentIfExistsAsyncTask(DBUserPost userPost,
                                                                Comment comment,
                                                                AsyncTaskCallbackWithReturnObject taskCallback) {
            this.userPost = userPost;
            this.comment = comment;
            this.taskCallback = taskCallback;
            this.isPostDeleted =  true;     // assume that post was deleted
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // save the input userPost if we can query it
            DBUserPost userPostExisting = loadDBUserPost(userPost.getPostId());
            if (userPostExisting != null) {
                isPostDeleted = false;  // post found
                // if not looking for comment or found comment, return true
                if (comment == null || userPostExisting.hasComment(comment.getCommentId())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean postAndCommentFound) {
            super.onPostExecute(postAndCommentFound);
            Bundle bundle = new Bundle();
            bundle.putBoolean("POST_AND_COMMENT_EXISTS", postAndCommentFound);
            if (!postAndCommentFound)
                bundle.putBoolean("IS_POST_DELETED", isPostDeleted);

            if (taskCallback != null) {
                taskCallback.onTaskDone(bundle);   // if post and comment was found
            }
        }
    }

    private class CreateNewPostCommentAsyncTask extends AsyncTask<Bundle, Void, Void> {

        private AsyncTaskCallback taskCallback;

        private CreateNewPostCommentAsyncTask(AsyncTaskCallback taskCallback) {
            this.taskCallback = taskCallback;
        }

        @Override
        protected Void doInBackground(Bundle... bundles) {
            Bundle bundle = bundles[0];

            DBUserProfile commentUserProfile = loadDBUserProfile(bundle.getString("COMMENT_USERNAME"));
            if (commentUserProfile != null) {
                Comment newComment = new Comment(
                        bundle.getString("POST_ID"),
                        bundle.getString("COMMENT_ID"),
                        bundle.getString("COMMENT_USERNAME"),
                        commentUserProfile.getFirstname(),
                        bundle.getString("POST_USERNAME"),
                        commentUserProfile.getFacebookId(),
                        bundle.getString("COMMENT_TEXT"),
                        bundle.getLong("TIMESTAMP"),
                        0);
                DBUserPost parentPost = loadDBUserPost(newComment.getPostId());
                if (parentPost != null) {
                    parentPost.addComment(newComment);
                    mapper.save(parentPost);
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
            DBUserProfile userProfile = loadDBUserProfile(userPost.getUsername());
            if (userProfile != null) {
                userProfile.setPostsCount(userProfile.getPostsCount() - 1);
                mapper.save(userProfile);
            }
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
    private class BatchDeleteDBUserPostsAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];

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
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    /**
     * Notifications async tasks
     */
    private class CreateNewDBUserNotificationAsyncTask extends AsyncTask<Bundle, Void, Boolean> {
        private NotificationEnum notificationType;
        private boolean isPostDeleted;
        private AsyncTaskCallbackWithReturnObject taskCallback;
        private CreateNewDBUserNotificationAsyncTask(NotificationEnum notificationType,
                                                     AsyncTaskCallbackWithReturnObject taskCallback) {
            this.notificationType = notificationType;
            this.taskCallback = taskCallback;
            this.isPostDeleted = true;  // assume that if a notification wasn't created, it was because the post got deleted
        }
        @Override
        protected Boolean doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            // initialize bundle contents
            UserProfileParcel userProfileParcel = bundle.getParcelable("USER_PROFILE_PARCEL");
            String receiverUsername = bundle.getString("RECEIVER_USERNAME");

            if (userProfileParcel != null) {
                // get sender/receiver profile
                DBUserProfile senderProfile = loadDBUserProfile(userProfileParcel.getCurrentUsername());
                DBUserProfile receiverProfile = loadDBUserProfile(receiverUsername);
                // initialize new notification object
                DBUserNotification userNotification = new DBUserNotification();
                userNotification.setNotificationType(notificationType.toInt());
                userNotification.setUsername(receiverUsername); // who the notification is going to
                userNotification.setSenderUsername(userProfileParcel.getCurrentUsername());
                userNotification.setFacebookIdSender(senderProfile.getFacebookId());
                userNotification.setTimestampSeconds(Helpers.getTimestampSeconds());

                switch (notificationType) {
                    case POST_COMMENT: {
                        // check if post exists
                        if (loadDBUserPost(bundle.getString("POST_ID")) == null) return false;
                        userNotification.setPostId(bundle.getString("POST_ID"));
                        userNotification.setCommentId(bundle.getString("COMMENT_ID"));
                        userNotification.setComment(bundle.getString("COMMENT"));
                        break;
                    }
                    case POST_FISTBUMP: {
                        // check if post exists
                        if (loadDBUserPost(bundle.getString("POST_ID")) == null) return false;
                        userNotification.setPostId(bundle.getString("POST_ID"));
                        // Update sent/received fistbumps of sender and receiver respectively
                        updateFistbumpsCount(senderProfile, receiverProfile, true);
                        break;
                    }
                    case COMMENT_FISTBUMP: {
                        // check if post exists
                        DBUserPost userPost = loadDBUserPost(bundle.getString("POST_ID"));
                        if (userPost == null) return false;
                        else {
                            // check if comment exists
                            if (!userPost.hasComment(bundle.getString("COMMENT_ID"))) {
                                isPostDeleted = false;
                                return false;
                            }
                        }
                        userNotification.setPostId(bundle.getString("POST_ID"));
                        userNotification.setCommentId(bundle.getString("COMMENT_ID"));
                        // Update sent/received fistbumps of sender and receiver respectively
                        updateFistbumpsCount(senderProfile, receiverProfile, true);
                        break;
                    }
                }

                // set receiver profile's newNotification flag to true
                DBUserProfile receiverUserProfile = loadDBUserProfile(bundle.getString("RECEIVER_USERNAME"));
                if (receiverUserProfile != null) {
                    receiverUserProfile.setHasNewNotification(true);
                    saveDBObject(receiverUserProfile);
                }

                saveDBObject(userNotification);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean notificationCreated) {
            super.onPostExecute(notificationCreated);
            if (taskCallback != null) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("TASK_SUCCESS", notificationCreated);
                bundle.putBoolean("IS_POST_DELETED", isPostDeleted);
                taskCallback.onTaskDone(bundle);
            }
        }
    }

    private class DeleteDBUserNotificationAsyncTask extends AsyncTask<Bundle, Void, Boolean> {
        private NotificationEnum notificationType;
        private AsyncTaskCallbackWithReturnObject taskCallback;
        private boolean isPostDeleted;

        private  DeleteDBUserNotificationAsyncTask (NotificationEnum notificationType,
                                                    AsyncTaskCallbackWithReturnObject taskCallback) {
            this.notificationType = notificationType;
            this.taskCallback = taskCallback;
            this.isPostDeleted = true;     // assume that in the case where the querying object is already deleted, the object is a post
        }
        @Override
        protected Boolean doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            DBUserNotification userNotification = new DBUserNotification();
            DynamoDBQueryExpression<DBUserNotification> queryExpression = null;
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":type", new AttributeValue().withN(String.valueOf(notificationType.toInt())));

            if (notificationType == NotificationEnum.DIRECT_FISTBUMP) {
                // don't need to care about if conversation still exists
                userNotification.setUsername(bundle.getString("SENDER_USERNAME"));
                queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                        .withHashKeyValues(userNotification)
                        .withFilterExpression("NotificationType = :type")
                        .withExpressionAttributeValues(expressionAttributeValues)
                        .withConsistentRead(false);
            } else if (notificationType == NotificationEnum.POST_COMMENT
                    || notificationType == NotificationEnum.POST_FISTBUMP
                    || notificationType == NotificationEnum.COMMENT_FISTBUMP) {

                // check if post exists
                DBUserPost userPost = loadDBUserPost(bundle.getString("POST_ID"));
                if (userPost == null) {
                    Log.d("DH: ", "not here ");
                    return false;
                }

                if (notificationType == NotificationEnum.POST_COMMENT) {
                    userNotification.setPostId(bundle.getString("POST_ID"));
                    queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                            .withIndexName("PostId-CommentId-index")
                            .withHashKeyValues(userNotification)
                            .withRangeKeyCondition("CommentId", new Condition()
                                    .withComparisonOperator(ComparisonOperator.EQ)
                                    .withAttributeValueList(new AttributeValue().withS(bundle.getString("COMMENT_ID"))))
                            .withFilterExpression("NotificationType = :type")
                            .withExpressionAttributeValues(expressionAttributeValues)
                            .withConsistentRead(false);
                } else {
                    String senderUsername = bundle.getString("SENDER_USERNAME");
                    String receiverUsername = bundle.getString("RECEIVER_USERNAME");

                    // Update sent/received fistbumps of sender and receiver respectively
                    DBUserProfile senderProfile = loadDBUserProfile(senderUsername);
                    DBUserProfile receiverProfile = loadDBUserProfile(receiverUsername);
                    updateFistbumpsCount(senderProfile, receiverProfile, false);

                    if (notificationType == NotificationEnum.POST_FISTBUMP) {
                        userNotification.setPostId(bundle.getString("POST_ID"));
                        queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                                .withIndexName("PostId-SenderUsername-index")
                                .withHashKeyValues(userNotification)
                                .withRangeKeyCondition("SenderUsername", new Condition()
                                        .withComparisonOperator(ComparisonOperator.EQ)
                                        .withAttributeValueList(new AttributeValue().withS(senderUsername)))
                                .withFilterExpression("NotificationType = :type")
                                .withExpressionAttributeValues(expressionAttributeValues)
                                .withConsistentRead(false);
                    } else if (notificationType == NotificationEnum.COMMENT_FISTBUMP) {
                        Log.d("DH: ", "comments = " + userPost.getComments().toString());
                        // check if comment exists
                        if (!userPost.hasComment(bundle.getString("COMMENT_ID"))) {
                            isPostDeleted = false;
                            return false;
                        }
                        userNotification.setCommentId(bundle.getString("COMMENT_ID"));
                        queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                                .withIndexName("CommentId-SenderUsername-index")
                                .withHashKeyValues(userNotification)
                                .withRangeKeyCondition("SenderUsername", new Condition()
                                        .withComparisonOperator(ComparisonOperator.EQ)
                                        .withAttributeValueList(new AttributeValue().withS(senderUsername)))
                                .withFilterExpression("NotificationType = :type")
                                .withExpressionAttributeValues(expressionAttributeValues)
                                .withConsistentRead(false);
                    }
                }

            }

            if (queryExpression != null) {
                List<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);
                // make sure only 1 entry is found
                if (queryResults != null && queryResults.size() == 1) {
                    mapper.delete(queryResults.get(0));
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean notificationDeleted) {
            super.onPostExecute(notificationDeleted);
            if (taskCallback != null) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("TASK_SUCCESS", notificationDeleted);
                bundle.putBoolean("IS_POST_DELETED", isPostDeleted);
                taskCallback.onTaskDone(bundle);
            }
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

    @SuppressWarnings("unchecked")
    private  class BatchDeleteCommentDBUserNotificationsAsyncTask extends AsyncTask<Comment, Void, Void> {

        private NotificationEnum notificationType;

        private BatchDeleteCommentDBUserNotificationsAsyncTask(NotificationEnum notificationType) {
            this.notificationType = notificationType;
        }

        @Override
        protected Void doInBackground(Comment... comments) {
            Comment userComment = comments[0];
            DBUserNotification userNotification = new DBUserNotification();
            userNotification.setPostId(userComment.getPostId());
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":type", new AttributeValue().withN(String.valueOf(notificationType.toInt())));
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                    .withIndexName("PostId-CommentId-index")
                    .withHashKeyValues(userNotification)
                    .withRangeKeyCondition("CommentId", new Condition()
                            .withComparisonOperator(ComparisonOperator.EQ)
                            .withAttributeValueList(new AttributeValue().withS(userComment.getCommentId())))
                    .withFilterExpression("NotificationType = :type")
                    .withExpressionAttributeValues(expressionAttributeValues)
                    .withConsistentRead(false);

            PaginatedQueryList<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);

            if (queryResults != null && queryResults.size() > 0) {
                for (DBUserNotification notification : queryResults) {
                    mapper.delete(notification);
                }
            }
            return null;
        }
    }


    /**
     * Conversation/Messaging async tasks
     */
    private class CreateNewDBUserConversationAsyncTask extends AsyncTask<String, Void, Conversation> {
        private AsyncTaskCallbackWithReturnObject callbackWithReturnObject;
        private CreateNewDBUserConversationAsyncTask(AsyncTaskCallbackWithReturnObject callbackWithReturnObject) {
            this.callbackWithReturnObject = callbackWithReturnObject;
        }
        @Override
        protected Conversation doInBackground(String... usernames) {
            DBUserProfile receiverProfile = loadDBUserProfile(usernames[0]);
            DBUserProfile senderProfile = loadDBUserProfile(usernames[1]);
            if (receiverProfile != null && senderProfile != null) {
                DBUserConversation newConversation = new DBUserConversation();
                String conversation_id = senderProfile.getFacebookId() + "_" + receiverProfile.getFacebookId();
                newConversation.setConversationID(conversation_id);
                newConversation.setSenderUsername(senderProfile.getUsername());
                newConversation.setReceiverUsername(receiverProfile.getUsername());
                Long timeInSeconds = Helpers.getTimestampSeconds();
                newConversation.setTimeStampCreated(timeInSeconds);
                newConversation.setTimeStampLatest(timeInSeconds);
                Map<String, String> usernameFacebookIDMap = new HashMap<>();
                usernameFacebookIDMap.put(receiverProfile.getUsername(), receiverProfile.getFacebookId());
                usernameFacebookIDMap.put(senderProfile.getUsername(), senderProfile.getFacebookId());
                newConversation.setConversation(new Conversation(conversation_id,
                                                                 timeInSeconds,
                                                                 new ArrayList<ChatMessage>(),
                                                                 usernameFacebookIDMap));

                // append conversation_id to each user
                mapper.save(newConversation);
                receiverProfile.addConversationId(conversation_id);
                senderProfile.addConversationId(conversation_id);
                mapper.save(receiverProfile);
                mapper.save(senderProfile);
                return newConversation.getConversation();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Conversation conversation) {
            if (conversation != null) {
                callbackWithReturnObject.onTaskDone(conversation);
            }
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
                for (String username : conversation.getUsernameFacebookIdMap().keySet()) {
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

    /**
     * Other misc dynamodb async tasks
     */
    private class CreateNewDBUserFeedbackAsyncTask extends AsyncTask<Void, Void, Void> {
        private Feedback feedback;
        private CreateNewDBUserFeedbackAsyncTask(Feedback feedback) {
            this.feedback = feedback;
        }
        @Override
        protected Void doInBackground(Void... v) {
            if (feedback != null) {
                DBUserProfile userProfile = null;
                if (feedback.getFeedbackType() == FeedbackEnum.GENERAL) {
                    userProfile = loadDBUserProfile(feedback.getUsername());
                } else if (feedback.getFeedbackType() == FeedbackEnum.ACCOUNT_DELETION){
                    userProfile = loadDBUserProfile("deleted_profiles_feedback");
                }
                if (userProfile != null) {
                    userProfile.addFeedback(feedback);
                    mapper.save(userProfile);
                }
            }
            return null;
        }
    }

    private class UpdateFirebaseInstanceIdAsyncTask extends AsyncTask<String, Void, Void> {
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
