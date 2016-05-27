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

    public static class asyncTaskObjectThumbsUpDownButtons {
        public ImageButton thumbsUp;
        public ImageButton thumbsDown;
        public TextView likesCount;
        public Context callingContext;
        public Bundle dataBundle;
        public DynamoDBMapper mapper;
        public asyncTaskObjectThumbsUpDownButtons(ImageButton thumbsUp,
                                                  ImageButton thumbsDown,
                                                  TextView likesCount,
                                                  Context callingContext,
                                                  Bundle dataBundle,
                                                  DynamoDBMapper mapper) {
            this.thumbsUp = thumbsUp;
            this.thumbsDown = thumbsDown;
            this.likesCount = likesCount;
            this.callingContext = callingContext;
            this.dataBundle = dataBundle;
            this.mapper = mapper;
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

    public static class CheckIfUserLikedDislikedMainPost extends AsyncTask<asyncTaskObjectThumbsUpDownButtons, Void, Integer> {
        ImageButton thumbsUp, thumbsDown;
        Context callingContext;
        @Override
        protected Integer doInBackground(asyncTaskObjectThumbsUpDownButtons... params) {
            thumbsUp = params[0].thumbsUp;
            thumbsDown = params[0].thumbsDown;
            callingContext = params[0].callingContext;
            Bundle dataBundle = params[0].dataBundle;
            DynamoDBMapper mapper = params[0].mapper;
            String nickname = dataBundle.getString("NICKNAME");
            DBUserPost userPost = mapper.load(DBUserPost.class, nickname, dataBundle.getLong("TIME_IN_SECONDS"));
            // likedStatus:
            // 0 (default) = neither liked or disliked
            // 1 = liked
            // 2 = disliked
            Integer likedStatus = 0;
            if (userPost.getLikedUsers().contains(nickname)) {
                likedStatus = 1;
            }
            else if (userPost.getDislikedUsers().contains(nickname)) {
                likedStatus = 2;
            }
            return likedStatus;
        }

        @Override
        protected void onPostExecute(Integer likedStatus) {
            switch (likedStatus) {
                case 1:
                    thumbsUp.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_uped));
                    break;
                case 2:
                    thumbsDown.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_downed));
                    break;
            }
        }
    }

    public static class PushUserProfilePostsCountToDBTask extends AsyncTask<asyncTaskObjectUserPostBundle, Void, Void> {
        private com.peprally.jeremy.peprally.db_models.DBUserProfile userProfile;
        private DynamoDBMapper mapper;
        @Override
        protected Void doInBackground(asyncTaskObjectUserPostBundle... params) {
            mapper = params[0].mapper;
            DBUserPost userPost = params[0].post;
            UserProfileParcel parcel = params[0].parcel;
            Bundle data = params[0].data;
            if (userPost == null) {
                userProfile = mapper.load(DBUserProfile.class, parcel.getCognitoID(), parcel.getFirstname());
            } else {
                userProfile = mapper.load(DBUserProfile.class, userPost.getCognitoID(), userPost.getFirstname());
            }
            if (data != null)
                if (data.getBoolean("INCREMENT_POSTS_COUNT")) {
                    incrementPostsCount();
                }
                else if (!data.getBoolean("INCREMENT_POSTS_COUNT")){
                    decrementPostsCount();
                }
            return null;
        }

        private void incrementPostsCount() {
            int curPostCount = userProfile.getPostsCount();
            userProfile.setPostsCount(curPostCount + 1);
            mapper.save(userProfile);
        }

        private void decrementPostsCount() {
            int curPostCount = userProfile.getPostsCount();
            userProfile.setPostsCount(curPostCount - 1);
            mapper.save(userProfile);
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
