package com.peprally.jeremy.peprally.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
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
import com.peprally.jeremy.peprally.activities.ViewFistbumpsActivity;
import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.data.UserPost;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.model.BaseResponse;
import com.peprally.jeremy.peprally.model.PostDeletionResponse;
import com.peprally.jeremy.peprally.model.PostResponse;
import com.peprally.jeremy.peprally.network.ApiManager;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.network.NetworkHelpers;
import com.peprally.jeremy.peprally.network.callbacks.UserResponsePostCommentCallback;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.ArrayList;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private UserPost mainPost;

    // UI Variables
    private ProgressDialog progressDialogDeleteComment;

    /***********************************************************************************************
     ********************************** ADAPTER CONSTRUCTOR/METHODS ********************************
     **********************************************************************************************/
    public CommentCardAdapter(Context callingContext,
                              ArrayList<Comment> comments,
                              UserProfileParcel userProfileParcel,
                              UserPost mainPost) {
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
    public void onBindViewHolder(final CommentHolder commentHolder, final int position) {
        final Comment currentComment = comments.get(position);
        final String currentUsername = userProfileParcel.getCurrentUsername();
        final String commentUsername = currentComment.getCommentUsername();
        Set<String> fistbumpedUsers = currentComment.getFistbumpedUsers();

        Helpers.setFacebookProfileImage(callingContext,
                                        commentHolder.profileImage,
                                        currentComment.getFacebookId(),
                                        Helpers.FacebookProfilePictureEnum.LARGE,
                                        true);

        if (fistbumpedUsers != null && fistbumpedUsers.contains(currentUsername)) {
            commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
        } else {
            commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
        }

        commentHolder.username.setText(commentUsername);
        commentHolder.commentText.setText(currentComment.getCommentText());
        commentHolder.fistbumpsCount.setText(String.valueOf(currentComment.getFistbumpsCount()));

        commentHolder.timestamp.setText(Helpers.getTimetampString(currentComment.getTimestampSeconds(), true));

        // profile picture onclick handler
        commentHolder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApiManager.getInstance()
                        .getLoginService()
                        .getUserProfileWithUsername(mainPost.getUsername())
                        .enqueue(new UserResponsePostCommentCallback(callingContext,
                                                                    currentUsername,
                                                                    new DynamoDBHelper.AsyncTaskCallback() {
	                                                                    @Override
	                                                                    public void onTaskDone() {
		                                                                    ((PostCommentActivity) callingContext).launchUserAccountIsDeletedDialog(currentComment.getCommentId());
	                                                                    }
                                                                    }));
            }
        });

        // fistbumps count button onclick handler:
        commentHolder.fistbumpsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentComment.getFistbumpsCount() > 0) {
                    Pair<String, Long> postQueryParams = NetworkHelpers.parsePostId(mainPost.getPostId());
                    ApiManager.getInstance()
                            .getPostService()
                            .getPost(postQueryParams.first, postQueryParams.second)
                            .enqueue(new Callback<PostResponse>() {
                                @Override
                                public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                                    PostResponse postResponse = response.body();
                                    if (postResponse != null) {
                                        if (postResponse.getPost().hasComment(currentComment.getCommentId())) {
                                            Intent intent = new Intent(callingContext, ViewFistbumpsActivity.class);
                                            intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                                            intent.putExtra("USER_POST", mainPost);
                                            intent.putExtra("COMMENT_INDEX", commentHolder.getAdapterPosition());
                                            callingContext.startActivity(intent);
                                        } else {
                                            ((PostCommentActivity) callingContext).launchPostOrCommentIsDeletedDialog(true);
                                        }
                                    } else {
                                        ((PostCommentActivity) callingContext).launchPostOrCommentIsDeletedDialog(false);
                                    }
                                }

                                @Override
                                public void onFailure(Call<PostResponse> call, Throwable throwable) {
                                    ApiManager.handleCallbackFailure(throwable);
                                }
                            });
                }
            }
        });

        // fistbump button onclick handler:
        commentHolder.fistbumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Set<String> fistbumpedUsers = currentComment.getFistbumpedUsers();
                int fistbumpsCount;
                // If user already liked the comment
                if (fistbumpedUsers != null && fistbumpedUsers.contains(currentUsername)) {
                    // update db items: remove current user from fistbumped users
                    fistbumpsCount = currentComment.getFistbumpsCount() - 1;
                    currentComment.removeFistbumpedUser(currentUsername);
                    currentComment.setFistbumpsCount(fistbumpsCount);
                    commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);

                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!commentUsername.equals(currentUsername)) {
                        ApiManager.getInstance()
                                .getNotificationService()
                                .deleteNotification(currentComment.getPostId(), currentComment.getCommentId())
                                .enqueue(new Callback<PostDeletionResponse>() {
                                    @Override
                                    public void onResponse(Call<PostDeletionResponse> call, Response<PostDeletionResponse> response) {}

                                    @Override
                                    public void onFailure(Call<PostDeletionResponse> call, Throwable throwable) {
                                        ApiManager.handleCallbackFailure(throwable);
                                    }
                                });
                    }
                }
                // If user has not liked the comment yet
                else {
                    // update db items: add current user from fistbumped users
                    fistbumpsCount = currentComment.getFistbumpsCount() + 1;
                    currentComment.addFistbumpedUser(currentUsername);
                    currentComment.setFistbumpsCount(fistbumpsCount);
                    commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);

                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!commentUsername.equals(currentUsername)) {
                        ApiManager.getInstance()
                                .getNotificationService()
                                .createNewNotification(currentComment)
                                .enqueue(new Callback<BaseResponse>() {
                                    @Override
                                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                                        if (response.body() != null) {
                                            // send push notification once db user notification is successfully created
                                            httpRequestsHelper.makePushNotificationRequest(makePushNotificationBundleCommentFistbump(currentComment));
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<BaseResponse> call, Throwable throwable) {
                                        ApiManager.handleCallbackFailure(throwable);
                                    }
                                });
                    }
                }

                commentHolder.fistbumpsCount.setText(String.valueOf(fistbumpsCount));
                mainPost.setComments(comments);
                ApiManager.getInstance().getPostService().updatePost(mainPost)
                        .enqueue(new Callback<PostDeletionResponse>() {
                            @Override
                            public void onResponse(Call<PostDeletionResponse> call, Response<PostDeletionResponse> response) {
                                PostDeletionResponse postDeletionResponse = response.body();
                                if (postDeletionResponse != null) {
                                    if (postDeletionResponse.isPostDeleted()) {
                                        ((PostCommentActivity) callingContext).launchPostOrCommentIsDeletedDialog(true);
                                    } else if (postDeletionResponse.isCommentDeleted()){
                                        ((PostCommentActivity) callingContext).launchPostOrCommentIsDeletedDialog(false);
                                    }
                                }

                            }

                            @Override
                            public void onFailure(Call<PostDeletionResponse> call, Throwable throwable) {
                                ApiManager.handleCallbackFailure(throwable);
                            }
                        });
            }
        });

        // delete comment event
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
    private Bundle makeDBNotificationBundleCommentFistbump(Comment comment) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.COMMENT_FISTBUMP.toInt());
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putString("RECEIVER_USERNAME", comment.getCommentUsername());
        bundle.putString("POST_ID", comment.getPostId());
        bundle.putString("COMMENT_ID", comment.getCommentId());
        return bundle;
    }

    private Bundle makeDBNotificationBundlePostComment(String comment, String commentID) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_COMMENT.toInt());
        bundle.putString("RECEIVER_USERNAME", mainPost.getUsername());
        bundle.putString("POST_ID", mainPost.getPostId());
        bundle.putString("COMMENT_ID", commentID);
        bundle.putString("COMMENT", comment);
        return bundle;
    }

    private Bundle makePushNotificationBundleCommentFistbump(Comment comment) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.COMMENT_FISTBUMP.toInt());
        bundle.putString("SENDER_USERNAME", userProfileParcel.getCurrentUsername());
        bundle.putString("RECEIVER_USERNAME", comment.getCommentUsername());
        return bundle;
    }

    private Bundle makePushNotificationBundlePostComment(String comment) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_COMMENT.toInt());
        bundle.putString("SENDER_USERNAME", userProfileParcel.getCurrentUsername());
        bundle.putString("RECEIVER_USERNAME", mainPost.getUsername());
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
    public void addComment(final String commentText, final String commentUsername) {
        Long timestampSeconds = Helpers.getTimestampSeconds();
        Bundle bundle = new Bundle();
        bundle.putString("POST_ID", Helpers.getPostCommentIdString(mainPost.getUsername(), mainPost.getTimestampSeconds()));
        bundle.putString("POST_USERNAME", mainPost.getUsername());
        bundle.putString("COMMENT_ID", Helpers.getPostCommentIdString(commentUsername, timestampSeconds));
        bundle.putString("COMMENT_USERNAME", commentUsername);
        bundle.putLong("TIMESTAMP", timestampSeconds);
        bundle.putString("COMMENT_TEXT", commentText);

        dynamoDBHelper.createNewPostComment(bundle, new DynamoDBHelper.AsyncTaskCallback() {
            @Override
            public void onTaskDone() {
                ((PostCommentActivity) callingContext).postAddCommentCleanup();
            }
        });

        // create new user notification (if not commenting on own post)
        if (!commentUsername.equals(mainPost.getUsername())) {
            dynamoDBHelper.createNewNotification(
                    makeDBNotificationBundlePostComment(commentText, Helpers.getPostCommentIdString(commentUsername, timestampSeconds)),
                    new DynamoDBHelper.AsyncTaskCallbackWithReturnObject() {
                        @Override
                        public void onTaskDone(Object bundle) {
                            if (((Bundle) bundle).getBoolean("TASK_SUCCESS", false)) {  // assume notification was not created
                                httpRequestsHelper.makePushNotificationRequest(makePushNotificationBundlePostComment(commentText));
                            } else {
                                ((PostCommentActivity) callingContext).launchPostOrCommentIsDeletedDialog(((Bundle) bundle).getBoolean("IS_POST_DELETED", true)); // assume post is deleted
                            }
                        }
                    });
        }
    }

    private void launchDeleteCommentDialog(final Comment comment, final int position) {
        // First "Delete comment" dialog
        AlertDialog.Builder dialogBuilderDelete = new AlertDialog.Builder(callingContext);
        final View dialogViewDelete = View.inflate(callingContext, R.layout.dialog_delete_comment, null);
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
                    dynamoDBHelper.deletePostCommentNotification(comment.getPostId(), comment.getCommentId(), null);

                dynamoDBHelper.batchDeleteCommentFistbumpNotifications(NotificationEnum.COMMENT_FISTBUMP, comment);
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
