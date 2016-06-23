package com.peprally.jeremy.peprally.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.NewCommentActivity;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommentCardAdapter extends RecyclerView.Adapter<CommentCardAdapter.CommentHolder> {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private DynamoDBHelper dbHelper;

    // General Variables
    private Context callingContext;
    private List<DBUserComment> comments;
    final private String curUserNickname;

    /***********************************************************************************************
     ********************************** ADAPTER CONSTRUCTOR/METHODS ********************************
     **********************************************************************************************/
    public CommentCardAdapter(Context callingContext,
                              List<DBUserComment> comments,
                              String curUserNickname) {
        this.comments = comments;
        this.callingContext = callingContext;
        this.curUserNickname = curUserNickname;
        dbHelper = new DynamoDBHelper(callingContext);
    }

    static class CommentHolder extends RecyclerView.ViewHolder {
        LinearLayout commentContainer;
        CardView cardView;
        ImageView profilePhoto;
        TextView nickname;
        TextView timeStamp;
        TextView postContent;
        ImageButton thumbsUp;
        ImageButton thumbsDown;
        TextView likesCount;

        private CommentHolder(View itemView) {
            super(itemView);
            commentContainer = (LinearLayout) itemView.findViewById(R.id.id_container_comment_clickable);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_new_comment);
            profilePhoto = (ImageView) itemView.findViewById(R.id.id_image_view_comment_profile);
            nickname = (TextView) itemView.findViewById(R.id.id_text_view_comment_nickname);
            timeStamp = (TextView) itemView.findViewById(R.id.id_text_view_comment_time_stamp);
            postContent = (TextView) itemView.findViewById(R.id.id_text_view_comment_content);
            thumbsUp = (ImageButton) itemView.findViewById(R.id.id_image_button_comment_thumbs_up);
            thumbsDown = (ImageButton) itemView.findViewById(R.id.id_image_button_comment_thumbs_down);
            likesCount = (TextView) itemView.findViewById(R.id.id_text_view_comment_likes);
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
                                        commentHolder.profilePhoto,
                                        curComment.getFacebookID(),
                                        4);

        Set<String> likedUsers = curComment.getLikedUsers();
        Set<String> dislikedUsers = curComment.getDislikedUsers();

        if (likedUsers.contains(curUserNickname)) {
            commentHolder.thumbsUp.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.ic_thumb_uped));
        }
        else if (dislikedUsers.contains(curUserNickname)) {
            commentHolder.thumbsDown.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.ic_thumb_downed));
        }

        commentHolder.nickname.setText(curComment.getNickname());
        commentHolder.postContent.setText(curComment.getTextContent());
        int numOfLikes = curComment.getNumberOfLikes();
        if (numOfLikes > 0) {
            commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorGreen));
        }
        else if (numOfLikes == 0) {
            commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorAccent));
        }
        else {
            commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorRed));
        }
        commentHolder.likesCount.setText(String.valueOf(numOfLikes));

        long tsLong = System.currentTimeMillis()/1000;
        long timeInSeconds = tsLong - curComment.getTimeInSeconds();
        if (timeInSeconds < 60) {
            String s = String.valueOf(timeInSeconds) + "s";
            commentHolder.timeStamp.setText(s);
        }
        else if (timeInSeconds < 60 * 60) {
            long timeInMins = timeInSeconds / 60;
            String s = String.valueOf(timeInMins) + "m";
            commentHolder.timeStamp.setText(s);
        }
        else if (timeInSeconds < 60 * 60 * 24) {
            long timeInHrs = timeInSeconds/60/60;
            String s = String.valueOf(timeInHrs) + "h";
            commentHolder.timeStamp.setText(s);
        }
        else {
            long timeInDays = timeInSeconds/60/60/24;
            String s = String.valueOf(timeInDays) + "d";
            commentHolder.timeStamp.setText(s);
        }

        // ImageButton OnClick handlers:
        commentHolder.thumbsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNumOfLikes = Integer.parseInt(commentHolder.likesCount.getText().toString());
                Set<String> likedUsers = curComment.getLikedUsers();
                Set<String> dislikedUsers = curComment.getDislikedUsers();
                // If user already liked the comment
                if (likedUsers.contains(curUserNickname)) {
                    currentNumOfLikes--;
                    commentHolder.likesCount.setText(String.valueOf(currentNumOfLikes));
                    commentHolder.thumbsUp.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.ic_thumb_up));
                    curComment.setNumberOfLikes(currentNumOfLikes);
                    // Special UI transition case
                    if (currentNumOfLikes == 0) {
                        commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorAccent));
                    } else if (currentNumOfLikes == -1) {
                        commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorRed));
                    }
                    // Remove user from likedUsers set
                    curComment.removeLikedUsers(curUserNickname);
                    new AsyncHelpers.PushUserCommentChangesToDBTask().execute(
                            new AsyncHelpers.asyncTaskObjectUserCommentBundle(curComment, dbHelper.getMapper(), null));
                }
                // If user has not liked the comment yet
                else {
                    // lose previous dislike and +1 like
                    if (dislikedUsers.contains(curUserNickname)) {
                        currentNumOfLikes += 2;
                    }
                    else {
                        currentNumOfLikes++;
                    }
                    commentHolder.likesCount.setText(String.valueOf(currentNumOfLikes));
                    commentHolder.thumbsUp.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.ic_thumb_uped));
                    commentHolder.thumbsDown.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.ic_thumb_down));
                    curComment.setNumberOfLikes(currentNumOfLikes);
                    // Special UI transition case
                    if (currentNumOfLikes == 0) {
                        commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorAccent));
                    } else if (currentNumOfLikes == 1) {
                        commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorGreen));
                    }
                    // Remove user from dislikedUsers set and add to likedUsers set
                    curComment.addLikedUsers(curUserNickname);
                    curComment.removedislikedUsers(curUserNickname);
                    new AsyncHelpers.PushUserCommentChangesToDBTask().execute(
                            new AsyncHelpers.asyncTaskObjectUserCommentBundle(curComment, dbHelper.getMapper(), null));
                }
            }
        });
        
        commentHolder.thumbsDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNumOfLikes = Integer.parseInt(commentHolder.likesCount.getText().toString());
                Set<String> likedUsers = curComment.getLikedUsers();
                Set<String> dislikedUsers = curComment.getDislikedUsers();
                // If user already disliked the post
                if (dislikedUsers.contains(curUserNickname)) {
                    currentNumOfLikes++;
                    commentHolder.likesCount.setText(String.valueOf(currentNumOfLikes));
                    commentHolder.thumbsDown.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.ic_thumb_down));
                    curComment.setNumberOfLikes(currentNumOfLikes);
                    // Special UI transition case
                    if (currentNumOfLikes == 0) {
                        commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorAccent));
                    } else if (currentNumOfLikes == 1) {
                        commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorGreen));
                    }
                    // Remove user from likedUsers set
                    curComment.removedislikedUsers(curUserNickname);
                    new AsyncHelpers.PushUserCommentChangesToDBTask().execute(
                            new AsyncHelpers.asyncTaskObjectUserCommentBundle(curComment, dbHelper.getMapper(), null));
                }
                // If user has not disliked the post yet
                else {
                    if (likedUsers.contains(curUserNickname)) {
                        // lose previous like and +1 dislike
                        currentNumOfLikes -= 2;
                    }
                    else {
                        currentNumOfLikes--;
                    }
                    commentHolder.likesCount.setText(String.valueOf(currentNumOfLikes));
                    commentHolder.thumbsUp.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.ic_thumb_up));
                    commentHolder.thumbsDown.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.ic_thumb_downed));
                    curComment.setNumberOfLikes(currentNumOfLikes);
                    // Special UI transition case
                    if (currentNumOfLikes == 0) {
                        commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorAccent));
                    } else if (currentNumOfLikes == -1) {
                        commentHolder.likesCount.setTextColor(ContextCompat.getColor(callingContext, R.color.colorRed));
                    }
                    // Remove user from dislikedUsers set and add to likedUsers set
                    curComment.adddislikedUsers(curUserNickname);
                    curComment.removeLikedUsers(curUserNickname);
                    new AsyncHelpers.PushUserCommentChangesToDBTask().execute(
                            new AsyncHelpers.asyncTaskObjectUserCommentBundle(curComment, dbHelper.getMapper(), null));
                }
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
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    public void addComment(String commentText, Bundle bundle) {
        DBUserComment newComment = new DBUserComment();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Long timeInSeconds = System.currentTimeMillis() / 1000;
        Long postTimeInSeconds = ((NewCommentActivity) callingContext).getPostCommentBundleLong("TIME_IN_SECONDS");
        newComment.setPostID(bundle.getString("POST_NICKNAME") + "_" + postTimeInSeconds.toString());
        newComment.setNickname(bundle.getString("CUR_USER_NICKNAME"));
        newComment.setTimeInSeconds(timeInSeconds);
        newComment.setCognitoID(dbHelper.getIdentityID());
        newComment.setFacebookID(bundle.getString("FACEBOOK_ID"));
        newComment.setTimeStamp(df.format(c.getTime()));
        newComment.setTextContent(commentText);
        newComment.setLikedUsers(new HashSet<>(Collections.singletonList("_")));
        newComment.setDislikedUsers(new HashSet<>(Collections.singletonList("_")));
        newComment.setNumberOfLikes(0);
        new PushNewUserCommentToDBTask().execute(newComment);
        Bundle asyncData = new Bundle();
        asyncData.putLong("POST_TIME_IN_SECONDS", postTimeInSeconds);
        asyncData.putBoolean("INCREMENT_COMMENTS_COUNT", true);
        new AsyncHelpers.PushPostCommentsCountToDBTask().execute(
                new AsyncHelpers.asyncTaskObjectUserCommentBundle(
                        newComment,
                        dbHelper.getMapper(),
                        asyncData));
        ((NewCommentActivity) callingContext).postAddCommentCleanup();
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
                new DeleteUserCommentDBTask().execute(position);
                Bundle asyncData = new Bundle();
                asyncData.putLong("POST_TIME_IN_SECONDS", ((NewCommentActivity) callingContext).getPostCommentBundleLong("TIME_IN_SECONDS"));
                asyncData.putBoolean("INCREMENT_COMMENTS_COUNT", false);
                new AsyncHelpers.PushPostCommentsCountToDBTask().execute(
                        new AsyncHelpers.asyncTaskObjectUserCommentBundle(
                                curComment,
                                dbHelper.getMapper(),
                                asyncData));
                ((NewCommentActivity) callingContext).refreshAdapter();
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
            notifyItemInserted(0);
        }
    }

    private class DeleteUserCommentDBTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            Integer position = params[0];
            DBUserComment comment = comments.get(position);
            dbHelper.deleteDBObject(comment);
            comments.remove(position);
            return position;
        }

        @Override
        protected void onPostExecute(Integer position) {
            notifyItemRemoved(position);
        }
    }
}
