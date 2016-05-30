package com.peprally.jeremy.peprally.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.NewCommentActivity;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.PostHolder> {

    private Context callingContext;

    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    private List<DBUserPost> posts;

    private static final String TAG = "PostCardAdapter: ";

    public static class PostHolder extends RecyclerView.ViewHolder {
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

        public PostHolder(View itemView) {
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

    public PostCardAdapter(Context callingContext, List<DBUserPost> posts) {
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
    public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_new_post_container, parent, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(final PostHolder newPostHolder, int position) {
        final DBUserPost curPost = posts.get(position);
        new AsyncHelpers.LoadFBProfilePictureTask().execute(new AsyncHelpers.asyncTaskObjectProfileImage(curPost.getFacebookID(), newPostHolder.profilePhoto));

        final String userNickName = ((ProfileActivity) callingContext).getUserProfileParcel().getNickname();

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
        final int numOfLikes = curPost.getNumberOfLikes();
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
                    new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                            new AsyncHelpers.asyncTaskObjectUserPostBundle(curPost, mapper, null, null));
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
                    new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                            new AsyncHelpers.asyncTaskObjectUserPostBundle(curPost, mapper, null, null));
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
                    new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                            new AsyncHelpers.asyncTaskObjectUserPostBundle(curPost, mapper, null, null));
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
                    new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                            new AsyncHelpers.asyncTaskObjectUserPostBundle(curPost, mapper, null, null));
                }
            }
        });

        // New Comment Events
        newPostHolder.postContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle postCommentBundle = new Bundle();
                postCommentBundle.putString("POST_ID", curPost.getPostID());
                postCommentBundle.putString("TEXT_CONTENT", curPost.getTextContent());
                postCommentBundle.putString("NICKNAME", curPost.getNickname());
                postCommentBundle.putLong("TIME_IN_SECONDS", curPost.getTimeInSeconds());
                postCommentBundle.putString("FACEBOOK_ID", curPost.getFacebookID());
                postCommentBundle.putInt("LIKES_COUNT", curPost.getNumberOfLikes());
                postCommentBundle.putInt("COMMENTS_COUNT", curPost.getNumberOfComments());
                Intent intent = new Intent(callingContext, NewCommentActivity.class);
                intent.putExtra("POST_COMMENT_BUNDLE", postCommentBundle);
                ((ProfileActivity) callingContext).startActivityForResult(intent, ProfileActivity.POST_COMMENT_REQUEST_CODE);
                ((ProfileActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        newPost.setNickname(bundle.getString("NICKNAME"));
        Long timeInSeconds = System.currentTimeMillis() / 1000;
        newPost.setTimeInSeconds(timeInSeconds);
        newPost.setPostID(bundle.getString("NICKNAME") + "_" + timeInSeconds.toString());
        newPost.setCognitoID(credentialsProvider.getIdentityId());
        newPost.setFacebookID(bundle.getString("FACEBOOK_ID"));
        newPost.setFirstname(bundle.getString("FIRST_NAME"));
        newPost.setTimeStamp(df.format(c.getTime()));
        newPost.setTextContent(newPostText);
        newPost.setLikedUsers(new HashSet<>(Collections.singletonList("_")));
        newPost.setDislikedUsers(new HashSet<>(Collections.singletonList("_")));
        newPost.setNumberOfLikes(0);
        newPost.setNumberOfComments(0);
        new PushNewUserPostToDBTask().execute(newPost);
        Bundle asyncData = new Bundle();
        asyncData.putBoolean("INCREMENT_POSTS_COUNT", true);
        new AsyncHelpers.PushUserProfilePostsCountToDBTask().execute(
                new AsyncHelpers.asyncTaskObjectUserPostBundle(
                        newPost,
                        mapper,
                        null,
                        asyncData));
    }

    /********************************** AsyncTasks **********************************/
    private class PushNewUserPostToDBTask extends AsyncTask<DBUserPost, Void, DBUserPost> {
        @Override
        protected DBUserPost doInBackground(DBUserPost... params) {
            DBUserPost newPost= params[0];
            mapper.save(newPost);
            return newPost;
        }

        @Override
        protected void onPostExecute(DBUserPost newPost) {
            posts.add(0, newPost);
            notifyItemInserted(0);
        }
    }
}
