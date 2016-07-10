package com.peprally.jeremy.peprally.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.HTTPRequestsHelper;
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
    private TextView mainPostCommentsCount, textViewCharCount;
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
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set up main post container
        final ImageView mainPostProfileImage = (ImageView) findViewById(R.id.id_image_view_comment_main_post);
        final TextView mainPostTimeStamp = (TextView) findViewById(R.id.id_text_view_post_card_time_stamp);
        final TextView mainPostNickname = (TextView) findViewById(R.id.id_text_view_comment_main_post_nickname);
        final TextView mainPostTextContent = (TextView) findViewById(R.id.id_text_view_comment_main_post_content);
        mainPostCommentsCount = (TextView) findViewById(R.id.id_text_view_post_card_comments_count);
        newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
        textViewCharCount = (TextView) findViewById(R.id.id_text_view_comment_char_count);
        final TextView postCommentButton = (TextView) findViewById(R.id.id_text_view_post_new_comment_button);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        postCommentBundle = getIntent().getBundleExtra("POST_COMMENT_BUNDLE");

        new FetchUserPostDBTask().execute(postCommentBundle);

        // Determine if selfPost
        selfPost = userProfileParcel.getProfileNickname().equals(postCommentBundle.getString("POST_NICKNAME"));

        // Display Post Info Correctly
        if (postCommentBundle.getInt("COMMENTS_COUNT") > 0)
            new FetchPostCommentsDBTask().execute(postCommentBundle.getString("POST_ID"));

        Helpers.setFacebookProfileImage(this,
                                        mainPostProfileImage,
                                        postCommentBundle.getString("FACEBOOK_ID"),
                                        3);

        mainPostTimeStamp.setText(Helpers.getTimeStampString(postCommentBundle.getLong("TIME_IN_SECONDS")));

        if (mainPostNickname != null && mainPostTextContent != null && postCommentButton != null) {
            mainPostNickname.setText(postCommentBundle.getString("POST_NICKNAME"));
            mainPostTextContent.setText(postCommentBundle.getString("TEXT_CONTENT"));

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
    public Long getPostCommentBundleLong(String key) {
        return postCommentBundle.getLong(key);
    }

    private void addCommentToAdapter(String newCommentText) {
        Log.d("NEW COMMENT ACTIVITY: ", newCommentText);
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
            commentCardAdapter.addComment(newCommentText, bundle);
            // send push notification
            dbHelper.sendNewNotification(makeNotificationPostCommentBundle(newCommentText));
//            new HTTPRequestsHelper.requestPOSTTask().execute(
//                    new HTTPRequestsHelper.HTTPPostRequestObject(getApplicationContext(),
//                                                                 postCommentBundle.getString("POST_NICKNAME"),
//                                                                 userProfileParcel.getCurUserNickname(),
//                                                                 newCommentText));
        }
    }

    private Bundle makeNotificationPostCommentBundle(String comment) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putInt("TYPE", 1);
        bundle.putString("NICKNAME", postCommentBundle.getString("POST_NICKNAME"));
        bundle.putString("POST_ID", postCommentBundle.getString("POST_ID"));
        bundle.putString("COMMENT", comment);
        return bundle;
    }

    private void initializeAdapter(List<DBUserComment> results) {
        List<DBUserComment> comments = new ArrayList<>();
        if (results != null && results.size() > 0) {
            for (DBUserComment userComment : results)
                comments.add(userComment);
        }
        // Initialize adapter for the case that the first comment is being made
        commentCardAdapter = new CommentCardAdapter(this, comments, userProfileParcel);
        recyclerView.setAdapter(commentCardAdapter);
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void refreshAdapter() {
        new FetchUserPostDBTask().execute(postCommentBundle);
        new FetchPostCommentsDBTask().execute(postCommentBundle.getString("POST_ID"));
    }

    public void postDeleteCommentCleanup() {
        new FetchUserPostDBTask().execute(postCommentBundle);
    }

    public void postAddCommentCleanup() {
        new FetchUserPostDBTask().execute(postCommentBundle);
        newCommentText.setText("");
        newCommentText.clearFocus();
        Helpers.hideSoftKeyboard(this, newCommentText);
    }

    private void refreshMainPostData(final DBUserPost userPost) {
        final TextView mainPostFistbump = (TextView) findViewById(R.id.id_button_comment_main_post_fistbump);
        final TextView mainPostFistbumpsCount = (TextView) findViewById(R.id.id_text_view_post_card_fistbumps_count);

        final String userNickname = userProfileParcel.getCurUserNickname();

        // Setup button views and onClick handling callbacks:
        if (mainPostFistbump != null && mainPostFistbumpsCount != null) {

            // Update fistbumps/comments count
            mainPostFistbumpsCount.setText(String.valueOf(userPost.getFistbumpsCount()));
            mainPostCommentsCount.setText(String.valueOf(userPost.getNumberOfComments()));

            // if user already liked the post
            if (userPost.getFistbumpedUsers().contains(userNickname)) {
                mainPostFistbump.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                mainPostFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
            }

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
                        }
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
                        }
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
