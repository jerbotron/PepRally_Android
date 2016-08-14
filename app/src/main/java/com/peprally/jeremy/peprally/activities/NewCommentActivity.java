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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.CommentCardAdapter;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NewCommentActivity extends AppCompatActivity{

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private RecyclerView recyclerView;
    private ProgressDialog progressDialogDeletePost;
    private RelativeLayout progressCircleContainer;
    private SwipeRefreshLayout postCommentsSwipeRefreshContainer;

    // AWS/HTTP Variables
    private DynamoDBHelper dynamoDBHelper;
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

        dynamoDBHelper = new DynamoDBHelper(this);
        httpRequestsHelper = new HTTPRequestsHelper(this);

        // Setup toolbar
        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Setup recycler view
        recyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_post_comments);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        // Setup main post container
        final ImageView mainPostProfileImage = (ImageView) findViewById(R.id.id_image_view_comment_main_post);
        final TextView mainPostTimeStamp = (TextView) findViewById(R.id.id_text_view_post_card_time_stamp);
        final TextView mainPostUsername = (TextView) findViewById(R.id.id_text_view_comment_main_post_username);
        final TextView mainPostTextContent = (TextView) findViewById(R.id.id_text_view_comment_main_post_content);
        final EditText newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
        final TextView textViewCharCount = (TextView) findViewById(R.id.id_text_view_comment_char_count);
        final TextView postCommentButton = (TextView) findViewById(R.id.id_text_view_button_new_comment_post);
        progressCircleContainer = (RelativeLayout) findViewById(R.id.id_container_comments_progress_circle);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        mainPost = getIntent().getParcelableExtra("MAIN_POST");

        if (mainPostUsername != null && mainPostTextContent != null && postCommentButton != null
                && mainPost != null && userProfileParcel != null) {

            // Determine if selfPost
            selfPost = userProfileParcel.getProfileUsername().equals(mainPost.getUsername());

            // Display Post Info Correctly
            refreshMainPostData(mainPost);
            // stop on load progress circle animation
            if (mainPost.getCommentsCount() > 0) {
                initializeAdapter(mainPost.getComments());
            }

            mainPostUsername.setText(mainPost.getUsername());
            mainPostTextContent.setText(mainPost.getPostText());

            Helpers.setFacebookProfileImage(this,
                    mainPostProfileImage,
                    mainPost.getFacebookId(),
                    3,
                    true);

            mainPostTimeStamp.setText(Helpers.getTimetampString(mainPost.getTimestampSeconds()));

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

        // setup swipe refresh container
        postCommentsSwipeRefreshContainer = (SwipeRefreshLayout) findViewById(R.id.container_swipe_refresh_post_comments);
        postCommentsSwipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAdapter();
            }
        });

        // stop on load progress circle animation
        progressCircleContainer.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        // Hide delete post menu option if not own post
        if (!selfPost) {
            menu.findItem(R.id.id_item_delete).setVisible(false);
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
            case R.id.id_item_delete:
                AlertDialog.Builder dialogBuilderConfirmDelete = new AlertDialog.Builder(this);
                View dialogViewConfirmDelete = View.inflate(this, R.layout.dialog_confirm_delete, null);
                dialogBuilderConfirmDelete.setView(dialogViewConfirmDelete);
                dialogBuilderConfirmDelete.setTitle("Confirm Delete");
                dialogBuilderConfirmDelete.setMessage("Are you sure you want to delete this post?");
                dialogBuilderConfirmDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        toggleDeletingPostLoadingDialog(true);
                        // delete all notifications related to this post
                        dynamoDBHelper.batchDeletePostNotifications(mainPost);
                        // delete user post and all post comments
                        dynamoDBHelper.deleteUserPost(mainPost, new DynamoDBHelper.AsyncTaskCallback() {
                            @Override
                            public void onTaskDone() {
                                onPostDeletePostEventHandler();
                            }
                        });
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
            bundle.putString("POST_USERNAME", mainPost.getUsername());
            bundle.putString("COMMENT_USERNAME", userProfileParcel.getCurUsername());
            bundle.putString("COMMENT_FIRST_NAME", userProfileParcel.getFirstname());
            bundle.putString("FACEBOOK_ID", userProfileParcel.getFacebookID());
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
        bundle.putString("RECEIVER_USERNAME", curPost.getUsername());    // who the notification is going to
        bundle.putString("POST_ID", curPost.getPostId());
        return bundle;
    }

    private Bundle makeHTTPPostRequestPostFistbumpBundle(DBUserPost curPost) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_FISTBUMP.toInt());
        bundle.putString("RECEIVER_USERNAME", curPost.getUsername());
        bundle.putString("SENDER_USERNAME", userProfileParcel.getCurUsername());
        return bundle;
    }

    private void initializeAdapter(List<Comment> results) {
        ArrayList<Comment> comments = new ArrayList<>();
        if (results != null && results.size() > 0) {
            for (Comment userComment : results)
                comments.add(userComment);
        }
        commentCardAdapter = new CommentCardAdapter(this, comments, userProfileParcel, mainPost);
        recyclerView.swapAdapter(commentCardAdapter, true);
    }

    public void onPostDeletePostEventHandler() {
        // dismiss the progress dialog
        toggleDeletingPostLoadingDialog(false);

        // Hide soft keyboard if keyboard is up
        EditText newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
        Helpers.hideSoftKeyboard(getApplicationContext(), newCommentText);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
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

        final String username = userProfileParcel.getCurUsername();

        // Setup button views and onClick handling callbacks:
        if (mainPostFistbump != null && mainPostFistbumpsCount != null) {

            // Update fistbumps/comments count
            mainPostFistbumpsCount.setText(String.valueOf(userPost.getFistbumpsCount()));
            mainPostCommentsCount.setText(String.valueOf(userPost.getCommentsCount()));

            // if user already liked the post
            if (userPost.getFistbumpedUsers().contains(username)) {
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
                    if (fistbumpedUsers.contains(username)) {
                        fistbumpsCount -= 1;
                        mainPostFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                        mainPostFistbump.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
                        mainPostFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_20, 0, 0, 0);

                        // remove user from fistbumpedUsers set
                        userPost.removeFistbumpedUser(username);

                        // update user fistbumps counts:
                        // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                        if (!userPost.getUsername().equals(userProfileParcel.getCurUsername())) {
                            // update the received fistbumps count of the main post user
                            dynamoDBHelper.decrementUserReceivedFistbumpsCount(userPost.getUsername());
                            // update the sent fistbumps count of the current user
                            dynamoDBHelper.decrementUserSentFistbumpsCount(userProfileParcel.getCurUsername());
                            // remove notification
                            dynamoDBHelper.deletePostFistbumpNotification(NotificationEnum.POST_FISTBUMP, userPost.getPostId(), userProfileParcel.getCurUsername());
                        }
                        // remove current user from fistbumped users
                        userPost.removeFistbumpedUser(userProfileParcel.getCurUsername());
                    }
                    // If user has not liked the post yet
                    else {
                        fistbumpsCount += 1;
                        mainPostFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                        mainPostFistbump.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                        mainPostFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);

                        // add user to fistbumpedUsers set
                        userPost.addFistbumpedUser(username);

                        // update user fistbumps counts:
                        // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                        if (!userPost.getUsername().equals(userProfileParcel.getCurUsername())) {
                            // update the received fistbumps count of the main post user
                            dynamoDBHelper.incrementUserReceivedFistbumpsCount(userPost.getUsername());
                            // update the sent fistbumps count of the current user
                            dynamoDBHelper.incrementUserSentFistbumpsCount(userProfileParcel.getCurUsername());
                            // send push notification
                            dynamoDBHelper.createNewNotification(makeNotificationPostFistbumpBundle(userPost));
                            httpRequestsHelper.makePushNotificationRequest(makeHTTPPostRequestPostFistbumpBundle(userPost));
                        }
                        // add current user to fistbumped users
                        userPost.addFistbumpedUser(userProfileParcel.getCurUsername());
                    }
                    // update post fistbumps count
                    userPost.setFistbumpsCount(fistbumpsCount);
                    dynamoDBHelper.saveDBObjectAsync(userPost);
                }
            });
        }
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchUserPostDBTask extends AsyncTask<DBUserPost, Void, DBUserPost> {
        @Override
        protected DBUserPost doInBackground(DBUserPost... params) {
            DBUserPost userPost = params[0];
            return dynamoDBHelper.loadDBUserPost(userPost.getUsername(), userPost.getTimestampSeconds());
        }

        @Override
        protected void onPostExecute(DBUserPost userPost) {
            refreshMainPostData(userPost);
            // update cached copy of mainPost
            mainPost = userPost;

            if (userPost.getCommentsCount() > 0)
                initializeAdapter(userPost.getComments());

            // stop refresh loading animation
            if (postCommentsSwipeRefreshContainer.isRefreshing())
                postCommentsSwipeRefreshContainer.setRefreshing(false);

            // stop on load progress circle animation
            if (progressCircleContainer != null)
                progressCircleContainer.setVisibility(View.GONE);
        }
    }
}
