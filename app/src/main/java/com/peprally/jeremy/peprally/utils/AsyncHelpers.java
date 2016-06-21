package com.peprally.jeremy.peprally.utils;

import android.os.AsyncTask;
import android.os.Bundle;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.db_models.DBUserPost;


public class AsyncHelpers {

    private static final String TAG = AsyncHelpers.class.getSimpleName();

    public static class asyncTaskObjectUserPostBundle {
        public DBUserPost post;
        public DynamoDBMapper mapper;
        public UserProfileParcel parcel;
        public Bundle data;
        public asyncTaskObjectUserPostBundle(DBUserPost post,
                                             DynamoDBMapper mapper,
                                             UserProfileParcel parcel,
                                             Bundle data) {
            this.post = post;
            this.mapper = mapper;
            this.parcel = parcel;
            this.data = data;
        }
    }

    public static class asyncTaskObjectUserCommentBundle {
        public DBUserComment comment;
        public DynamoDBMapper mapper;
        public Bundle data;
        public asyncTaskObjectUserCommentBundle(DBUserComment comment,
                                                DynamoDBMapper mapper,
                                                Bundle data) {
            this.comment = comment;
            this.mapper = mapper;
            this.data = data;
        }
    }

    /********************************** AsyncTasks **********************************/

    public static class PushUserPostChangesToDBTask extends AsyncTask<asyncTaskObjectUserPostBundle, Void, Void> {
        @Override
        protected Void doInBackground(asyncTaskObjectUserPostBundle... params) {
            params[0].mapper.save(params[0].post);
            return null;
        }
    }

    public static class PushUserCommentChangesToDBTask extends AsyncTask<asyncTaskObjectUserCommentBundle, Void, Void> {
        @Override
        protected Void doInBackground(asyncTaskObjectUserCommentBundle... params) {
            params[0].mapper.save(params[0].comment);
            return null;
        }
    }

    public static class PushPostCommentsCountToDBTask extends AsyncTask<asyncTaskObjectUserCommentBundle, Void, Void> {
        private DBUserPost userPost;
        private DynamoDBMapper mapper;
        @Override
        protected Void doInBackground(asyncTaskObjectUserCommentBundle... params) {
            mapper = params[0].mapper;
            DBUserComment userComment = params[0].comment;
            String postNickname = userComment.getPostID().split("_")[0];
            Bundle data = params[0].data;
            userPost = mapper.load(DBUserPost.class, postNickname, data.getLong("POST_TIME_IN_SECONDS"));
            if (data.getBoolean("INCREMENT_COMMENTS_COUNT")) {
                incrementCommentsCount();
            }
            else if (!data.getBoolean("INCREMENT_COMMENTS_COUNT")){
                decrementCommentsCount();
            }
            return null;
        }

        private void incrementCommentsCount() {
            int curCommentsCount = userPost.getNumberOfComments();
            userPost.setNumberOfComments(curCommentsCount + 1);
            mapper.save(userPost);
        }

        private void decrementCommentsCount() {
            int curCommentsCount = userPost.getNumberOfComments();
            userPost.setNumberOfComments(curCommentsCount - 1);
            mapper.save(userPost);
        }
    }
}
