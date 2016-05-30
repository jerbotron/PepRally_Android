package com.peprally.jeremy.peprally.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.NewCommentActivity;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommentCardAdapter extends RecyclerView.Adapter<CommentCardAdapter.CommentHolder> {

    private Context callingContext;

    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    private List<DBUserComment> comments;

    private static final String TAG = "CommentCardAdapter: ";

    public static class CommentHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView profilePhoto;
        TextView nickname;
        TextView timeStamp;
        TextView postContent;
        ImageButton thumbsUp;
        ImageButton thumbsDown;
        TextView postLikesCount;

        public CommentHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_new_comment);
            profilePhoto = (ImageView) itemView.findViewById(R.id.id_image_view_comment_profile);
            nickname = (TextView) itemView.findViewById(R.id.id_text_view_comment_nickname);
            timeStamp = (TextView) itemView.findViewById(R.id.id_text_view_comment_time_stamp);
            postContent = (TextView) itemView.findViewById(R.id.id_text_view_comment_content);
            thumbsUp = (ImageButton) itemView.findViewById(R.id.id_image_button_comment_thumbs_up);
            thumbsDown = (ImageButton) itemView.findViewById(R.id.id_image_button_comment_thumbs_down);
            postLikesCount = (TextView) itemView.findViewById(R.id.id_text_view_comment_likes);
        }
    }

    public CommentCardAdapter(Context callingContext, List<DBUserComment> comments) {
        this.comments = comments;
        this.callingContext = callingContext;
        credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,                             // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_new_comment_container, parent, false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentHolder commentHolder, int position) {
        final DBUserComment curComment = comments.get(position);
        new AsyncHelpers.LoadFBProfilePictureTask().execute(new AsyncHelpers.asyncTaskObjectProfileImage(curComment.getFacebookID(), commentHolder.profilePhoto));

        final String userNickName = ((NewCommentActivity) callingContext).getPostCommentBundleString("NICKNAME");
        Set<String> likedUsers = curComment.getLikedUsers();
        Set<String> dislikedUsers = curComment.getDislikedUsers();

        if (likedUsers.contains(userNickName)) {
            commentHolder.thumbsUp.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_uped));
        }
        else if (dislikedUsers.contains(userNickName)) {
            commentHolder.thumbsDown.setImageDrawable(callingContext.getResources().getDrawable(R.drawable.ic_thumb_downed));
        }

        commentHolder.nickname.setText(curComment.getNickname());
        commentHolder.postContent.setText(curComment.getTextContent());
        int numOfLikes = curComment.getNumberOfLikes();
        if (numOfLikes > 0) {
            commentHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorGreen)));
        }
        else if (numOfLikes == 0) {
            commentHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorAccent)));
        }
        else {
            commentHolder.postLikesCount.setTextColor(ColorStateList.valueOf(callingContext.getResources().getColor(R.color.colorRed)));
        }
        commentHolder.postLikesCount.setText(String.valueOf(numOfLikes));

        long tsLong = System.currentTimeMillis()/1000;
        long timeInSeconds = tsLong - curComment.getTimeInSeconds();
        if (timeInSeconds < 60) {
            commentHolder.timeStamp.setText(String.valueOf(timeInSeconds) + "s");
        }
        else if (timeInSeconds < 60 * 60) {
            long timeInMins = timeInSeconds / 60;
            commentHolder.timeStamp.setText(String.valueOf(timeInMins) + "m");
        }
        else if (timeInSeconds < 60 * 60 * 24) {
            long timeInHrs = timeInSeconds/60/60;
            commentHolder.timeStamp.setText(String.valueOf(timeInHrs) + "h");
        }
        else {
            long timeInDays = timeInSeconds/60/60/24;
            commentHolder.timeStamp.setText(String.valueOf(timeInDays) + "d");
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void addComment(String commentText, Bundle bundle) {
        DBUserComment newComment = new DBUserComment();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Long timeInSeconds = System.currentTimeMillis() / 1000;
        Long postTimeInSeconds = ((NewCommentActivity) callingContext).getPostCommentBundleLong("TIME_IN_SECONDS");
        newComment.setPostID(bundle.getString("NICKNAME") + "_" + postTimeInSeconds.toString());
        newComment.setNickname(bundle.getString("NICKNAME"));
        newComment.setTimeInSeconds(timeInSeconds);
        newComment.setCognitoID(credentialsProvider.getIdentityId());
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
                        mapper,
                        asyncData));
        ((NewCommentActivity) callingContext).postAddCommentCleanup();
    }

    /********************************** AsyncTasks **********************************/
    private class PushNewUserCommentToDBTask extends AsyncTask<DBUserComment, Void, DBUserComment> {
        @Override
        protected DBUserComment doInBackground(DBUserComment... params) {
            DBUserComment newComment = params[0];
            mapper.save(newComment);
            return newComment;
        }

        @Override
        protected void onPostExecute(DBUserComment newComment) {
            comments.add(newComment);
            notifyItemInserted(0);
        }
    }
}
