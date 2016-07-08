package com.peprally.jeremy.peprally.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
        RelativeLayout commentContainer;
        CardView cardView;
        ImageView profilePhoto;
        TextView timeStamp;
        TextView nickname;
        TextView postContent;
        TextView fistbumpButton;
        TextView fistbumpsCount;

        private CommentHolder(View itemView) {
            super(itemView);
            commentContainer = (RelativeLayout) itemView.findViewById(R.id.id_container_comment_clickable);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_new_comment);
            profilePhoto = (ImageView) itemView.findViewById(R.id.id_image_view_comment_profile);
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
                                        commentHolder.profilePhoto,
                                        curComment.getFacebookID(),
                                        3);

        Set<String> fistbumpedUsers = curComment.getFistbumpedUsers();

        if (fistbumpedUsers.contains(curUserNickname)) {
            commentHolder.fistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
        }

        commentHolder.nickname.setText(curComment.getNickname());
        commentHolder.postContent.setText(curComment.getTextContent());
        int fistbumpsCount = curComment.getFistbumpsCount();
        commentHolder.fistbumpsCount.setText(String.valueOf(fistbumpsCount));

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

        // fistbump button onClick handler:
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
                    }
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
                    }
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
        newComment.setPostNickname(bundle.getString("POST_NICKNAME"));
        newComment.setTimeInSeconds(timeInSeconds);
        newComment.setCognitoID(dbHelper.getIdentityID());
        newComment.setFacebookID(bundle.getString("FACEBOOK_ID"));
        newComment.setTimeStamp(df.format(c.getTime()));
        newComment.setTextContent(commentText);
        newComment.setFistbumpedUsers(new HashSet<>(Collections.singletonList("_")));
        newComment.setFistbumpsCount(0);
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
            if (comments.size() == 0) {
                Log.d("comments adapter", "ASOFIHALFHLASHFI");
                ((NewCommentActivity) callingContext).showNoCommentsText();
            }
            notifyItemRemoved(position);
        }
    }
}
