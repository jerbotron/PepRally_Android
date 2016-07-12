package com.peprally.jeremy.peprally.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.ActivityEnum;
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.PostHolder> {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS/HTTP Variables
    private DynamoDBHelper dbHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // General Variables
//    private static final String TAG = "PostCardAdapter: ";
    private Context callingContext;
    private List<DBUserPost> posts;
    private UserProfileParcel userProfileParcel;

    /***********************************************************************************************
     ********************************** ADAPTER CONSTRUCTOR/METHODS ********************************
     **********************************************************************************************/
    public PostCardAdapter(Context callingContext, List<DBUserPost> posts, UserProfileParcel userProfileParcel) {
        this.callingContext = callingContext;
        this.posts = posts;
        this.userProfileParcel = userProfileParcel;
        dbHelper = new DynamoDBHelper(callingContext);
        httpRequestsHelper = new HTTPRequestsHelper(callingContext);
    }

    static class PostHolder extends RecyclerView.ViewHolder {
        RelativeLayout postContainer;
        CardView cardView;
        ImageView profileImage;
        TextView timeStamp;
        TextView nickname;
        TextView postContent;
        TextView postFistbumpButton;
        TextView postFistbumpsCount;
        TextView postCommentsCount;

        private PostHolder(View itemView) {
            super(itemView);
            postContainer = (RelativeLayout) itemView.findViewById(R.id.id_container_post_clickable);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_new_post);
            profileImage = (ImageView) itemView.findViewById(R.id.id_image_view_post_profile);
            nickname = (TextView) itemView.findViewById(R.id.id_text_view_post_nickname);
            timeStamp = (TextView) itemView.findViewById(R.id.id_text_view_post_card_time_stamp);
            postContent = (TextView) itemView.findViewById(R.id.id_text_view_post_content);
            postFistbumpButton = (TextView) itemView.findViewById(R.id.id_button_post_card_fistbump);
            postFistbumpsCount = (TextView) itemView.findViewById(R.id.id_text_view_post_card_fistbumps_count);
            postCommentsCount = (TextView) itemView.findViewById(R.id.id_text_view_post_card_comments_count);
        }
    }

    @Override
    public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_new_post_container, parent, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(final PostHolder postHolder, int position) {
        final DBUserPost curPost = posts.get(position);
        Helpers.setFacebookProfileImage(callingContext,
                                        postHolder.profileImage,
                                        curPost.getFacebookID(),
                                        3);

//        final String profileNickname = userProfileParcel.getProfileNickname();
        final String curUserNickname = userProfileParcel.getCurUserNickname();

        Set<String> fistbumpedUsers = curPost.getFistbumpedUsers();

        if (fistbumpedUsers.contains(curUserNickname)) {
            postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
            postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
        }

        postHolder.nickname.setText(curPost.getNickname());
        postHolder.postContent.setText(curPost.getTextContent());
        final int fistbumpsCount = curPost.getFistbumpsCount();
        postHolder.postFistbumpsCount.setText(String.valueOf(fistbumpsCount));
        postHolder.postCommentsCount.setText(String.valueOf(curPost.getNumberOfComments()));

        postHolder.timeStamp.setText(Helpers.getTimeStampString(curPost.getTimeInSeconds()));

        // Profile Picture OnClick Handlers:
        postHolder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userProfileParcel.getCurrentActivity() != ActivityEnum.PROFILE) {
                    Intent intent = new Intent(callingContext, ProfileActivity.class);
                    // Clicked on own profile
                    if (curPost.getNickname().equals(curUserNickname)) {
                        userProfileParcel.setCurrentActivity(ActivityEnum.PROFILE);
                        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                    }
                    // Clicked on another user's profile
                    else {
                        UserProfileParcel parcel = new UserProfileParcel(ActivityEnum.PROFILE,
                                                                         curUserNickname,
                                                                         curPost);
                        intent.putExtra("USER_PROFILE_PARCEL", parcel);
                    }
                    callingContext.startActivity(intent);
                    ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
            }
        });

        // fistbump button onClick handler:
        postHolder.postFistbumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fistbumpsCount = Integer.parseInt(postHolder.postFistbumpsCount.getText().toString());
                Set<String> fistbumpedUsers = curPost.getFistbumpedUsers();
                // If user already liked the post
                if (fistbumpedUsers.contains(curUserNickname)) {
                    fistbumpsCount -= 1;
                    postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
                    postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_20, 0, 0, 0);
                    postHolder.postFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    // remove user from fistbumpedUsers set
                    curPost.removeFistbumpedUser(curUserNickname);
                    // update user fistbumps counts
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!curPost.getNickname().equals(userProfileParcel.getCurUserNickname())) {
                        // update the received fistbumps count of the main post user
                        dbHelper.decrementUserReceivedFistbumpsCount(curPost.getNickname());
                        // update the sent fistbumps count of the current user
                        dbHelper.decrementUserSentFistbumpsCount(userProfileParcel.getCurUserNickname());
                    }
                }
                // If user has not liked the post yet
                else {
                    fistbumpsCount += 1;
                    postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                    postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
                    postHolder.postFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    // add user to fistbumpedUsers set
                    curPost.addFistbumpedUser(curUserNickname);
                    // update user fistbumps counts
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!curPost.getNickname().equals(userProfileParcel.getCurUserNickname())) {
                        // update the received fistbumps count of the main post user
                        dbHelper.incrementUserReceivedFistbumpsCount(curPost.getNickname());
                        // update the sent fistbumps count of the current user
                        dbHelper.incrementUserSentFistbumpsCount(userProfileParcel.getCurUserNickname());
                        // send push notification
                        dbHelper.sendNewNotification(makeNotificationPostFistbumpBundle(curPost));
                        httpRequestsHelper.makeHTTPPostRequest(makeHTTPPostRequestPostFistbumpBundle(curPost));
                    }
                }
                // update post fistbumps count
                curPost.setFistbumpsCount(fistbumpsCount);
                dbHelper.saveDBObjectAsync(curPost);
            }
        });

        // New Comment Events
        postHolder.postContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchNewCommentActivity(curPost);
            }
        });

        postHolder.postCommentsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchNewCommentActivity(curPost);
            }
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private void adapterAddItemTop(DBUserPost newPost) {
        posts.add(0, newPost);
        notifyItemInserted(0);
    }
    private void launchNewCommentActivity(DBUserPost curPost) {
        Bundle postCommentBundle = new Bundle();
        postCommentBundle.putString("POST_ID", curPost.getPostID());
        postCommentBundle.putString("TEXT_CONTENT", curPost.getTextContent());
        postCommentBundle.putString("POST_NICKNAME", curPost.getNickname());
        postCommentBundle.putLong("TIME_IN_SECONDS", curPost.getTimeInSeconds());
        postCommentBundle.putString("FACEBOOK_ID", curPost.getFacebookID());
        postCommentBundle.putInt("FISTBUMPS_COUNT", curPost.getFistbumpsCount());
        postCommentBundle.putInt("COMMENTS_COUNT", curPost.getNumberOfComments());
        Intent intent = new Intent(callingContext, NewCommentActivity.class);
        userProfileParcel.setCurrentActivity(ActivityEnum.NEWCOMMENT);
        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
        intent.putExtra("POST_COMMENT_BUNDLE", postCommentBundle);
        callingContext.startActivity(intent);
        ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    private Bundle makeNotificationPostFistbumpBundle(DBUserPost curPost) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putInt("TYPE", 2);
        bundle.putString("NICKNAME", curPost.getNickname());    // who the notification is going to
        bundle.putString("POST_ID", curPost.getPostID());
        return bundle;
    }

    private Bundle makeHTTPPostRequestPostFistbumpBundle(DBUserPost curPost) {
        Bundle bundle = new Bundle();
        bundle.putInt("TYPE", 2);
        bundle.putString("RECEIVER_NICKNAME", curPost.getNickname());
        bundle.putString("SENDER_NICKNAME", userProfileParcel.getCurUserNickname());
        return bundle;
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    public void addPost(String newPostText, Bundle bundle) {
        DBUserPost newPost = new DBUserPost();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        newPost.setNickname(bundle.getString("NICKNAME"));
        Long timeInSeconds = System.currentTimeMillis() / 1000;
        newPost.setTimeInSeconds(timeInSeconds);
        newPost.setPostID(bundle.getString("NICKNAME") + "_" + timeInSeconds.toString());
        newPost.setCognitoID(dbHelper.getIdentityID());
        newPost.setFacebookID(bundle.getString("FACEBOOK_ID"));
        newPost.setFirstname(bundle.getString("FIRST_NAME"));
        newPost.setTimeStamp(df.format(c.getTime()));
        newPost.setTextContent(newPostText);
        newPost.setFistbumpedUsers(new HashSet<>(Collections.singletonList("_")));
        newPost.setFistbumpsCount(0);
        newPost.setNumberOfComments(0);
        new PushNewUserPostToDBTask().execute(newPost);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class PushNewUserPostToDBTask extends AsyncTask<DBUserPost, Void, DBUserPost> {
        @Override
        protected DBUserPost doInBackground(DBUserPost... params) {
            // Save new post to DBUserPosts
            DBUserPost newPost= params[0];
            dbHelper.saveDBObject(newPost);

            // Update UserProfile post count
            DBUserProfile userProfile = dbHelper.loadDBUserProfile(newPost.getNickname());
            int curPostCount = userProfile.getPostsCount();
            userProfile.setPostsCount(curPostCount + 1);
            dbHelper.saveDBObject(userProfile);

            return newPost;
        }

        @Override
        protected void onPostExecute(DBUserPost newPost) {
            adapterAddItemTop(newPost);
        }
    }
}
