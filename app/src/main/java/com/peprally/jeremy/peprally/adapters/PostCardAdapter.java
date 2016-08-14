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
import com.peprally.jeremy.peprally.activities.ViewFistbumpsActivity;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.List;
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
        TextView username;
        TextView postContent;
        TextView postFistbumpButton;
        TextView postFistbumpsCount;
        TextView postCommentsCount;

        private PostHolder(View itemView) {
            super(itemView);
            postContainer = (RelativeLayout) itemView.findViewById(R.id.id_container_post_clickable);
            cardView = (CardView) itemView.findViewById(R.id.id_card_view_new_post);
            profileImage = (ImageView) itemView.findViewById(R.id.id_image_view_post_profile);
            username = (TextView) itemView.findViewById(R.id.id_text_view_post_username);
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
                                        curPost.getFacebookId(),
                                        3);

        final String curUsername = userProfileParcel.getCurUsername();

        Set<String> fistbumpedUsers = curPost.getFistbumpedUsers();

        if (fistbumpedUsers.contains(curUsername)) {
            postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
            postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
        }

        postHolder.username.setText(curPost.getUsername());
        postHolder.postContent.setText(curPost.getPostText());
        final int fistbumpsCount = curPost.getFistbumpsCount();
        postHolder.postFistbumpsCount.setText(String.valueOf(fistbumpsCount));
        postHolder.postCommentsCount.setText(String.valueOf(curPost.getCommentsCount()));

        postHolder.timeStamp.setText(Helpers.getTimetampString(curPost.getTimestampSeconds()));

        // profile picture onclick handlers:
        postHolder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userProfileParcel.getCurrentActivity() != ActivityEnum.PROFILE) {
                    Intent intent = new Intent(callingContext, ProfileActivity.class);
                    // clicked on own profile
                    if (curPost.getUsername().equals(curUsername)) {
                        userProfileParcel.setCurrentActivity(ActivityEnum.PROFILE);
                        userProfileParcel.setIsSelfProfile(true);
                        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                    }
                    // clicked on another user's profile
                    else {
                        UserProfileParcel parcel = new UserProfileParcel(ActivityEnum.PROFILE,
                                                                         curUsername,
                                                                         curPost);
                        intent.putExtra("USER_PROFILE_PARCEL", parcel);
                    }
                    callingContext.startActivity(intent);
                    ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
            }
        });

        // fistbump count button onclick handler:
        postHolder.postFistbumpsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (curPost.getFistbumpsCount() > 0) {
                    Intent intent = new Intent(callingContext, ViewFistbumpsActivity.class);
                    intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                    intent.putStringArrayListExtra("FISTBUMPED_USERS", new ArrayList<>(curPost.getFistbumpedUsers()));
                    callingContext.startActivity(intent);
                }
            }
        });

        // fistbump button onclick handler:
        postHolder.postFistbumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fistbumpsCount = Integer.parseInt(postHolder.postFistbumpsCount.getText().toString());
                Set<String> fistbumpedUsers = curPost.getFistbumpedUsers();
                // If user already liked the post
                if (fistbumpedUsers.contains(curUsername)) {
                    fistbumpsCount -= 1;
                    postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
                    postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_20, 0, 0, 0);
                    postHolder.postFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    // remove user from fistbumpedUsers set
                    curPost.removeFistbumpedUser(curUsername);
                    // update user fistbumps counts
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!curPost.getUsername().equals(userProfileParcel.getCurUsername())) {
                        // update the received fistbumps count of the main post user
                        dbHelper.decrementUserReceivedFistbumpsCount(curPost.getUsername());
                        // update the sent fistbumps count of the current user
                        dbHelper.decrementUserSentFistbumpsCount(userProfileParcel.getCurUsername());
                        // remove notification
                        dbHelper.deletePostFistbumpNotification(NotificationEnum.POST_FISTBUMP, curPost.getPostId(), userProfileParcel.getCurUsername());
                    }
                    // remove current user from fistbumped users
                    curPost.removeFistbumpedUser(userProfileParcel.getCurUsername());
                }
                // If user has not liked the post yet
                else {
                    fistbumpsCount += 1;
                    postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                    postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
                    postHolder.postFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    // add user to fistbumpedUsers set
                    curPost.addFistbumpedUser(curUsername);
                    // update user fistbumps counts
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!curPost.getUsername().equals(userProfileParcel.getCurUsername())) {
                        // update the received fistbumps count of the main post user
                        dbHelper.incrementUserReceivedFistbumpsCount(curPost.getUsername());
                        // update the sent fistbumps count of the current user
                        dbHelper.incrementUserSentFistbumpsCount(userProfileParcel.getCurUsername());
                        // make new notification
                        dbHelper.createNewNotification(makeNotificationPostFistbumpBundle(curPost));
                        // send push notification
                        httpRequestsHelper.makePushNotificationRequest(makeHTTPPostRequestPostFistbumpBundle(curPost));
                    }
                    // add current user to fistbumped users
                    curPost.addFistbumpedUser(userProfileParcel.getCurUsername());
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
        Intent intent = new Intent(callingContext, NewCommentActivity.class);
        userProfileParcel.setCurrentActivity(ActivityEnum.NEWCOMMENT);
        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
        intent.putExtra("MAIN_POST", curPost);
        callingContext.startActivity(intent);
        ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
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

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    public void addPost(String newPostText, Bundle bundle) {
        Long timestampSeconds = Helpers.getTimestampSeconds();
        DBUserPost newPost = new DBUserPost(
                bundle.getString("USERNAME"),
                bundle.getString("USERNAME") + "_" + timestampSeconds.toString(),
                dbHelper.getIdentityID(),
                bundle.getString("FACEBOOK_ID"),
                bundle.getString("FIRST_NAME"),
                newPostText,
                timestampSeconds
        );
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
            DBUserProfile userProfile = dbHelper.loadDBUserProfile(newPost.getUsername());
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
