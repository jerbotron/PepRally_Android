package com.peprally.jeremy.peprally.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.NotificationEnum;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewCommentActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private RecyclerView recyclerView;
    private ProgressDialog progressDialogDeletePost;
    private RelativeLayout progressCircleContainer;
    private SwipeRefreshLayout postCommentsSwipeRefreshContainer;

    // AWS/HTTP Variables
    private DynamoDBHelper dbHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // General Variables
    private static final String TAG = NewCommentActivity.class.getSimpleName();
    private CommentCardAdapter commentCardAdapter;
    private DBUserPost mainPost;
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
        httpRequestsHelper = new HTTPRequestsHelper(this);

        // Set up toolbar buttons
        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set up main post container
        final ImageView mainPostProfileImage = (ImageView) findViewById(R.id.id_image_view_comment_main_post);
        final TextView mainPostTimeStamp = (TextView) findViewById(R.id.id_text_view_post_card_time_stamp);
        final TextView mainPostNickname = (TextView) findViewById(R.id.id_text_view_comment_main_post_nickname);
        final TextView mainPostTextContent = (TextView) findViewById(R.id.id_text_view_comment_main_post_content);
        final EditText newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
        final  TextView textViewCharCount = (TextView) findViewById(R.id.id_text_view_comment_char_count);
        final TextView postCommentButton = (TextView) findViewById(R.id.id_text_view_post_new_comment_button);
        progressCircleContainer = (RelativeLayout) findViewById(R.id.id_container_comments_progress_circle);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        mainPost = getIntent().getParcelableExtra("MAIN_POST");

        if (mainPostNickname != null && mainPostTextContent != null && postCommentButton != null
                && mainPost != null && userProfileParcel != null) {

            // Determine if selfPost
            selfPost = userProfileParcel.getProfileNickname().equals(mainPost.getNickname());

            // Load comments if applicable
            if (mainPost.getCommentsCount() > 0)
                new FetchPostCommentsDBTask().execute(mainPost.getPostId());
            else {
                progressCircleContainer.setVisibility(View.GONE);
            }

            // Display Post Info Correctly
            refreshMainPostData(mainPost);

            mainPostNickname.setText(mainPost.getNickname());
            mainPostTextContent.setText(mainPost.getTextContent());

            Helpers.setFacebookProfileImage(this,
                    mainPostProfileImage,
                    mainPost.getFacebookId(),
                    3);

            mainPostTimeStamp.setText(Helpers.getTimetampString(mainPost.getTimeInSeconds()));

            // Post Button onClick handler
            postCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (charCount < 0)
                        Toast.makeText(NewCommentActivity.this, "Comment too long!", Toast.LENGTH_SHORT).show();
                    else
                        addCommentToAdapter(newCommentText.getText().toString().trim());
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

        // setup swipe refresh container
        postCommentsSwipeRefreshContainer = (SwipeRefreshLayout) findViewById(R.id.container_swipe_refresh_post_comments);
        postCommentsSwipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAdapter();
            }
        });
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
                EditText newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
                Helpers.hideSoftKeyboard(getApplicationContext(), newCommentText);
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
                        toggleDeletingPostLoadingDialog(true);
                        // delete all notifications related to this post
                        dbHelper.batchDeletePostNotifications(mainPost);
                        // handle post deletion sequentially starting with updating post fistbumps
                        new UpdateFistbumpsCountAfterPostDeleteDBTask().execute(mainPost);
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
            Toast.makeText(this, "Comment can't be empty!", Toast.LENGTH_SHORT).show();
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putString("POST_NICKNAME", mainPost.getNickname());
            bundle.putString("CUR_USER_NICKNAME", userProfileParcel.getCurUserNickname());
            bundle.putString("FACEBOOK_ID", userProfileParcel.getFacebookID());
            bundle.putString("FIRST_NAME", userProfileParcel.getFirstname());
            // when adding the first comment, initialize commentCardAdapter with null list
            if (commentCardAdapter == null)
                initializeAdapter(null);
            commentCardAdapter.addComment(newCommentText, bundle);
        }
    }

    private Bundle makeNotificationPostFistbumpBundle(DBUserPost curPost) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_FISTBUMP.toInt());
        bundle.putString("RECEIVER_NICKNAME", curPost.getNickname());    // who the notification is going to
        bundle.putString("POST_ID", curPost.getPostId());
        return bundle;
    }

    private Bundle makeHTTPPostRequestPostFistbumpBundle(DBUserPost curPost) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_FISTBUMP.toInt());
        bundle.putString("RECEIVER_NICKNAME", curPost.getNickname());
        bundle.putString("SENDER_NICKNAME", userProfileParcel.getCurUserNickname());
        return bundle;
    }

    private void initializeAdapter(List<DBUserComment> results) {
        List<DBUserComment> comments = new ArrayList<>();
        if (results != null && results.size() > 0) {
            for (DBUserComment userComment : results)
                comments.add(userComment);
        }
        commentCardAdapter = new CommentCardAdapter(this, comments, userProfileParcel, mainPost);
        recyclerView.setAdapter(commentCardAdapter);
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    public void postAddCommentCleanup() {
        new FetchUserPostDBTask().execute(mainPost);
        EditText newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
        newCommentText.setText("");
        newCommentText.clearFocus();
        Helpers.hideSoftKeyboard(getApplicationContext(), newCommentText);
    }

    public void postDeleteCommentCleanup() {
        new FetchUserPostDBTask().execute(mainPost);
    }

    private void refreshAdapter() {
        new FetchUserPostDBTask().execute(mainPost);
        new FetchPostCommentsDBTask().execute(mainPost.getPostId());
    }

    private void toggleDeletingPostLoadingDialog(boolean show) {
        if (show)
            progressDialogDeletePost = ProgressDialog.show(NewCommentActivity.this, "Delete Post", "Deleting ... ", true);
        else
            progressDialogDeletePost.dismiss();
    }

    private void refreshMainPostData(final DBUserPost userPost) {
        final TextView mainPostFistbump = (TextView) findViewById(R.id.id_button_comment_main_post_fistbump);
        final TextView mainPostFistbumpsCount = (TextView) findViewById(R.id.id_text_view_post_card_fistbumps_count);
        final TextView mainPostCommentsCount = (TextView) findViewById(R.id.id_text_view_post_card_comments_count);

        final String userNickname = userProfileParcel.getCurUserNickname();

        // Setup button views and onClick handling callbacks:
        if (mainPostFistbump != null && mainPostFistbumpsCount != null) {

            // Update fistbumps/comments count
            mainPostFistbumpsCount.setText(String.valueOf(userPost.getFistbumpsCount()));
            mainPostCommentsCount.setText(String.valueOf(userPost.getCommentsCount()));

            // if user already liked the post
            if (userPost.getFistbumpedUsers().contains(userNickname)) {
                mainPostFistbump.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                mainPostFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
            }

            mainPostFistbumpsCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mainPost.getFistbumpsCount() > 0) {
                        Intent intent = new Intent(getApplicationContext(), ViewFistbumpsActivity.class);
                        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                        intent.putStringArrayListExtra("FISTBUMPED_USERS", new ArrayList<>(mainPost.getFistbumpedUsers()));
                        startActivity(intent);
                    }
                }
            });

            // fistbump button onclick handler
            mainPostFistbump.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int fistbumpsCount = Integer.parseInt(mainPostFistbumpsCount.getText().toString());
                    Set<String> fistbumpedUsers = userPost.getFistbumpedUsers();
                    // if user already liked the post
                    if (fistbumpedUsers.contains(userNickname)) {
                        fistbumpsCount -= 1;
                        mainPostFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                        mainPostFistbump.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
                        mainPostFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_20, 0, 0, 0);

                        // remove user from fistbumpedUsers set
                        userPost.removeFistbumpedUser(userNickname);

                        // update user fistbumps counts:
                        // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                        if (!userPost.getNickname().equals(userProfileParcel.getCurUserNickname())) {
                            // update the received fistbumps count of the main post user
                            dbHelper.decrementUserReceivedFistbumpsCount(userPost.getNickname());
                            // update the sent fistbumps count of the current user
                            dbHelper.decrementUserSentFistbumpsCount(userProfileParcel.getCurUserNickname());
                            // remove notification
                            dbHelper.deletePostFistbumpNotification(NotificationEnum.POST_FISTBUMP, userPost.getPostId(), userProfileParcel.getCurUserNickname());
                        }
                        // remove current user from fistbumped users
                        userPost.removeFistbumpedUser(userProfileParcel.getCurUserNickname());
                    }
                    // If user has not liked the post yet
                    else {
                        fistbumpsCount += 1;
                        mainPostFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                        mainPostFistbump.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                        mainPostFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);

                        // add user to fistbumpedUsers set
                        userPost.addFistbumpedUser(userNickname);

                        // update user fistbumps counts:
                        // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                        if (!userPost.getNickname().equals(userProfileParcel.getCurUserNickname())) {
                            // update the received fistbumps count of the main post user
                            dbHelper.incrementUserReceivedFistbumpsCount(userPost.getNickname());
                            // update the sent fistbumps count of the current user
                            dbHelper.incrementUserSentFistbumpsCount(userProfileParcel.getCurUserNickname());
                            // send push notification
                            dbHelper.makeNewNotification(makeNotificationPostFistbumpBundle(userPost));
                            httpRequestsHelper.makePushNotificationRequest(makeHTTPPostRequestPostFistbumpBundle(userPost));
                        }
                        // add current user to fistbumped users
                        userPost.addFistbumpedUser(userProfileParcel.getCurUserNickname());
                    }
                    // update post fistbumps count
                    userPost.setFistbumpsCount(fistbumpsCount);
                    dbHelper.saveDBObjectAsync(userPost);
                }
            });
        }
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/

    // Fetching Tasks

    private class FetchUserPostDBTask extends AsyncTask<DBUserPost, Void, DBUserPost> {
        @Override
        protected DBUserPost doInBackground(DBUserPost... params) {
            DBUserPost userPost = params[0];
            return dbHelper.loadDBUserPost(userPost.getNickname(), userPost.getTimeInSeconds());
        }

        @Override
        protected void onPostExecute(DBUserPost userPost) {
            refreshMainPostData(userPost);
            // update cached copy of mainPost
            mainPost = userPost;

            // stop refresh loading animation
            if (postCommentsSwipeRefreshContainer.isRefreshing())
                postCommentsSwipeRefreshContainer.setRefreshing(false);
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
                    .withConsistentRead(true);
            return dbHelper.getMapper().query(DBUserComment.class, queryExpression);
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBUserComment> result) {
            initializeAdapter(result);

            // stop on load progress circle animation
            if (progressCircleContainer != null)
                progressCircleContainer.setVisibility(View.GONE);
        }
    }

    private class UpdateFistbumpsCountAfterPostDeleteDBTask extends AsyncTask<DBUserPost, Void, DBUserPost> {
        @Override
        protected DBUserPost doInBackground(DBUserPost... params) {
            DBUserPost userPost = params[0];
            if (userPost != null && userPost.getFistbumpsCount() > 0) {
                DBUserProfile postUserProfile = dbHelper.loadDBUserProfile(userPost.getNickname());

                // decrement post user received fistbumps count
                postUserProfile.setReceivedFistbumpsCount(postUserProfile.getReceivedFistbumpsCount() - userPost.getFistbumpsCount());
                dbHelper.saveDBObject(postUserProfile);

                // decrement fistbumped users' sent fistbumps count
                for (String nickname : userPost.getFistbumpedUsers()) {
                    DBUserProfile fistbumpedUser = dbHelper.loadDBUserProfile(nickname);
                    if (fistbumpedUser != null && !nickname.equals(userPost.getNickname())) {
                        fistbumpedUser.decrementSentFistbumpsCount();
                        dbHelper.saveDBObject(fistbumpedUser);
                    }
                }
            }
            return userPost;
        }

        @Override
        protected void onPostExecute(DBUserPost userPost) {
            new DeletePostCommentsDBTask().execute(userPost);
        }
    }

    // Deletion Tasks

    private class DeletePostCommentsDBTask extends AsyncTask<DBUserPost, Void, DBUserPost> {
        @Override
        protected DBUserPost doInBackground(DBUserPost... params) {
            DBUserPost userPost = params[0];

            if (userPost != null && userPost.getCommentsCount() > 0) {
                // query for all the comments under this post
                DBUserComment userComment = new DBUserComment();
                userComment.setPostID(userPost.getPostId());
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(userComment)
                        .withConsistentRead(true);
                List<DBUserComment> results = dbHelper.getMapper().query(DBUserComment.class, queryExpression);

                // delete the comments under the post and update all corresponding user fistbumps
                Map<String, Integer> fistbumpedUsersMap = new HashMap<>();
                for (DBUserComment comment : results) {
                    if (comment.getFistbumpsCount() > 0) {
                        // decrement comment user's received fistbumps by fistbumpsCount on the comment
                        DBUserProfile commentUserProfile = dbHelper.loadDBUserProfile(comment.getNickname());
                        if (commentUserProfile != null) {
                            commentUserProfile.setReceivedFistbumpsCount(commentUserProfile.getReceivedFistbumpsCount() - comment.getFistbumpsCount());
                            dbHelper.saveDBObject(commentUserProfile);
                        }
                        // accumulate total sent fistbumps for each user who fistbumped comments under this post
                        for (String nickname : comment.getFistbumpedUsers()) {
                            if (fistbumpedUsersMap.containsKey(nickname))
                                fistbumpedUsersMap.put(nickname, fistbumpedUsersMap.get(nickname) + 1);
                            else
                                fistbumpedUsersMap.put(nickname, 1);
                        }
                    }
                    userPost.decrementCommentsCount();
                    dbHelper.deleteDBObject(comment);
                }

                // update sentFistbumpsCount for all users who fistbumped comments under this post
                if (fistbumpedUsersMap.size() != 0) {
                    for (String nickname : fistbumpedUsersMap.keySet()) {
                        DBUserProfile userProfile = dbHelper.loadDBUserProfile(nickname);
                        if (userProfile != null) {
                            userProfile.setSentFistbumpsCount(userProfile.getSentFistbumpsCount() - fistbumpedUsersMap.get(nickname));
                            dbHelper.saveDBObject(userProfile);
                        }
                    }
                }
                dbHelper.saveDBObject(userPost);
            }
            return userPost;
        }

        @Override
        protected void onPostExecute(DBUserPost userPost) {
            new DeletePostFromDBTask().execute(userPost);
        }
    }

    private class DeletePostFromDBTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost userPost = params[0];
            if (userPost != null) {
                DBUserProfile userProfile = dbHelper.loadDBUserProfile(userPost.getNickname());

                // decrement userProfile post count
                userProfile.decrementPostCount();
                dbHelper.saveDBObject(userProfile);

                // delete user post
                dbHelper.deleteDBObject(userPost);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // dismiss the progress dialog
            toggleDeletingPostLoadingDialog(false);

            // Hide soft keyboard if keyboard is up
            EditText newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
            Helpers.hideSoftKeyboard(getApplicationContext(), newCommentText);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }
}
