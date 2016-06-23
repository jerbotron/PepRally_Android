package com.peprally.jeremy.peprally.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.CommentCardAdapter;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NewCommentActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/

    // UI Variables
    private RecyclerView recyclerView;
    private LinearLayout commentsContainer;
    private TextView noCommentsText, mainPostCommentsCount, textViewCharCount;
    private EditText newCommentText;

    // AWS Variables
    private DynamoDBHelper dbHelper;

    // General Variables
//    private static final String TAG = NewCommentActivity.class.getSimpleName();
    private CommentCardAdapter commentCardAdapter;
    private Bundle postCommentBundle;
    private UserProfileParcel userProfileParcel;
    private boolean selfPost;       // if current post is user's own post
    private int charCount = 200;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post_comment);

        dbHelper = new DynamoDBHelper(this);

        // Set up toolbar buttons
        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Comments");
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set up main post container
        commentsContainer = (LinearLayout) findViewById(R.id.id_container_post_comments);
        final ImageView mainPostProfileImage = (ImageView) findViewById(R.id.id_image_view_comment_main_post_profile);
        final TextView mainPostTimeStamp = (TextView) findViewById(R.id.id_text_view_comment_main_post_time_stamp);
        final TextView mainPostNickname = (TextView) findViewById(R.id.id_text_view_comment_main_post_nickname);
        final TextView mainPostTextContent = (TextView) findViewById(R.id.id_text_view_comment_main_post_content);
        final TextView mainPostLikesCount = (TextView) findViewById(R.id.id_text_view_comment_main_post_likes);
        mainPostCommentsCount = (TextView) findViewById(R.id.id_text_view_comment_main_post_comments);
        noCommentsText = (TextView) findViewById(R.id.id_text_view_post_empty_comments_text);
        newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
        textViewCharCount = (TextView) findViewById(R.id.id_text_view_comment_char_count);
        final TextView postCommentButton = (TextView) findViewById(R.id.id_text_view_post_new_comment_button);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        postCommentBundle = getIntent().getBundleExtra("POST_COMMENT_BUNDLE");

        new FetchUserPostDBTask().execute(postCommentBundle);

        // Determine if selfPost
        selfPost = userProfileParcel.getProfileNickname().equals(postCommentBundle.getString("POST_NICKNAME"));

        // Display Post Info Correctly
        if (postCommentBundle.getInt("COMMENTS_COUNT") <= 0) {
            noCommentsText.setText(getResources().getString(R.string.no_comments_message));
        } else {
            commentsContainer.removeView(noCommentsText);
            new FetchPostCommentsDBTask().execute(postCommentBundle.getString("POST_ID"));
        }

        Helpers.setFacebookProfileImage(this,
                                        mainPostProfileImage,
                                        postCommentBundle.getString("FACEBOOK_ID"),
                                        3);

        long tsLong = System.currentTimeMillis()/1000;
        final long timeInSeconds = tsLong - postCommentBundle.getLong("TIME_IN_SECONDS");
        if (mainPostTimeStamp != null) {
            if (timeInSeconds < 60) {
                String s = String.valueOf(timeInSeconds) + "s";
                mainPostTimeStamp.setText(s);
            } else if (timeInSeconds < 60 * 60) {
                long timeInMins = timeInSeconds / 60;
                String s = String.valueOf(timeInMins) + "m";
                mainPostTimeStamp.setText(s);
            } else if (timeInSeconds < 60 * 60 * 24) {
                long timeInHrs = timeInSeconds / 60 / 60;
                String s = String.valueOf(timeInHrs) + "h";
                mainPostTimeStamp.setText(s);
            } else {
                long timeInDays = timeInSeconds / 60 / 60 / 24;
                String s = String.valueOf(timeInDays) + "d";
                mainPostTimeStamp.setText(s);
            }
        }

        if (mainPostNickname != null && mainPostTextContent != null && mainPostLikesCount != null
                && postCommentButton != null) {
            int likesCount = postCommentBundle.getInt("LIKES_COUNT");
            mainPostNickname.setText(postCommentBundle.getString("POST_NICKNAME"));
            mainPostTextContent.setText(postCommentBundle.getString("TEXT_CONTENT"));
            mainPostCommentsCount.setText(String.valueOf(postCommentBundle.getInt("COMMENTS_COUNT")));
            mainPostLikesCount.setText(String.valueOf(likesCount));
            if (likesCount > 0)
                mainPostLikesCount.setTextColor(ContextCompat.getColor(this, R.color.colorGreen));
            else if (likesCount < 0)
                mainPostLikesCount.setTextColor(ContextCompat.getColor(this, R.color.colorRed));

            // Post Button onClick handler
            postCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (charCount < 0)
                        Toast.makeText(NewCommentActivity.this, "Comment too long!", Toast.LENGTH_SHORT).show();
                    else
                        addCommentToAdapter(newCommentText.getText().toString());
                }
            });

            newCommentText.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (Helpers.isKeyboardShown(newCommentText.getRootView()))
                        textViewCharCount.setVisibility(View.VISIBLE);
                    else
                        textViewCharCount.setVisibility(View.INVISIBLE);
                }
            });
            newCommentText.addTextChangedListener(new TextWatcher() {
                int prev_length = 0;
                public void afterTextChanged(Editable s) {
                    if (prev_length >= 200) {
                        textViewCharCount.setTextColor(Color.RED);
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    charCount -= (s.length() - prev_length);
                    textViewCharCount.setText(String.valueOf(charCount));
                    prev_length = s.length();
                }
            });
        }

        // Recycler View Setup
        recyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_post_comments);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete_post, menu);
        // Hide delete post menu option if not own post
        if (!selfPost) {
            menu.findItem(R.id.id_item_delete_post).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Hide soft keyboard if keyboard is up
//                EditText et = (EditText) findViewById(R.id.id_edit_text_new_comment);
                Helpers.hideSoftKeyboard(this, newCommentText);
                onBackPressed();
                return true;
            case R.id.id_item_delete_post:
                AlertDialog.Builder dialogBuilderConfirmDelete = new AlertDialog.Builder(this);
                View dialogViewConfirmDelete = View.inflate(this, R.layout.dialog_confirm_delete, null);
                dialogBuilderConfirmDelete.setView(dialogViewConfirmDelete);
                dialogBuilderConfirmDelete.setTitle("Confirm Delete");
                dialogBuilderConfirmDelete.setMessage("Are you sure you want to delete this post?");
                dialogBuilderConfirmDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new DeletePostFromDBTask().execute(postCommentBundle);
                    }
                });
                dialogBuilderConfirmDelete.setNegativeButton("No", null);
                dialogBuilderConfirmDelete.create().show();

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

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private void addCommentToAdapter(String newCommentText) {
        if (newCommentText == null || newCommentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty!", Toast.LENGTH_SHORT).show();
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putString("POST_NICKNAME", postCommentBundle.getString("POST_NICKNAME"));
            bundle.putString("CUR_USER_NICKNAME", userProfileParcel.getCurUserNickname());
            bundle.putString("FACEBOOK_ID", userProfileParcel.getFacebookID());
            bundle.putString("FIRST_NAME", userProfileParcel.getFirstname());
            if (commentCardAdapter == null)
                initializeAdapter(null);
            // Checks is this post is the first user comment
            if (postCommentBundle.getInt("COMMENTS_COUNT") == 0)
                commentsContainer.removeView(noCommentsText);
            commentCardAdapter.addComment(newCommentText, bundle);
        }
    }

    public Long getPostCommentBundleLong(String key) {
        return postCommentBundle.getLong(key);
    }

    private void initializeAdapter(List<DBUserComment> result) {
        List<DBUserComment> comments = new ArrayList<>();
        if (result != null) {
            for (DBUserComment userComment : result)
                comments.add(userComment);
        }
        // Initialize adapter for the case that the first comment is being made
        commentCardAdapter = new CommentCardAdapter(this, comments, userProfileParcel.getCurUserNickname());
        recyclerView.setAdapter(commentCardAdapter);
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    public void refreshAdapter() {
        new FetchUserPostDBTask().execute(postCommentBundle);
        new FetchPostCommentsDBTask().execute(postCommentBundle.getString("POST_ID"));
    }

    public void postAddCommentCleanup() {
        refreshAdapter();
        newCommentText.setText("");
        newCommentText.clearFocus();
        Helpers.hideSoftKeyboard(this, newCommentText);
    }

    private void refreshMainPostData(final DBUserPost userPost) {
        final ImageButton mainPostThumbsUp = (ImageButton) findViewById(R.id.id_image_button_comment_main_post_thumbs_up);
        final ImageButton mainPostThumbsDown = (ImageButton) findViewById(R.id.id_image_button_comment_main_post_thumbs_down);
        final TextView mainPostLikesCount = (TextView) findViewById(R.id.id_text_view_comment_main_post_likes);

        final String userNickname = userProfileParcel.getCurUserNickname();

        // Update comments count
        mainPostCommentsCount.setText(String.valueOf(userPost.getNumberOfComments()));

        // Display thumbs up/down icons:
        if (userPost.getLikedUsers().contains(userNickname) && mainPostThumbsUp != null)
            mainPostThumbsUp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_thumb_uped));
        else if (userPost.getDislikedUsers().contains(userNickname) && mainPostThumbsDown != null)
            mainPostThumbsDown.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_thumb_downed));

        // Button Click Events:
        if (mainPostLikesCount != null && mainPostThumbsUp != null &&
                mainPostThumbsDown != null) {
            mainPostThumbsUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentNumOfLikes = Integer.parseInt(mainPostLikesCount.getText().toString());
                    Set<String> likedUsers = userPost.getLikedUsers();
                    Set<String> dislikedUsers = userPost.getDislikedUsers();
                    // If user already liked the post
                    if (likedUsers.contains(userNickname)) {
                        currentNumOfLikes -= 1;
                        mainPostLikesCount.setText(String.valueOf(currentNumOfLikes));
                        mainPostThumbsUp.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_thumb_up));
                        userPost.setNumberOfLikes(currentNumOfLikes);
                        // Special transition cases
                        if (currentNumOfLikes == 0) {
                            mainPostLikesCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        } else if (currentNumOfLikes == -1) {
                            mainPostLikesCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRed));
                        }
                        // Remove user from likedUsers set
                        userPost.removeLikedUsers(userNickname);
                        new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                                new AsyncHelpers.asyncTaskObjectUserPostBundle(userPost, dbHelper.getMapper(), null, null));
                    }
                    // If user has not liked the post yet
                    else {
                        // lose previous dislike and +1 like
                        if (dislikedUsers.contains(userNickname)) {
                            currentNumOfLikes += 2;
                        } else {
                            currentNumOfLikes += 1;
                        }
                        mainPostLikesCount.setText(String.valueOf(currentNumOfLikes));
                        mainPostThumbsUp.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_thumb_uped));
                        mainPostThumbsDown.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_thumb_down));
                        userPost.setNumberOfLikes(currentNumOfLikes);
                        if (currentNumOfLikes == 0) {
                            mainPostLikesCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        } else if (currentNumOfLikes == 1) {
                            mainPostLikesCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));
                        }
                        // Remove user from dislikedUsers set and add to likedUsers set
                        userPost.addLikedUsers(userNickname);
                        userPost.removedislikedUsers(userNickname);
                        new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                                new AsyncHelpers.asyncTaskObjectUserPostBundle(userPost, dbHelper.getMapper(), null, null));
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
                    if (dislikedUsers.contains(userNickname)) {
                        currentNumOfLikes += 1;
                        mainPostLikesCount.setText(String.valueOf(currentNumOfLikes));
                        mainPostThumbsDown.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_thumb_down));
                        userPost.setNumberOfLikes(currentNumOfLikes);
                        // Special transition cases
                        if (currentNumOfLikes == 0) {
                            mainPostLikesCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        } else if (currentNumOfLikes == 1) {
                            mainPostLikesCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));
                        }
                        // Remove user from likedUsers set
                        userPost.removedislikedUsers(userNickname);
                        new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                                new AsyncHelpers.asyncTaskObjectUserPostBundle(userPost, dbHelper.getMapper(), null, null));
                    }
                    // If user has not disliked the post yet
                    else {
                        if (likedUsers.contains(userNickname)) {
                            // lose previous like and +1 dislike
                            currentNumOfLikes -= 2;
                        } else {
                            currentNumOfLikes -= 1;
                        }
                        mainPostLikesCount.setText(String.valueOf(currentNumOfLikes));
                        mainPostThumbsUp.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_thumb_up));
                        mainPostThumbsDown.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_thumb_downed));
                        userPost.setNumberOfLikes(currentNumOfLikes);
                        if (currentNumOfLikes == 0) {
                            mainPostLikesCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        } else if (currentNumOfLikes == -1) {
                            mainPostLikesCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRed));
                        }
                        // Remove user from dislikedUsers set and add to likedUsers set
                        userPost.adddislikedUsers(userNickname);
                        userPost.removeLikedUsers(userNickname);
                        new AsyncHelpers.PushUserPostChangesToDBTask().execute(
                                new AsyncHelpers.asyncTaskObjectUserPostBundle(userPost, dbHelper.getMapper(), null, null));
                    }
                }
            });
        }
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchUserPostDBTask extends AsyncTask<Bundle, Void, Void> {
        DBUserPost userPost;
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle data = params[0];
            userPost = dbHelper.loadDBPost(data.getString("POST_NICKNAME"), data.getLong("TIME_IN_SECONDS"));
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            refreshMainPostData(userPost);
        }
    }

    private class DeletePostFromDBTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle data = params[0];
            // Delete UserPost
            DBUserPost post = dbHelper.loadDBPost(data.getString("POST_NICKNAME"), data.getLong("TIME_IN_SECONDS"));
            dbHelper.deleteDBObject(post);

            // Update UserProfile post count
            DBUserProfile userProfile = dbHelper.loadDBUserProfile(data.getString("POST_NICKNAME"));
            int curPostCount = userProfile.getPostsCount();
            userProfile.setPostsCount(curPostCount - 1);
            dbHelper.saveDBObject(userProfile);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Hide soft keyboard if keyboard is up
//            EditText et = (EditText) findViewById(R.id.id_edit_text_new_comment);
            Helpers.hideSoftKeyboard(NewCommentActivity.this, newCommentText);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    private class FetchPostCommentsDBTask extends AsyncTask<String, Void, PaginatedQueryList<DBUserComment>> {
        @Override
        protected PaginatedQueryList<DBUserComment> doInBackground(String... params) {
            String postID = params[0];
            DBUserComment userComment = new DBUserComment();
            userComment.setPostID(postID);
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(userComment)
                    .withConsistentRead(false);
            return dbHelper.getMapper().query(DBUserComment.class, queryExpression);
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBUserComment> result) {
            initializeAdapter(result);
        }
    }
}
