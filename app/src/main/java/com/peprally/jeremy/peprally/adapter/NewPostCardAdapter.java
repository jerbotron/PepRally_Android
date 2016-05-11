package com.peprally.jeremy.peprally.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.HomeActivity;
import com.peprally.jeremy.peprally.activities.NewPostCommentActivity;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NewPostCardAdapter extends RecyclerView.Adapter<NewPostCardAdapter.NewPostHolder> {

    private Context callingContext;

    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    private List<DBUserPost> posts;

    private static final String TAG = "NewPostCardAdapter: ";

    public static class NewPostHolder extends RecyclerView.ViewHolder {
        LinearLayout postContainer;
        CardView cardView;
        ImageView profilePhoto;
        TextView nickname;
        TextView timeStamp;
        TextView postContent;
        ImageButton thumbsUp;
        ImageButton thumbsDown;
        TextView postLikesCount;
        TextView postComments;

        public NewPostHolder(View itemView) {
            super(itemView);
            postContainer = (LinearLayout) itemView.findViewById(R.id.id_container_post_clickable);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_new_post);
            profilePhoto = (ImageView) itemView.findViewById(R.id.id_image_view_post_profile);
            nickname = (TextView) itemView.findViewById(R.id.id_text_view_post_nickname);
            timeStamp = (TextView) itemView.findViewById(R.id.id_text_view_post_time_stamp);
            postContent = (TextView) itemView.findViewById(R.id.id_text_view_post_content);
            thumbsUp = (ImageButton) itemView.findViewById(R.id.id_image_button_post_thumbs_up);
            thumbsDown = (ImageButton) itemView.findViewById(R.id.id_image_button_post_thumbs_down);
            postLikesCount = (TextView) itemView.findViewById(R.id.id_text_view_post_likes);
            postComments = (TextView) itemView.findViewById(R.id.id_text_view_post_comments);
        }
    }

    public NewPostCardAdapter(Context callingContext, List<DBUserPost> posts) {
        this.posts = posts;
        this.callingContext = callingContext;
        credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,                             // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
    }

    @Override
    public NewPostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_new_post_container, parent, false);
        NewPostHolder newPostHolder = new NewPostHolder(view);
        return newPostHolder;
    }

    @Override
    public void onBindViewHolder(final NewPostHolder newPostHolder, int position) {
        final DBUserPost curPost = posts.get(position);
        new LoadFBProfilePictureTask().execute(new asyncTaskObject(curPost.getFacebookID(), newPostHolder.profilePhoto));

        final String userNickName = ((ProfileActivity) callingContext).getUserProfileBundleString("NICKNAME");
        Set<String> likedUsers = curPost.getLikedUsers();
        Set<String> dislikedUsers = curPost.getDislikedUsers();

        if (likedUsers.contains(userNickName)) {
            newPostHolder.thumbsUp.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_uped));
        }
        else if (dislikedUsers.contains(userNickName)) {
            newPostHolder.thumbsDown.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_downed));
        }

        newPostHolder.nickname.setText(curPost.getNickname());
        newPostHolder.postContent.setText(curPost.getTextContent());
        int numOfLikes = curPost.getNumberOfLikes();
        if (numOfLikes > 0) {
            newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorGreen)));
        }
        else if (numOfLikes == 0) {
            newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorAccent)));
        }
        else {
            newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorRed)));
        }
        newPostHolder.postLikesCount.setText(String.valueOf(numOfLikes));
        newPostHolder.postComments.setText(String.valueOf(curPost.getNumberOfComments()));

        long tsLong = System.currentTimeMillis()/1000;
        long timeInSeconds = tsLong - curPost.getTimeInSeconds();
        if (timeInSeconds < 60) {
            newPostHolder.timeStamp.setText(String.valueOf(timeInSeconds) + "s");
        }
        else if (timeInSeconds < 60 * 60) {
            long timeInMins = timeInSeconds / 60;
            newPostHolder.timeStamp.setText(String.valueOf(timeInMins) + "m");
        }
        else if (timeInSeconds < 60 * 60 * 24) {
            long timeInHrs = timeInSeconds/60/60;
            newPostHolder.timeStamp.setText(String.valueOf(timeInHrs) + "h");
        }
        else {
            long timeInDays = timeInSeconds/60/60/24;
            newPostHolder.timeStamp.setText(String.valueOf(timeInDays) + "d");
        }

        // Button Click Events:
        newPostHolder.thumbsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNumOfLikes = Integer.parseInt(newPostHolder.postLikesCount.getText().toString());
                Set<String> likedUsers = curPost.getLikedUsers();
                Set<String> dislikedUsers = curPost.getDislikedUsers();
                // If user already liked the post
                if (likedUsers.contains(userNickName)) {
                    currentNumOfLikes -= 1;
                    newPostHolder.postLikesCount.setText(String.valueOf(currentNumOfLikes));
                    newPostHolder.thumbsUp.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_up));
                    curPost.setNumberOfLikes(currentNumOfLikes);
                    // Special transition cases
                    if (currentNumOfLikes == 0) {
                        newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorAccent)));
                    } else if (currentNumOfLikes == -1) {
                        newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorRed)));
                    }
                    // Remove user from likedUsers set
                    curPost.removeLikedUsers(userNickName);
                    new PushUserPostChangesToDBTask().execute(curPost);
                }
                // If user has not liked the post yet
                else {
                    // lose previous dislike and +1 like
                    if (dislikedUsers.contains(userNickName)) {
                        currentNumOfLikes += 2;
                    }
                    else {
                        currentNumOfLikes += 1;
                    }
                    newPostHolder.postLikesCount.setText(String.valueOf(currentNumOfLikes));
                    newPostHolder.thumbsUp.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_uped));
                    newPostHolder.thumbsDown.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_down));
                    curPost.setNumberOfLikes(currentNumOfLikes);
                    if (currentNumOfLikes == 0) {
                        newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorAccent)));
                    } else if (currentNumOfLikes == 1) {
                        newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorGreen)));
                    }
                    // Remove user from dislikedUsers set and add to likedUsers set
                    curPost.addLikedUsers(userNickName);
                    curPost.removedislikedUsers(userNickName);
                    new PushUserPostChangesToDBTask().execute(curPost);
                }
            }
        });

        newPostHolder.thumbsDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNumOfLikes = Integer.parseInt(newPostHolder.postLikesCount.getText().toString());
                Set<String> likedUsers = curPost.getLikedUsers();
                Set<String> dislikedUsers = curPost.getDislikedUsers();
                // If user already disliked the post
                if (dislikedUsers.contains(userNickName)) {
                    currentNumOfLikes += 1;
                    newPostHolder.postLikesCount.setText(String.valueOf(currentNumOfLikes));
                    newPostHolder.thumbsDown.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_down));
                    curPost.setNumberOfLikes(currentNumOfLikes);
                    // Special transition cases
                    if (currentNumOfLikes == 0) {
                        newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorAccent)));
                    } else if (currentNumOfLikes == 1) {
                        newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorGreen)));
                    }
                    // Remove user from likedUsers set
                    curPost.removedislikedUsers(userNickName);
                    new PushUserPostChangesToDBTask().execute(curPost);
                }
                // If user has not disliked the post yet
                else {
                    if (likedUsers.contains(userNickName)) {
                        // lose previous like and +1 dislike
                        currentNumOfLikes -= 2;
                    }
                    else {
                        currentNumOfLikes -= 1;
                    }
                    newPostHolder.postLikesCount.setText(String.valueOf(currentNumOfLikes));
                    newPostHolder.thumbsUp.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_up));
                    newPostHolder.thumbsDown.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_downed));
                    curPost.setNumberOfLikes(currentNumOfLikes);
                    if (currentNumOfLikes == 0) {
                        newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorAccent)));
                    } else if (currentNumOfLikes == -1) {
                        newPostHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorRed)));
                    }
                    // Remove user from dislikedUsers set and add to likedUsers set
                    curPost.adddislikedUsers(userNickName);
                    curPost.removeLikedUsers(userNickName);
                    new PushUserPostChangesToDBTask().execute(curPost);
                }
            }
        });

        // New Comment Events
        newPostHolder.postContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(callingContext, NewPostCommentActivity.class);
                callingContext.startActivity(intent);
                ((ProfileActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
                ((ProfileActivity) callingContext).finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void addPost(String newPostText, Bundle bundle) {
        DBUserPost newPost = new DBUserPost();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        newPost.setNickname(bundle.getString("NICKNAME"));
        newPost.setTimeInSeconds(System.currentTimeMillis() / 1000);
        newPost.setCognitoID(credentialsProvider.getIdentityId());
        newPost.setFacebookID(bundle.getString("FACEBOOK_ID"));
        newPost.setTimeStamp(df.format(c.getTime()));
        newPost.setTextContent(newPostText);
        newPost.setLikedUsers(new HashSet<String>(Arrays.asList("_")));
        newPost.setDislikedUsers(new HashSet<String>(Arrays.asList("_")));
        new PushNewUserPostToDBTask().execute(newPost);
        new PushUserProfilePostsCountToDBTask().execute(bundle.getString("FIRST_NAME"));
    }

    private Bitmap getFacebookProfilePicture(String userID) throws IOException {
        URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=small");
        return BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
    }

    /********************************** AsyncTasks **********************************/

    private class asyncTaskObject {
        public String facebookID;
        public ImageView imageView;
        asyncTaskObject(String facebookID, ImageView imageView) {
            this.facebookID = facebookID;
            this.imageView = imageView;
        }
    }

    private class LoadFBProfilePictureTask extends AsyncTask<asyncTaskObject, Void, Bitmap> {
        private ImageView imageView;
        @Override
        protected Bitmap doInBackground(asyncTaskObject... params) {
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

    private class PushUserPostChangesToDBTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            mapper.save(params[0]);
            return null;
        }
    }

    private class PushNewUserPostToDBTask extends AsyncTask<DBUserPost, Void, DBUserPost> {
        @Override
        protected DBUserPost doInBackground(DBUserPost... params) {
            DBUserPost newUserPost= params[0];
            HashMap<String, AttributeValue> newUserPostMap = new HashMap<>();
            newUserPostMap.put("Nickname", new AttributeValue().withS(newUserPost.getNickname()));
            newUserPostMap.put("TimeInSeconds", new AttributeValue().withN(String.valueOf(newUserPost.getTimeInSeconds())));
            newUserPostMap.put("CognitoID", new AttributeValue().withS(newUserPost.getCognitoID()));
            newUserPostMap.put("FacebookID", new AttributeValue().withS(newUserPost.getFacebookID()));
            newUserPostMap.put("TimeStamp", new AttributeValue().withS(newUserPost.getTimeStamp()));
            newUserPostMap.put("TextContent", new AttributeValue().withS(newUserPost.getTextContent()));
            newUserPostMap.put("NumberOfLikes", new AttributeValue().withN(String.valueOf(0)));
            newUserPostMap.put("NumberOfComments", new AttributeValue().withN(String.valueOf(0)));
            newUserPostMap.put("LikedUsers", new AttributeValue().withSS(newUserPost.getLikedUsers()));
            newUserPostMap.put("DislikedUsers", new AttributeValue().withSS(newUserPost.getDislikedUsers()));
            ddbClient.putItem(new PutItemRequest().withTableName("UserPosts").withItem(newUserPostMap));
            return newUserPost;
        }

        @Override
        protected void onPostExecute(DBUserPost newUserPost) {
            posts.add(0, newUserPost);
            notifyItemInserted(0);
        }
    }

    private class PushUserProfilePostsCountToDBTask extends AsyncTask<String, Void, Void> {
        private com.peprally.jeremy.peprally.db_models.DBUserProfile userProfile;
        @Override
        protected Void doInBackground(String... params) {
            userProfile = mapper.load(DBUserProfile.class, credentialsProvider.getIdentityId(), params[0]);
            incrementPostsCount();
            return null;
        }

        private void incrementPostsCount() {
            int curPostCount = userProfile.getPostsCount();
            userProfile.setPostsCount(curPostCount + 1);
            mapper.save(userProfile);
        }
    }
}
