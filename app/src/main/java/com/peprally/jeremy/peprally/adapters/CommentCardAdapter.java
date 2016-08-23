package com.peprally.jeremy.peprally.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.PostCommentActivity;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.activities.ViewFistbumpsActivity;
import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CommentCardAdapter extends RecyclerView.Adapter<CommentCardAdapter.CommentHolder> {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS/HTTP Variables
    private DynamoDBHelper dynamoDBHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // General Variables
    private Context callingContext;
    private ArrayList<Comment> comments;
    private UserProfileParcel userProfileParcel;
    private DBUserPost mainPost;

    // UI Variables
    private ProgressDialog progressDialogDeleteComment;

    /***********************************************************************************************
     ********************************** ADAPTER CONSTRUCTOR/METHODS ********************************
     **********************************************************************************************/
    public CommentCardAdapter(Context callingContext,
                              ArrayList<Comment> comments,
                              UserProfileParcel userProfileParcel,
                              DBUserPost mainPost) {
        this.comments = comments;
        this.callingContext = callingContext;
        this.userProfileParcel = userProfileParcel;
        this.mainPost = mainPost;
        dynamoDBHelper = new DynamoDBHelper(callingContext);
        httpRequestsHelper = new HTTPRequestsHelper(callingContext);
    }

    static class CommentHolder extends RecyclerView.ViewHolder {
        RelativeLayout commentContainer;
        CardView cardView;
        ImageView profileImage;
        TextView timestamp;
        TextView username;
        TextView commentText;
        TextView fistbumpButton;
        TextView fistbumpsCount;

        private CommentHolder(View itemView) {
            super(itemView);
            commentContainer = (RelativeLayout) itemView.findViewById(R.id.id_container_comment_clickable);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_new_comment);
            profileImage = (ImageView) itemView.findViewById(R.id.id_image_view_comment_profile);
            username = (TextView) itemView.findViewById(R.id.id_text_view_comment_username);
            timestamp = (TextView) itemView.findViewById(R.id.id_text_view_comment_time_stamp);
            commentText = (TextView) itemView.findViewById(R.id.id_text_view_comment_content);
            fistbumpButton = (TextView) itemView.findViewById(R.id.id_button_comment_fistbump);
            fistbumpsCount = (TextView) itemView.findViewById(R.id.id_text_view_comment_fistbumps_count);
        }
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_new_comment_container, parent, false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentHolder commentHolder, int position) {
        final Comment currentComment = comments.get(position);
        final String currentUsername = userProfileParcel.getCurrentUsername();
        final String commentUsername = currentComment.getCommentUsername();
        Set<String> fistbumpedUsers = currentComment.getFistbumpedUsers();

        Helpers.setFacebookProfileImage(callingContext,
                                        commentHolder.profileImage,
                                        currentComment.getFacebookId(),
                                        3,
                                        true);

        if (fistbumpedUsers.contains(currentUsername)) {
            commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
        }

        commentHolder.username.setText(commentUsername);
        commentHolder.commentText.setText(currentComment.getCommentText());
        commentHolder.fistbumpsCount.setText(String.valueOf(currentComment.getFistbumpsCount()));

        commentHolder.timestamp.setText(Helpers.getTimetampString(currentComment.getTimestampSeconds(), true));

        // profile picture onclick handler
        commentHolder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(callingContext, ProfileActivity.class);
                // clicked on own profile
                if (commentUsername.equals(currentUsername)) {
                    intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                }
                // clicked on another user's profile
                else {
                    UserProfileParcel parcel = new UserProfileParcel(ActivityEnum.PROFILE,
                            currentUsername,
                            currentComment);
                    intent.putExtra("USER_PROFILE_PARCEL", parcel);
                }
                callingContext.startActivity(intent);
                ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        // fistbumps count button onclick handler:
        commentHolder.fistbumpsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentComment.getFistbumpsCount() > 0) {
                    Intent intent = new Intent(callingContext, ViewFistbumpsActivity.class);
                    intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                    intent.putStringArrayListExtra("FISTBUMPED_USERS", new ArrayList<>(currentComment.getFistbumpedUsers()));
                    callingContext.startActivity(intent);
                }
            }
        });

        // fistbump button onclick handler:
        commentHolder.fistbumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fistbumpsCount = currentComment.getFistbumpsCount();
                Set<String> fistbumpedUsers = currentComment.getFistbumpedUsers();
                // If user already liked the comment
                if (fistbumpedUsers.contains(currentUsername)) {
                    fistbumpsCount--;
                    commentHolder.fistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
                    // update fistbumps counts
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!commentUsername.equals(currentUsername)) {
                        // update the received fistbumps count of the main post user
                        dynamoDBHelper.decrementUserReceivedFistbumpsCount(commentUsername);
                        // update the sent fistbumps count of the current user
                        dynamoDBHelper.decrementUserSentFistbumpsCount(currentUsername);
                        // remove notification
                        dynamoDBHelper.deleteCommentFistbumpNotification(NotificationEnum.COMMENT_FISTBUMP, currentComment.getCommentId(), commentUsername);
                    }
                    // remove current user from fistbumped users
                    currentComment.removeFistbumpedUser(currentUsername);
                }
                // If user has not liked the comment yet
                else {
                    fistbumpsCount++;
                    commentHolder.fistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                    // update fistbumps counts:
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!commentUsername.equals(currentUsername)) {
                        // update the received fistbumps count of the main post user
                        dynamoDBHelper.incrementUserReceivedFistbumpsCount(commentUsername);
                        // update the sent fistbumps count of the current user
                        dynamoDBHelper.incrementUserSentFistbumpsCount(currentUsername);
                        // send push notification
                        dynamoDBHelper.createNewNotification(makeNotificationCommentFistbumpBundle(currentComment), null);
                        httpRequestsHelper.makePushNotificationRequest(makeHTTPPostRequestCommentFistbumpBundle(currentComment));
                    }
                    // add current user to fistbumped users
                    currentComment.addFistbumpedUser(currentUsername);

                }
                // update comment fistbumps count and save new comments to post and save post
                currentComment.setFistbumpsCount(fistbumpsCount);
                mainPost.setComments(comments);
                dynamoDBHelper.saveDBObjectAsync(mainPost);
            }
        });

        // Delete comment event
        commentHolder.commentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commentUsername.equals(currentUsername))
                    launchDeleteCommentDialog(currentComment, commentHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    /***********************************************************************************************
     *********************************** GENERAL_METHODS ********************************
     **********************************************************************************************/
    private Bundle makeNotificationCommentFistbumpBundle(Comment comment) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.COMMENT_FISTBUMP.toInt());
        bundle.putString("RECEIVER_USERNAME", comment.getCommentUsername());    // who the notification is going to
        bundle.putString("POST_ID", comment.getPostId());
        bundle.putString("COMMENT_ID", comment.getCommentId());
        return bundle;
    }

    private Bundle makeNotificationPostCommentBundle(String comment, String commentID) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_COMMENT.toInt());
        bundle.putString("RECEIVER_USERNAME", mainPost.getUsername());
        bundle.putString("POST_ID", mainPost.getPostId());
        bundle.putString("COMMENT_ID", commentID);
        bundle.putString("COMMENT", comment);
        return bundle;
    }

    private Bundle makeHTTPPostRequestCommentFistbumpBundle(Comment comment) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.COMMENT_FISTBUMP.toInt());
        bundle.putString("RECEIVER_USERNAME", comment.getCommentUsername());
        bundle.putString("SENDER_USERNAME", userProfileParcel.getCurrentUsername());
        bundle.putString("SENDER_FACEBOOK_ID", userProfileParcel.getFacebookID());
        return bundle;
    }

    private Bundle makeHTTPPostRequestPostCommentBundle(String comment) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_COMMENT.toInt());
        bundle.putString("RECEIVER_USERNAME", mainPost.getUsername());
        bundle.putString("SENDER_USERNAME", userProfileParcel.getCurrentUsername());
        bundle.putString("SENDER_FACEBOOK_ID", userProfileParcel.getFacebookID());
        bundle.putString("COMMENT", comment);
        return bundle;
    }

    private void toggleDeletingCommentLoadingDialog(boolean show) {
        if (show)
            progressDialogDeleteComment = ProgressDialog.show(callingContext, "Delete Comment", "Deleting ... ", true);
        else
            progressDialogDeleteComment.dismiss();
    }

    private void adapterRemoveItemAt(int position) {
        comments.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    public void addComment(String commentText, Bundle bundle) {
        Long timestampSeconds = Helpers.getTimestampSeconds();
        Long postTimeInSeconds = mainPost.getTimestampSeconds();

        Comment newComment = new Comment(
                bundle.getString("POST_USERNAME") + "_" + postTimeInSeconds.toString(),
                bundle.getString("COMMENT_USERNAME") + "_" + timestampSeconds.toString(),
                bundle.getString("COMMENT_USERNAME"),
                bundle.getString("COMMENT_FIRST_NAME"),
                bundle.getString("POST_USERNAME"),
                bundle.getString("FACEBOOK_ID"),
                commentText,
                timestampSeconds,
                0,
                new HashSet<>(Collections.singletonList("_"))
        );

        dynamoDBHelper.addNewPostComment(newComment, new DynamoDBHelper.AsyncTaskCallback() {
            @Override
            public void onTaskDone() {
                ((PostCommentActivity) callingContext).postAddCommentCleanup();
            }
        });

        // send push notification (if not commenting on own post)
        if (!newComment.getCommentUsername().equals(newComment.getPostUsername())) {
            dynamoDBHelper.createNewNotification(makeNotificationPostCommentBundle(commentText, newComment.getCommentId()), null);
            httpRequestsHelper.makePushNotificationRequest(makeHTTPPostRequestPostCommentBundle(commentText));
        }
    }

    private void launchDeleteCommentDialog(final Comment comment, final int position) {
        // First "Delete comment" dialog
        AlertDialog.Builder dialogBuilderDelete = new AlertDialog.Builder(callingContext);
        final View dialogViewDelete = View.inflate(callingContext, R.layout.dialog_delete, null);
        dialogBuilderDelete.setView(dialogViewDelete);
        TextView textViewDelete = (TextView) dialogViewDelete.findViewById(R.id.id_text_view_comment_delete_button);

        // Second "Confirm delete" dialog
        final AlertDialog.Builder dialogBuilderConfirmDelete = new AlertDialog.Builder(callingContext);
        final View dialogViewConfirmDelete = View.inflate(callingContext, R.layout.dialog_confirm_delete, null);
        dialogBuilderConfirmDelete.setView(dialogViewConfirmDelete);
        dialogBuilderConfirmDelete.setTitle("Confirm Delete");
        dialogBuilderConfirmDelete.setMessage("Are you sure you want to delete this comment?");
        dialogBuilderConfirmDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // launch delete comment progress dialog
                toggleDeletingCommentLoadingDialog(true);
                // delete post comment
                dynamoDBHelper.deletePostComment(position, mainPost, new DynamoDBHelper.AsyncTaskCallback() {
                    @Override
                    public void onTaskDone() {
                        adapterRemoveItemAt(position);
                        ((PostCommentActivity) callingContext).postDeleteCommentCleanup();
                        // dismiss comment delete loading dialog
                        toggleDeletingCommentLoadingDialog(false);
                    }
                });
                // remove notification if comment is not by the same post user
                if (!comment.getCommentUsername().equals(mainPost.getUsername()))
                    dynamoDBHelper.deletePostCommentNotification(NotificationEnum.POST_COMMENT, comment);
            }
        });
        dialogBuilderConfirmDelete.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });


        final AlertDialog deleteDialog = dialogBuilderDelete.create();
        final AlertDialog confirmDeleteDialog = dialogBuilderConfirmDelete.create();

        textViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
                confirmDeleteDialog.show();
            }
        });

        deleteDialog.show();
    }
}
