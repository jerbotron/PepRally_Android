package com.peprally.jeremy.peprally.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;

public class AsyncHelpers {

    private static final String TAG = AsyncHelpers.class.getSimpleName();

    private static Bitmap getFacebookProfilePicture(String userID) throws IOException {
        URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=small");
        return BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
    }

    public static class asyncTaskObjectProfileImage {
        public String facebookID;
        public ImageView imageView;
        public asyncTaskObjectProfileImage(String facebookID, ImageView imageView) {
            this.facebookID = facebookID;
            this.imageView = imageView;
        }
    }

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

    public static class LoadFBProfilePictureTask extends AsyncTask<asyncTaskObjectProfileImage, Void, Bitmap> {
        private ImageView imageView;
        @Override
        protected Bitmap doInBackground(asyncTaskObjectProfileImage... params) {
            Bitmap profileBitmap = null;
            try {
                profileBitmap = getFacebookProfilePicture(params[0].facebookID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "COULD NOT GET USER PROFILE");
            }
            imageView = params[0].imageView;
            return profileBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

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
            Bundle data = params[0].data;
            userPost = mapper.load(DBUserPost.class, userComment.getNickname(), data.getLong("POST_TIME_IN_SECONDS"));
            if (data != null) {
                if (data.getBoolean("INCREMENT_COMMENTS_COUNT")) {
                    incrementCommentsCount();
                }
                else if (!data.getBoolean("INCREMENT_COMMENTS_COUNT")){
                    decrementCommentsCount();
                }
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
