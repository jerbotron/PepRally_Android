package com.peprally.jeremy.peprally.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapter.CommentCardAdapter;
import com.peprally.jeremy.peprally.adapter.EmptyAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.Helpers;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NewCommentActivity extends AppCompatActivity {

    private Bundle postCommentBundle;

    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    private List<DBUserComment> comments;
    private CommentCardAdapter commentCardAdapter;
    private RecyclerView recyclerView;
    private LinearLayout commentsContainer;
    private TextView noCommentsText, mainPostCommentsCount;
    private EditText newCommentText;

    private static final String TAG = NewCommentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post_comment);

        // Set up AWS members
        credentialsProvider = new CognitoCachingCredentialsProvider(
                this,                                       // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        // Set up toolbar buttons
        final ActionBar supportActionBar = getSupportActionBar();
        assert (supportActionBar != null);
        supportActionBar.setTitle("Comments");
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        // Set up main post container
        commentsContainer = (LinearLayout) findViewById(R.id.id_container_post_comments);
        final ImageView mainPostProfileImage = (ImageView) findViewById(R.id.id_image_view_comment_main_post_profile);
        final TextView mainPostTimeStamp = (TextView) findViewById(R.id.id_text_view_comment_main_post_time_stamp);
        final TextView mainPostNickname = (TextView) findViewById(R.id.id_text_view_comment_main_post_nickname);
        final TextView mainPostTextContent = (TextView) findViewById(R.id.id_text_view_comment_main_post_content);
        final ImageButton mainPostThumbsUp = (ImageButton) findViewById(R.id.id_image_button_comment_main_post_thumbs_up);
        final ImageButton mainPostThumbsDown = (ImageButton) findViewById(R.id.id_image_button_comment_main_post_thumbs_down);
        final TextView mainPostLikesCount = (TextView) findViewById(R.id.id_text_view_comment_main_post_likes);
        mainPostCommentsCount = (TextView) findViewById(R.id.id_text_view_comment_main_post_comments);
        noCommentsText = (TextView) findViewById(R.id.id_text_view_post_empty_comments_text);
        newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
        final TextView postCommentButton = (TextView) findViewById(R.id.id_text_view_post_new_comment_button);

        assert (mainPostNickname != null && mainPostTextContent != null && mainPostTimeStamp != null &&
                mainPostLikesCount != null && mainPostCommentsCount != null && noCommentsText != null &&
                mainPostThumbsUp != null && mainPostThumbsDown != null && postCommentButton != null &&
                newCommentText != null);

        postCommentBundle = getIntent().getBundleExtra("POST_COMMENT_BUNDLE");

        new DisplayThumbsUpOrDownDBTask().execute(
                new AsyncHelpers.asyncTaskObjectThumbsUpDownButtons(mainPostThumbsUp,
                                                                    mainPostThumbsDown,
                                                                    mainPostLikesCount,
                                                                    getApplicationContext(),
                                                                    postCommentBundle,
                                                                    mapper));

        new AsyncHelpers.CheckIfUserLikedDislikedMainPost().execute(
                new AsyncHelpers.asyncTaskObjectThumbsUpDownButtons(mainPostThumbsUp,
                                                                    mainPostThumbsDown,
                                                                    mainPostLikesCount,
                                                                    getApplicationContext(),
                                                                    postCommentBundle,
                                                                    mapper));

        // Display Post Info Correctly
        if (postCommentBundle.getInt("COMMENTS_COUNT") <= 0) {
            noCommentsText.setText("No comments.");
        } else {
            commentsContainer.removeView(noCommentsText);
            refreshAdapter();
        }

        new AsyncHelpers.LoadFBProfilePictureTask().execute(new AsyncHelpers.asyncTaskObjectProfileImage(postCommentBundle.getString("FACEBOOK_ID"), mainPostProfileImage));

        long tsLong = System.currentTimeMillis()/1000;
        long timeInSeconds = tsLong - postCommentBundle.getLong("TIME_IN_SECONDS");
        if (timeInSeconds < 60) {
            mainPostTimeStamp.setText(String.valueOf(timeInSeconds) + "s");
        }
        else if (timeInSeconds < 60 * 60) {
            long timeInMins = timeInSeconds / 60;
            mainPostTimeStamp.setText(String.valueOf(timeInMins) + "m");
        }
        else if (timeInSeconds < 60 * 60 * 24) {
            long timeInHrs = timeInSeconds/60/60;
            mainPostTimeStamp.setText(String.valueOf(timeInHrs) + "h");
        }
        else {
            long timeInDays = timeInSeconds/60/60/24;
            mainPostTimeStamp.setText(String.valueOf(timeInDays) + "d");
        }

        int likesCount = postCommentBundle.getInt("LIKES_COUNT");
        mainPostNickname.setText(postCommentBundle.getString("NICKNAME"));
        mainPostTextContent.setText(postCommentBundle.getString("TEXT_CONTENT"));
        mainPostCommentsCount.setText(String.valueOf(postCommentBundle.getInt("COMMENTS_COUNT")));
        mainPostLikesCount.setText(String.valueOf(likesCount));
        if (likesCount > 0) {
            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
        }
        else if (likesCount < 0) {
            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
        }

        // Recycler View Setup
        recyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_post_comments);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        // Post Button onClick handler
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCommentToAdapter(newCommentText.getText().toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Hide soft keyboard if keyboard is up
                EditText et = (EditText) findViewById(R.id.id_edit_text_new_comment);
                Helpers.hideSoftKeyboard(this, et);
                onBackPressed();
                return true;
            case R.id.id_item_delete_post:
                new DeletePostFromDBTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void initializeAdapter(List<DBUserComment> result) {
        comments = new ArrayList<>();
        if (result != null) {
            for (DBUserComment userComment : result) {
                comments.add(userComment);
            }
        }
        commentCardAdapter = new CommentCardAdapter(this, comments);
        recyclerView.setAdapter(commentCardAdapter);
    }

    public void addCommentToAdapter(String newCommentText) {
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", postCommentBundle.getString("NICKNAME"));
        bundle.putString("FACEBOOK_ID", postCommentBundle.getString("FACEBOOK_ID"));
        bundle.putString("FIRST_NAME", postCommentBundle.getString("FIRST_NAME"));
        if (commentCardAdapter == null) {
            initializeAdapter(null);
        }
        // Checks is this post is the first user comment
        if (postCommentBundle.getInt("COMMENTS_COUNT") == 0) {
            commentsContainer.removeView(noCommentsText);
        }
        commentCardAdapter.addComment(newCommentText, bundle);
    }

    public void refreshAdapter() {
        new FetchPostCommentsCountTask().execute(postCommentBundle);
        new FetchPostCommentsTask().execute(postCommentBundle.getString("POST_ID"));
    }

    public void postAddCommentCleanup() {
        refreshAdapter();
        newCommentText.setText("");
        newCommentText.clearFocus();
        Helpers.hideSoftKeyboard(this, newCommentText);
    }

    public String getPostCommentBundleString(String key) {
        return postCommentBundle.getString(key);
    }

    public Long getPostCommentBundleLong(String key) {
        return postCommentBundle.getLong(key);
    }

    /********************************** AsyncTasks **********************************/

    private class DisplayThumbsUpOrDownDBTask extends AsyncTask<AsyncHelpers.asyncTaskObjectThumbsUpDownButtons, Void, DBUserPost> {
        private DynamoDBMapper mapper;
        private String userNickName;
        private ImageButton mainPostThumbsUp, mainPostThumbsDown;
        private TextView mainPostLikesCount;
        @Override
        protected DBUserPost doInBackground(AsyncHelpers.asyncTaskObjectThumbsUpDownButtons... params) {
            mapper = params[0].mapper;
            mainPostThumbsUp = params[0].thumbsUp;
            mainPostThumbsDown = params[0].thumbsDown;
            mainPostLikesCount = params[0].likesCount;
            Bundle dataBundle = params[0].dataBundle;
            userNickName = dataBundle.getString("NICKNAME");
            DBUserPost userPost = mapper.load(DBUserPost.class,
                    userNickName,
                    dataBundle.getLong("TIME_IN_SECONDS"));
            return userPost;
        }

        @Override
        protected void onPostExecute(final DBUserPost userPost) {
            // Button Click Events:
            mainPostThumbsUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentNumOfLikes = Integer.parseInt(mainPostLikesCount.getText().toString());
                    Set<String> likedUsers = userPost.getLikedUsers();
                    Set<String> dislikedUsers = userPost.getDislikedUsers();
                    // If user already liked the post
                    if (likedUsers.contains(userNickName)) {
                        currentNumOfLikes -= 1;
                        mainPostLikesCount.setText(String.valueOf(currentNumOfLikes));
                        mainPostThumbsUp.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up));
                        userPost.setNumberOfLikes(currentNumOfLikes);
                        // Special transition cases
                        if (currentNumOfLikes == 0) {
                            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                        } else if (currentNumOfLikes == -1) {
                            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                        }
                        // Remove user from likedUsers set
                        userPost.removeLikedUsers(userNickName);
                        new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                                new AsyncHelpers.asyncTaskObjectUserPostBundle(userPost, mapper, null, null));
                    }
                    // If user has not liked the post yet
                    else {
                        // lose previous dislike and +1 like
                        if (dislikedUsers.contains(userNickName)) {
                            currentNumOfLikes += 2;
                        } else {
                            currentNumOfLikes += 1;
                        }
                        mainPostLikesCount.setText(String.valueOf(currentNumOfLikes));
                        mainPostThumbsUp.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_uped));
                        mainPostThumbsDown.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_down));
                        userPost.setNumberOfLikes(currentNumOfLikes);
                        if (currentNumOfLikes == 0) {
                            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                        } else if (currentNumOfLikes == 1) {
                            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                        }
                        // Remove user from dislikedUsers set and add to likedUsers set
                        userPost.addLikedUsers(userNickName);
                        userPost.removedislikedUsers(userNickName);
                        new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                                new AsyncHelpers.asyncTaskObjectUserPostBundle(userPost, mapper, null, null));
                    }
                }
            });

            mainPostThumbsDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentNumOfLikes = Integer.parseInt(mainPostLikesCount.getText().toString());
                    Set<String> likedUsers = userPost.getLikedUsers();
                    Set<String> dislikedUsers = userPost.getDislikedUsers();
                    // If user already disliked the post
                    if (dislikedUsers.contains(userNickName)) {
                        currentNumOfLikes += 1;
                        mainPostLikesCount.setText(String.valueOf(currentNumOfLikes));
                        mainPostThumbsDown.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_down));
                        userPost.setNumberOfLikes(currentNumOfLikes);
                        // Special transition cases
                        if (currentNumOfLikes == 0) {
                            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                        } else if (currentNumOfLikes == 1) {
                            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                        }
                        // Remove user from likedUsers set
                        userPost.removedislikedUsers(userNickName);
                        new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                                new AsyncHelpers.asyncTaskObjectUserPostBundle(userPost, mapper, null, null));
                    }
                    // If user has not disliked the post yet
                    else {
                        if (likedUsers.contains(userNickName)) {
                            // lose previous like and +1 dislike
                            currentNumOfLikes -= 2;
                        } else {
                            currentNumOfLikes -= 1;
                        }
                        mainPostLikesCount.setText(String.valueOf(currentNumOfLikes));
                        mainPostThumbsUp.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up));
                        mainPostThumbsDown.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_downed));
                        userPost.setNumberOfLikes(currentNumOfLikes);
                        if (currentNumOfLikes == 0) {
                            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                        } else if (currentNumOfLikes == -1) {
                            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                        }
                        // Remove user from dislikedUsers set and add to likedUsers set
                        userPost.adddislikedUsers(userNickName);
                        userPost.removeLikedUsers(userNickName);
                        new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                                new AsyncHelpers.asyncTaskObjectUserPostBundle(userPost, mapper, null, null));
                    }
                }
            });
        }
    }

    private class DeletePostFromDBTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            DBUserPost post = mapper.load(DBUserPost.class, postCommentBundle.getString("NICKNAME"), postCommentBundle.getLong("TIME_IN_SECONDS"));
            mapper.delete(post);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Hide soft keyboard if keyboard is up
            EditText et = (EditText) findViewById(R.id.id_edit_text_new_comment);
            Helpers.hideSoftKeyboard(NewCommentActivity.this, et);
            Intent intent = new Intent();
            intent.putExtra("ACTION", "DELETE_POST");
            setResult(Activity.RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    private class FetchPostCommentsTask extends AsyncTask<String, Void, PaginatedQueryList<DBUserComment>> {
        @Override
        protected PaginatedQueryList<DBUserComment> doInBackground(String... params) {
            String postID = params[0];
            DBUserComment userComment = new DBUserComment();
            userComment.setPostID(postID);
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(userComment)
                    .withConsistentRead(false);
            return mapper.query(DBUserComment.class, queryExpression);
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBUserComment> result) {
            initializeAdapter(result);
        }
    }

    private class FetchPostCommentsCountTask extends AsyncTask<Bundle, Void, Integer> {
        @Override
        protected Integer doInBackground(Bundle... params) {
            Bundle data = params[0];
            DBUserPost userPost = mapper.load(DBUserPost.class, data.getString("NICKNAME"), data.getLong("TIME_IN_SECONDS"));
            return userPost.getNumberOfComments();
        }

        @Override
        protected void onPostExecute(Integer result) {
            mainPostCommentsCount.setText(result.toString());
        }
    }
}
