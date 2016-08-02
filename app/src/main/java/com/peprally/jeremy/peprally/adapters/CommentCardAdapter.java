package com.peprally.jeremy.peprally.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.peprally.jeremy.peprally.activities.NewCommentActivity;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.activities.ViewFistbumpsActivity;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.utils.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.NotificationEnum;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommentCardAdapter extends RecyclerView.Adapter<CommentCardAdapter.CommentHolder> {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS/HTTP Variables
    private DynamoDBHelper dbHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // General Variables
    private Context callingContext;
    private List<DBUserComment> comments;
    private UserProfileParcel userProfileParcel;
    private DBUserPost mainPost;

    // UI Variables
    private ProgressDialog progressDialogDeleteComment;

    /***********************************************************************************************
     ********************************** ADAPTER CONSTRUCTOR/METHODS ********************************
     **********************************************************************************************/
    public CommentCardAdapter(Context callingContext,
                              List<DBUserComment> comments,
                              UserProfileParcel userProfileParcel,
                              DBUserPost mainPost) {
        this.comments = comments;
        this.callingContext = callingContext;
        this.userProfileParcel = userProfileParcel;
        this.mainPost = mainPost;
        dbHelper = new DynamoDBHelper(callingContext);
        httpRequestsHelper = new HTTPRequestsHelper(callingContext);
    }

    static class CommentHolder extends RecyclerView.ViewHolder {
        RelativeLayout commentContainer;
        CardView cardView;
        ImageView profileImage;
        TextView timeStamp;
        TextView nickname;
        TextView postContent;
        TextView fistbumpButton;
        TextView fistbumpsCount;

        private CommentHolder(View itemView) {
            super(itemView);
            commentContainer = (RelativeLayout) itemView.findViewById(R.id.id_container_comment_clickable);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_new_comment);
            profileImage = (ImageView) itemView.findViewById(R.id.id_image_view_comment_profile);
            nickname = (TextView) itemView.findViewById(R.id.id_text_view_comment_nickname);
            timeStamp = (TextView) itemView.findViewById(R.id.id_text_view_comment_time_stamp);
            postContent = (TextView) itemView.findViewById(R.id.id_text_view_comment_content);
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
        final DBUserComment curComment = comments.get(position);
        Helpers.setFacebookProfileImage(callingContext,
                                        commentHolder.profileImage,
                                        curComment.getFacebookID(),
                                        3);

        Set<String> fistbumpedUsers = curComment.getFistbumpedUsers();
        final String curUserNickname = userProfileParcel.getCurUserNickname();
        if (fistbumpedUsers.contains(curUserNickname)) {
            commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
        }

        commentHolder.nickname.setText(curComment.getNickname());
        commentHolder.postContent.setText(curComment.getTextContent());
        int fistbumpsCount = curComment.getFistbumpsCount();
        commentHolder.fistbumpsCount.setText(String.valueOf(fistbumpsCount));

        commentHolder.timeStamp.setText(Helpers.getTimetampString(curComment.getTimeInSeconds()));

        // profile picture onclick handler
        commentHolder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(callingContext, ProfileActivity.class);
                // clicked on own profile
                if (curComment.getNickname().equals(curUserNickname)) {
                    userProfileParcel.setCurrentActivity(ActivityEnum.PROFILE);
                    intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                }
                // clicked on another user's profile
                else {
                    UserProfileParcel parcel = new UserProfileParcel(ActivityEnum.PROFILE,
                            curUserNickname,
                            curComment);
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
                if (curComment.getFistbumpsCount() > 0) {
                    Intent intent = new Intent(callingContext, ViewFistbumpsActivity.class);
                    intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                    intent.putStringArrayListExtra("FISTBUMPED_USERS", new ArrayList<>(curComment.getFistbumpedUsers()));
                    callingContext.startActivity(intent);
                }
            }
        });

        // fistbump button onclick handler:
        commentHolder.fistbumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fistbumpsCount = Integer.parseInt(commentHolder.fistbumpsCount.getText().toString());
                Set<String> fistbumpedUsers = curComment.getFistbumpedUsers();
                // If user already liked the comment
                if (fistbumpedUsers.contains(curUserNickname)) {
                    fistbumpsCount--;
                    commentHolder.fistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
                    // remove user from fistbumpedUsers set
                    curComment.removeFistbumpedUser(curUserNickname);
                    dbHelper.saveDBObjectAsync(curComment);
                    // update fistbumps counts
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!curComment.getNickname().equals(curUserNickname)) {
                        // update the received fistbumps count of the main post user
                        dbHelper.decrementUserReceivedFistbumpsCount(curComment.getNickname());
                        // update the sent fistbumps count of the current user
                        dbHelper.decrementUserSentFistbumpsCount(curUserNickname);
                        // remove notification
                        dbHelper.deleteCommentFistbumpNotification(NotificationEnum.COMMENT_FISTBUMP, curComment.getCommentID(), curComment.getNickname());
                    }
                    // remove current user from fistbumped users
                    curComment.removeFistbumpedUser(userProfileParcel.getCurUserNickname());
                }
                // If user has not liked the comment yet
                else {
                    fistbumpsCount++;
                    commentHolder.fistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                    // add user to fistbumpedUsers set
                    curComment.addFistbumpedUser(curUserNickname);
                    // update fistbumps counts:
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!curComment.getNickname().equals(curUserNickname)) {
                        // update the received fistbumps count of the main post user
                        dbHelper.incrementUserReceivedFistbumpsCount(curComment.getNickname());
                        // update the sent fistbumps count of the current user
                        dbHelper.incrementUserSentFistbumpsCount(curUserNickname);
                        // send push notification
                        dbHelper.makeNewNotification(makeNotificationCommentFistbumpBundle(curComment));
                        httpRequestsHelper.makeHTTPPostRequest(makeHTTPPostRequestCommentFistbumpBundle(curComment));
                    }
                    // add current user to fistbumped users
                    curComment.addFistbumpedUser(userProfileParcel.getCurUserNickname());

                }
                // update comment fistbumps count
                curComment.setFistbumpsCount(fistbumpsCount);
                dbHelper.saveDBObjectAsync(curComment);
            }
        });

        // Delete comment event
        commentHolder.commentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curComment.getNickname().equals(curUserNickname))
                    launchDeleteCommentDialog(curComment, commentHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private Bundle makeNotificationCommentFistbumpBundle(DBUserComment curComment) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putInt("TYPE", 3);
        bundle.putString("RECEIVER_NICKNAME", curComment.getNickname());    // who the notification is going to
        bundle.putString("POST_ID", curComment.getPostID());
        bundle.putString("COMMENT_ID", curComment.getCommentID());
        return bundle;
    }

    private Bundle makeHTTPPostRequestCommentFistbumpBundle(DBUserComment curComment) {
        Bundle bundle = new Bundle();
        bundle.putInt("TYPE", 3);
        bundle.putString("RECEIVER_NICKNAME", curComment.getNickname());
        bundle.putString("SENDER_NICKNAME", userProfileParcel.getCurUserNickname());
        return bundle;
    }

    private void adapterAddItemBottom() {
        notifyItemInserted(getItemCount() - 1);
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
        DBUserComment newComment = new DBUserComment();
        Long timeInSeconds = Helpers.getTimestampMiliseconds();
        Long postTimeInSeconds = mainPost.getTimeInSeconds();
        newComment.setPostID(bundle.getString("POST_NICKNAME") + "_" + postTimeInSeconds.toString());
        newComment.setCommentID(bundle.getString("CUR_USER_NICKNAME") + "_" + timeInSeconds.toString());
        newComment.setNickname(bundle.getString("CUR_USER_NICKNAME"));
        newComment.setPostNickname(bundle.getString("POST_NICKNAME"));
        newComment.setFirstname(bundle.getString("FIRST_NAME"));
        newComment.setTimeInSeconds(timeInSeconds);
        newComment.setCognitoID(dbHelper.getIdentityID());
        newComment.setFacebookID(bundle.getString("FACEBOOK_ID"));
        newComment.setTimeStamp(Helpers.getTimestampString());
        newComment.setTextContent(commentText);
        newComment.setFistbumpedUsers(new HashSet<>(Collections.singletonList("_")));
        newComment.setFistbumpsCount(0);
        new PushNewUserCommentToDBTask().execute(newComment);
        dbHelper.incrementPostCommentsCount(mainPost);
        // send push notification (if not commenting on own post)
        if (!newComment.getNickname().equals(newComment.getPostNickname())) {
            dbHelper.makeNewNotification(makeNotificationPostCommentBundle(commentText, newComment.getCommentID()));
            httpRequestsHelper.makeHTTPPostRequest(makeHTTPPostRequestPostCommentBundle(commentText));
        }
    }

    private void launchDeleteCommentDialog(final DBUserComment curComment, final int position) {
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
                // delete the comment
                new DeleteUserCommentDBTask().execute(position);
                // decrement post comments count
                dbHelper.decrementPostCommentsCount(mainPost);
                // remove notification if comment is not by the same post user
                if (!curComment.getNickname().equals(mainPost.getNickname()))
                    dbHelper.deletePostCommentNotification(NotificationEnum.POST_COMMENT, curComment);
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

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private Bundle makeNotificationPostCommentBundle(String comment, String commentID) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putInt("TYPE", 1);
        bundle.putString("RECEIVER_NICKNAME", mainPost.getNickname());
        bundle.putString("POST_ID", mainPost.getPostID());
        bundle.putString("COMMENT_ID", commentID);
        bundle.putString("COMMENT", comment);
        return bundle;
    }

    private Bundle makeHTTPPostRequestPostCommentBundle(String comment) {
        Bundle bundle = new Bundle();
        bundle.putInt("TYPE", 1);
        bundle.putString("RECEIVER_NICKNAME", mainPost.getNickname());
        bundle.putString("SENDER_NICKNAME", userProfileParcel.getCurUserNickname());
        bundle.putString("COMMENT", comment);
        return bundle;
    }

    private void toggleDeletingCommentLoadingDialog(boolean show) {
        if (show)
            progressDialogDeleteComment = ProgressDialog.show(callingContext, "Delete Comment", "Deleting ... ", true);
        else
            progressDialogDeleteComment.dismiss();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class PushNewUserCommentToDBTask extends AsyncTask<DBUserComment, Void, Void> {
        @Override
        protected Void doInBackground(DBUserComment... params) {
            DBUserComment newComment = params[0];
            dbHelper.saveDBObject(newComment);
            comments.add(newComment);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            adapterAddItemBottom();
            ((NewCommentActivity) callingContext).postAddCommentCleanup();
        }
    }

    private class DeleteUserCommentDBTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            Integer position = params[0];
            DBUserComment comment = comments.get(position);
            dbHelper.deleteDBObject(comment);
            return position;
        }

        @Override
        protected void onPostExecute(Integer position) {
            adapterRemoveItemAt(position);
            ((NewCommentActivity) callingContext).postDeleteCommentCleanup();
            // dismiss comment delete loading dialog
            toggleDeletingCommentLoadingDialog(false);
        }
    }
}
