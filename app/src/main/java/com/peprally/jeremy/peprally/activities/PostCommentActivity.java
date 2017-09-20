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
import android.widget.TextView;
import android.widget.Toast;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.CommentCardAdapter;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.custom.ui.EmptyViewSwipeRefreshLayout;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.network.ApiManager;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.network.callbacks.UserResponsePostCommentCallback;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PostCommentActivity extends AppCompatActivity{

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private RecyclerView recyclerView;
    private ProgressDialog progressDialogDeletePost;
    private EmptyViewSwipeRefreshLayout postCommentsSwipeRefreshContainer;

    // AWS/HTTP Variables
    private DynamoDBHelper dynamoDBHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // General Variables
    private static final String TAG = PostCommentActivity.class.getSimpleName();
    private CommentCardAdapter commentCardAdapter;
    private DBUserPost mainPost;
    private UserProfileParcel userProfileParcel;
    private boolean isUserViewingOwnPost;       // if current post is user's own post
    private int newCommentCharCount = 200;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment);

        // set up network helpers
        dynamoDBHelper = new DynamoDBHelper(this);
        httpRequestsHelper = new HTTPRequestsHelper(this);

        // get activity arguments
        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        userProfileParcel.setCurrentActivity(ActivityEnum.POSTCOMMENT);
        mainPost = getIntent().getParcelableExtra("MAIN_POST");

        // setup UI components
        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_post_comments);
        final LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        // setup swipe refresh container
        postCommentsSwipeRefreshContainer = (EmptyViewSwipeRefreshLayout) findViewById(R.id.container_swipe_refresh_post_comments);
        postCommentsSwipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAdapter(false);
            }
        });

        // Setup main post container
        final ImageView mainPostProfileImage = (ImageView) findViewById(R.id.id_image_view_comment_main_post);
        final TextView mainPostTimeStamp = (TextView) findViewById(R.id.id_text_view_post_card_time_stamp);
        final TextView mainPostUsername = (TextView) findViewById(R.id.id_text_view_comment_main_post_username);
        final TextView mainPostTextContent = (TextView) findViewById(R.id.id_text_view_comment_main_post_content);
        final EditText editTextNewComment = (EditText) findViewById(R.id.id_edit_text_new_comment);
        final TextView textViewCharCount = (TextView) findViewById(R.id.id_text_view_comment_char_count);
        final TextView postCommentButton = (TextView) findViewById(R.id.id_text_view_button_new_comment_post);

        if (mainPostUsername != null && mainPostTextContent != null && postCommentButton != null
                && mainPost != null && userProfileParcel != null) {

            // determine if isUserViewingOwnPost
            isUserViewingOwnPost = userProfileParcel.getCurrentUsername().equals(mainPost.getUsername());

            // always try loading comments to ensure post has not been deleted yet
            refreshAdapter(false);

            // display main post info correctly
            Helpers.setFacebookProfileImage(this,
                    mainPostProfileImage,
                    mainPost.getFacebookId(),
                    Helpers.FacebookProfilePictureEnum.LARGE,
                    true);

            mainPostTimeStamp.setText(Helpers.getTimetampString(mainPost.getTimestampSeconds(), true));

            mainPostUsername.setText(mainPost.getUsername());
            mainPostTextContent.setText(mainPost.getPostText());
            refreshMainPostData(mainPost);

            // profile image onclick handler
            mainPostProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
	                ApiManager.getInstance()
			                .getLoginService()
			                .getUserProfileWithUsername(mainPost.getUsername())
			                .enqueue(new UserResponsePostCommentCallback(getApplicationContext(), userProfileParcel.getCurrentUsername(), null));
                }
            });

            // post button onclick handler
            postCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (newCommentCharCount < 0) {
                        Toast.makeText(PostCommentActivity.this, "Comment too long!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // disable post button so user can't spam posts
                        postCommentButton.setClickable(false);
                        addCommentToAdapter(editTextNewComment.getText().toString().trim());
                    }

                }
            });

            // real time update number of characters left on comment
            editTextNewComment.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (Helpers.isKeyboardShown(editTextNewComment.getRootView()))
                        textViewCharCount.setVisibility(View.VISIBLE);
                    else
                        textViewCharCount.setVisibility(View.INVISIBLE);
                }
            });
            editTextNewComment.addTextChangedListener(new TextWatcher() {
                int prev_length = 0;
                public void afterTextChanged(Editable s) {
                    if (prev_length >= 200)
                        textViewCharCount.setTextColor(Color.RED);
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    newCommentCharCount -= (s.length() - prev_length);
                    textViewCharCount.setText(String.valueOf(newCommentCharCount));
                    prev_length = s.length();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        // Hide delete post menu option if not own post
        if (!isUserViewingOwnPost) {
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
     *********************************** GENERAL_METHODS ********************************
     **********************************************************************************************/
    private void addCommentToAdapter(String newCommentText) {
        if (newCommentText == null || newCommentText.isEmpty()) {
            Toast.makeText(this, "Comment can't be empty!", Toast.LENGTH_SHORT).show();
            // re-enable post button
            final TextView postCommentButton = (TextView) findViewById(R.id.id_text_view_button_new_comment_post);
            postCommentButton.setClickable(true);
        }
        else {
            // when adding the first comment, initialize commentCardAdapter with null list
            if (commentCardAdapter == null)
                initializeAdapter(null, false);
            commentCardAdapter.addComment(newCommentText, userProfileParcel.getCurrentUsername());
        }
    }

    private Bundle makeDBNotificationBundlePostFistbump(DBUserPost curPost) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_FISTBUMP.toInt());
        bundle.putString("RECEIVER_USERNAME", curPost.getUsername());    // who the notification is going to
        bundle.putString("POST_ID", curPost.getPostId());
        return bundle;
    }

    private Bundle makePushNotificationBundlePostFistbump(DBUserPost curPost) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_FISTBUMP.toInt());
        bundle.putString("SENDER_USERNAME", userProfileParcel.getCurrentUsername());
        bundle.putString("RECEIVER_USERNAME", curPost.getUsername());
        return bundle;
    }

    private void initializeAdapter(List<Comment> results, boolean scrollToBottom) {
        ArrayList<Comment> comments = new ArrayList<>();
        if (results != null && results.size() > 0) {
            for (Comment userComment : results)
                comments.add(userComment);
        }
        commentCardAdapter = new CommentCardAdapter(this, comments, userProfileParcel, mainPost);
        recyclerView.swapAdapter(commentCardAdapter, true);

        // stop refresh loading animation
        if (postCommentsSwipeRefreshContainer.isRefreshing())
            postCommentsSwipeRefreshContainer.setRefreshing(false);

        if (scrollToBottom)
            recyclerView.scrollToPosition(comments.size() - 1);
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
        postCommentsSwipeRefreshContainer.setRefreshing(true);
        new FetchUserPostDBTask(true).execute();
        // reset new comment edit text
        final EditText newCommentText = (EditText) findViewById(R.id.id_edit_text_new_comment);
        newCommentText.setText("");
        newCommentText.clearFocus();
        Helpers.hideSoftKeyboard(getApplicationContext(), newCommentText);

        // re-enable post button
        final TextView postCommentButton = (TextView) findViewById(R.id.id_text_view_button_new_comment_post);
        postCommentButton.setClickable(true);
    }

    public void postDeleteCommentCleanup() {
        new FetchUserPostDBTask(false).execute();
    }

    private void refreshAdapter(boolean scrollToBottom) {
        // start refresh animation
        postCommentsSwipeRefreshContainer.post(new Runnable() {
            @Override
            public void run() {
                postCommentsSwipeRefreshContainer.setRefreshing(true);
            }
        });
        new FetchUserPostDBTask(scrollToBottom).execute();
    }

    private void toggleDeletingPostLoadingDialog(boolean show) {
        if (show)
            progressDialogDeletePost = ProgressDialog.show(PostCommentActivity.this, "Delete PostLikeResponse", "Deleting ... ", true);
        else
            progressDialogDeletePost.dismiss();
    }

    private void refreshMainPostData(final DBUserPost userPost) {
        final TextView mainPostFistbump = (TextView) findViewById(R.id.id_button_comment_main_post_fistbump);
        final TextView mainPostFistbumpsCount = (TextView) findViewById(R.id.id_text_view_post_card_fistbumps_count);
        final TextView mainPostCommentsCount = (TextView) findViewById(R.id.id_text_view_post_card_comments_count);

        final String username = userProfileParcel.getCurrentUsername();

        // Setup button views and onClick handling callbacks:
        if (mainPostFistbump != null && mainPostFistbumpsCount != null) {

            // Update fistbumps/comments count
            mainPostFistbumpsCount.setText(String.valueOf(userPost.getFistbumpsCount()));
            mainPostCommentsCount.setText(String.valueOf(userPost.getCommentsCount()));
            mainPostCommentsCount.setCompoundDrawablesWithIntrinsicBounds(
                    Helpers.getAPICompatVectorDrawable(getApplicationContext(), R.drawable.ic_replies), null, null, null);

            // if user already liked the post
            if (userPost.getFistbumpedUsers() != null && userPost.getFistbumpedUsers().contains(username)) {
                mainPostFistbump.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                mainPostFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
            }

            mainPostFistbumpsCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mainPost.getFistbumpsCount() > 0) {
                        dynamoDBHelper.doActionIfDBUserPostAndCommentExists(
                                mainPost,
                                null,       // don't need to check comment
                                new DynamoDBHelper.AsyncTaskCallbackWithReturnObject() {
                                    @Override
                                    public void onTaskDone(Object bundle) {
                                        if (((Bundle) bundle).getBoolean("POST_AND_COMMENT_EXISTS", false)) {   // assume worst case, that post/comment was deleted
                                            Intent intent = new Intent(PostCommentActivity.this, ViewFistbumpsActivity.class);
                                            intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                                            intent.putExtra("USER_POST", mainPost);
                                            startActivity(intent);
                                        } else {
                                            launchPostOrCommentIsDeletedDialog(((Bundle) bundle).getBoolean("IS_POST_DELETED", true)); // assume post was deleted
                                        }

                                    }
                                });
                    }
                }
            });

            // fistbump button onclick handler
            mainPostFistbump.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Set<String> fistbumpedUsers = userPost.getFistbumpedUsers();
                    // if user already liked the post
                    if (fistbumpedUsers != null && fistbumpedUsers.contains(username)) {
                        int fistbumpsCount = Integer.parseInt(mainPostFistbumpsCount.getText().toString()) - 1;
                        // remove user from fistbumpedUsers set and update post fistbumps count
                        userPost.removeFistbumpedUser(username);
                        userPost.setFistbumpsCount(fistbumpsCount);
                        // update UI
                        mainPostFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                        mainPostFistbump.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
                        mainPostFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_20, 0, 0, 0);
                        // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                        if (!userPost.getUsername().equals(userProfileParcel.getCurrentUsername())) {
                            // remove notification and also update fistbump counts respectively
                            dynamoDBHelper.deletePostFistbumpNotification(
                                    userPost.getPostId(),
                                    userProfileParcel.getCurrentUsername(),
                                    userPost.getUsername(),
                                    new DynamoDBHelper.AsyncTaskCallbackWithReturnObject() {
                                        @Override
                                        public void onTaskDone(Object bundle) {
                                            if (((Bundle) bundle).getBoolean("TASK_SUCCESS", false)) {  // assume notification wasn't created
                                                dynamoDBHelper.saveDBObjectAsync(userPost);
                                            } else {
                                                launchPostOrCommentIsDeletedDialog(((Bundle) bundle).getBoolean("IS_POST_DELETED", true));   // assume post is deleted
                                            }
                                        }
                                    });
                        } else {
                            // if I made changes to my own post, just save the post right away, I don't
                            // need to check if the post has been deleted
                            dynamoDBHelper.saveDBObjectAsync(userPost);
                        }
                    }
                    // If user has not liked the post yet
                    else {
                        int fistbumpsCount = Integer.parseInt(mainPostFistbumpsCount.getText().toString()) + 1;
                        // add user to fistbumpedUsers set and update post fistbumps count
                        userPost.addFistbumpedUser(username);
                        userPost.setFistbumpsCount(fistbumpsCount);
                        // update UI
                        mainPostFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                        mainPostFistbump.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                        mainPostFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
                        // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                        if (!userPost.getUsername().equals(userProfileParcel.getCurrentUsername())) {
                            dynamoDBHelper.createNewNotification(makeDBNotificationBundlePostFistbump(userPost),
                                    new DynamoDBHelper.AsyncTaskCallbackWithReturnObject(){
                                        @Override
                                        public void onTaskDone(Object bundle) {
                                            if (((Bundle) bundle).getBoolean("TASK_SUCCESS", false)) {  // assume notification wasn't created
                                                // send push notification
                                                httpRequestsHelper.makePushNotificationRequest(makePushNotificationBundlePostFistbump(userPost));
                                                dynamoDBHelper.saveDBObjectAsync(userPost);
                                            } else {
                                                launchPostOrCommentIsDeletedDialog(((Bundle) bundle).getBoolean("IS_POST_DELETED", true));   // assume post is deleted
                                            }
                                        }
                                    });
                        } else {
                            // if I made changes to my own post, just save the post right away, I don't
                            // need to check if the post has been deleted
                            dynamoDBHelper.saveDBObjectAsync(userPost);
                        }
                    }
                }
            });
        }
    }

    /***********************************************************************************************
     **************************************** GENERAL_METHODS **************************************
     **********************************************************************************************/
    public void launchUserAccountIsDeletedDialog(final String deletedCommentId) {
        final AlertDialog.Builder dialogBuilderCommentDeleted = new AlertDialog.Builder(PostCommentActivity.this);
        final View dialogViewConfirmDelete = View.inflate(PostCommentActivity.this, R.layout.dialog_confirm_delete, null);
        dialogBuilderCommentDeleted.setView(dialogViewConfirmDelete);
        dialogBuilderCommentDeleted.setTitle("Oops!");
        dialogBuilderCommentDeleted.setMessage("Looks like this user has deleted his/her account!");
        dialogBuilderCommentDeleted.setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new RemovePostCommentAsyncTask().execute(deletedCommentId);
            }
        });

        AlertDialog b = dialogBuilderCommentDeleted.create();
        b.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                new RemovePostCommentAsyncTask().execute(deletedCommentId);
            }
        });

        b.show();
    }

    public void launchPostOrCommentIsDeletedDialog(final boolean isPostDeleted) {
        final AlertDialog.Builder dialogBuilderPostDeleted = new AlertDialog.Builder(this);
        final View dialogViewConfirmDelete = View.inflate(this, R.layout.dialog_confirm_delete, null);
        dialogBuilderPostDeleted.setView(dialogViewConfirmDelete);
        dialogBuilderPostDeleted.setTitle("Oops!");
        if (isPostDeleted)
            dialogBuilderPostDeleted.setMessage("Looks like this post has been deleted!");
        else
            dialogBuilderPostDeleted.setMessage("Looks like this comment has been deleted!");
        dialogBuilderPostDeleted.setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (isPostDeleted) onBackPressed();
                else refreshAdapter(false);
            }
        });

        AlertDialog b = dialogBuilderPostDeleted.create();
        b.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (isPostDeleted) onBackPressed();
                else refreshAdapter(false);
            }
        });

        b.show();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchUserPostDBTask extends AsyncTask<Void, Void, DBUserPost> {

        private boolean scrollToBottom;

        private FetchUserPostDBTask(boolean scrollToBottom) {
            this.scrollToBottom = scrollToBottom;
        }

        @Override
        protected DBUserPost doInBackground(Void... params) {
            DBUserPost userPost = dynamoDBHelper.loadDBUserPost(mainPost.getUsername(), mainPost.getTimestampSeconds());
            if (userPost != null) {
                ArrayList<Comment> postComments = userPost.getComments();
                for (int i = 0; i < postComments.size(); ++i) {
                    if (dynamoDBHelper.loadDBUserProfile(postComments.get(i).getCommentUsername()) == null) {
                        postComments.remove(i);
                    }
                }

                userPost.setComments(postComments);
                userPost.setCommentsCount(postComments.size());
                dynamoDBHelper.saveDBObject(userPost);
            }
            return userPost;
        }

        @Override
        protected void onPostExecute(DBUserPost userPost) {
            if (userPost == null) {
                launchPostOrCommentIsDeletedDialog(true);   // post is deleted
            } else {
                refreshMainPostData(userPost);
                // update cached copy of mainPost
                mainPost = userPost;

                initializeAdapter(userPost.getComments(), scrollToBottom);

                // stop refresh animation
                postCommentsSwipeRefreshContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        postCommentsSwipeRefreshContainer.setRefreshing(false);
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private class RemovePostCommentAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String commentId = strings[0];
            ArrayList<Comment> postComments = mainPost.getComments();
            for (int i = 0; i < postComments.size(); ++i) {
                if (postComments.get(i).getCommentId().equals(commentId)) {
                    postComments.remove(i);
                    break;
                }
            }
            mainPost.setComments(postComments);
            dynamoDBHelper.saveDBObject(mainPost);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            refreshAdapter(false);
        }
    }
	
}
