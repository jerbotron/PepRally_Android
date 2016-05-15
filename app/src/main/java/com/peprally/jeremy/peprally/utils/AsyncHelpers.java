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

    public static class asyncTaskObjectUserInfoBundle {
        public String cognitoID;
        public String firstName;
        public boolean incrementPost;
        public DynamoDBMapper mapper;
        public asyncTaskObjectUserInfoBundle(String cognitoID,
                                             String firstName,
                                             boolean incrementPost,
                                             DynamoDBMapper mapper) {
            this.cognitoID = cognitoID;
            this.firstName = firstName;
            this.incrementPost = incrementPost;
            this.mapper = mapper;
        }
    }

    public static class asyncTaskObjectUserPostBundle {
        public DBUserPost userPost;
        public DynamoDBMapper mapper;
        public asyncTaskObjectUserPostBundle(DBUserPost userPost,
                                             DynamoDBMapper mapper) {
            this.userPost = userPost;
            this.mapper = mapper;
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
            params[0].mapper.save(params[0].userPost);
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

    public static class PushUserProfilePostsCountToDBTask extends AsyncTask<asyncTaskObjectUserInfoBundle, Void, Void> {
        private com.peprally.jeremy.peprally.db_models.DBUserProfile userProfile;
        private DynamoDBMapper mapper;
        @Override
        protected Void doInBackground(asyncTaskObjectUserInfoBundle... params) {
            mapper = params[0].mapper;
            userProfile = mapper.load(DBUserProfile.class, params[0].cognitoID, params[0].firstName);
            if (params[0].incrementPost) {
                incrementPostsCount();
            }
            else {
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
}
