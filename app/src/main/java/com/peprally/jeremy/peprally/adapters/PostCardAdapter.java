package com.peprally.jeremy.peprally.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.interfaces.PostContainerInterface;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.network.HTTPRequestsHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.List;
import java.util.Set;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.PostHolder> {
    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS/HTTP Variables
    private DynamoDBHelper dynamoDBHelper;
    private HTTPRequestsHelper httpRequestsHelper;

    // General Variables
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
        dynamoDBHelper = new DynamoDBHelper(callingContext);
        httpRequestsHelper = new HTTPRequestsHelper(callingContext);
    }

    static class PostHolder extends RecyclerView.ViewHolder {
        RelativeLayout postContainer;
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
                                        Helpers.FacebookProfilePictureEnum.LARGE,
                                        true);

        final String curUsername = userProfileParcel.getCurrentUsername();

        final Set<String> fistbumpedUsers = curPost.getFistbumpedUsers();

        if (fistbumpedUsers != null && fistbumpedUsers.contains(curUsername)) {
            postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
            postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
        } else {
            postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
            postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_20, 0, 0, 0);
        }

        postHolder.username.setText(curPost.getUsername());
        postHolder.postContent.setText(curPost.getPostText());
        final int fistbumpsCount = curPost.getFistbumpsCount();
        postHolder.postFistbumpsCount.setText(String.valueOf(fistbumpsCount));
        postHolder.postCommentsCount.setText(String.valueOf(curPost.getCommentsCount()));
        postHolder.postCommentsCount.setCompoundDrawablesWithIntrinsicBounds(
                Helpers.getAPICompatVectorDrawable(callingContext, R.drawable.ic_replies), null, null, null);

        postHolder.timeStamp.setText(Helpers.getTimetampString(curPost.getTimestampSeconds(), true));

        // profile picture onclick handlers:
        postHolder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userProfileParcel.getCurrentActivity() != ActivityEnum.PROFILE) {
                    Intent intent = new Intent(callingContext, ProfileActivity.class);
                    // clicked on own profile
                    if (curPost.getUsername().equals(curUsername)) {
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
                    dynamoDBHelper.doActionIfDBUserPostAndCommentExists(
                            curPost,
                            null,       // don't need to check comment
                            new DynamoDBHelper.AsyncTaskCallbackWithReturnObject() {
                                @Override
                                public void onTaskDone(Object bundle) {
                                    if (((Bundle) bundle).getBoolean("POST_AND_COMMENT_EXISTS", false)) {   // assume worst case, that post/comment was deleted
                                        Intent intent = new Intent(callingContext, ViewFistbumpsActivity.class);
                                        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
                                        intent.putExtra("USER_POST", curPost);
                                        callingContext.startActivity(intent);
                                    } else {
                                        launchPostOrCommentIsDeletedDialog(((Bundle) bundle).getBoolean("IS_POST_DELETED", true)); // assume post was deleted
                                    }

                                }
                            });
                }
            }
        });

        // fistbump button onclick handler:
        postHolder.postFistbumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Set<String> fistbumpedUsers = curPost.getFistbumpedUsers();
                // If user already liked the post
                if (fistbumpedUsers != null && fistbumpedUsers.contains(curUsername)) {
                    final int fistbumpsCount = Integer.parseInt(postHolder.postFistbumpsCount.getText().toString()) - 1;
                    // remove user from fistbumpedUsers set
                    curPost.removeFistbumpedUser(curUsername);
                    curPost.setFistbumpsCount(fistbumpsCount);
                    // update UI
                    postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_50, 0);
                    postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_20, 0, 0, 0);
                    postHolder.postFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!curPost.getUsername().equals(userProfileParcel.getCurrentUsername())) {
                        // remove notification and also update fistbump counts respectively
                        dynamoDBHelper.deletePostFistbumpNotification(
                                curPost.getPostId(),
                                userProfileParcel.getCurrentUsername(),
                                curPost.getUsername(),
                                new DynamoDBHelper.AsyncTaskCallbackWithReturnObject() {
                                    @Override
                                    public void onTaskDone(Object bundle) {
                                        if (((Bundle) bundle).getBoolean("TASK_SUCCESS", false)) { // assume that notification was not deleted
                                            dynamoDBHelper.saveDBObjectAsync(curPost);
                                        } else {
                                            launchPostOrCommentIsDeletedDialog(true);   // post was deleted
                                        }
                                    }
                                });
                    } else {
                        // if I made changes to my own post, just save the post right away, I don't
                        // need to check if the post has been deleted
                        dynamoDBHelper.saveDBObjectAsync(curPost);
                    }
                }
                // If user has not liked the post yet
                else {
                    final int fistbumpsCount = Integer.parseInt(postHolder.postFistbumpsCount.getText().toString()) + 1;
                    // add user to fistbumpedUsers set
                    curPost.addFistbumpedUser(curUsername);
                    curPost.setFistbumpsCount(fistbumpsCount);
                    // update UI
                    postHolder.postFistbumpButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fistbump_filled_50, 0);
                    postHolder.postFistbumpsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fistbump_filled_20, 0, 0, 0);
                    postHolder.postFistbumpsCount.setText(String.valueOf(fistbumpsCount));
                    // if current user did not fistbump his/her OWN post (fistbumping your own post does not change user's own fistbumps count)
                    if (!curPost.getUsername().equals(userProfileParcel.getCurrentUsername())) {
                        // make new notification
                        dynamoDBHelper.createNewNotification(
                                makeDBNotificationBundlePostFistbump(curPost),
                                new DynamoDBHelper.AsyncTaskCallbackWithReturnObject(){
                                    @Override
                                    public void onTaskDone(Object bundle) {
                                        if (((Bundle) bundle).getBoolean("TASK_SUCCESS", false)) { // assume that notification was not created
                                            // send push notification
                                            httpRequestsHelper.makePushNotificationRequest(makePushNotificationBundlePostFistbump(curPost));
                                            dynamoDBHelper.saveDBObjectAsync(curPost);
                                        } else {
                                            launchPostOrCommentIsDeletedDialog(true);   // post was deleted
                                        }
                                    }
                                });
                    } else {
                        // if I made changes to my own post, just save the post right away, I don't
                        // need to check if the post has been deleted
                        dynamoDBHelper.saveDBObjectAsync(curPost);
                    }
                }
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
     **************************************** GENERAL_METHODS **************************************
     **********************************************************************************************/
    private void adapterAddItemTop(DBUserPost newPost) {
        posts.add(0, newPost);
        notifyItemInserted(0);
    }

    private void launchNewCommentActivity(DBUserPost curPost) {
        Intent intent = new Intent(callingContext, PostCommentActivity.class);
        intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
        intent.putExtra("MAIN_POST", curPost);
        callingContext.startActivity(intent);
        ((AppCompatActivity) callingContext).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    private Bundle makeDBNotificationBundlePostFistbump(DBUserPost curPost) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_FISTBUMP.toInt());
        bundle.putParcelable("USER_PROFILE_PARCEL", userProfileParcel);
        bundle.putString("RECEIVER_USERNAME", curPost.getUsername());
        bundle.putString("POST_ID", curPost.getPostId());
        return bundle;
    }

    private Bundle makePushNotificationBundlePostFistbump(DBUserPost curPost) {
        Bundle bundle = new Bundle();
        bundle.putInt("NOTIFICATION_TYPE", NotificationEnum.POST_FISTBUMP.toInt());
        bundle.putString("RECEIVER_USERNAME", curPost.getUsername());
        bundle.putString("SENDER_USERNAME", userProfileParcel.getCurrentUsername());
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
                dynamoDBHelper.getIdentityID(),
                bundle.getString("FACEBOOK_ID"),
                bundle.getString("FIRST_NAME"),
                newPostText,
                timestampSeconds
        );
        new PushNewUserPostToDBTask().execute(newPost);
    }

    private void launchPostOrCommentIsDeletedDialog(boolean isPost) {
        final AlertDialog.Builder dialogBuilderPostDeleted = new AlertDialog.Builder(callingContext);
        final View dialogViewConfirmDelete = View.inflate(callingContext, R.layout.dialog_confirm_delete, null);
        dialogBuilderPostDeleted.setView(dialogViewConfirmDelete);
        dialogBuilderPostDeleted.setTitle("Oops!");
        if (isPost)
            dialogBuilderPostDeleted.setMessage("Looks like this post has been deleted!");
        else
            dialogBuilderPostDeleted.setMessage("Looks like this comment has been deleted!");
        dialogBuilderPostDeleted.setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ((PostContainerInterface) callingContext).refreshPosts();
            }
        });

        AlertDialog b = dialogBuilderPostDeleted.create();
        b.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ((PostContainerInterface) callingContext).refreshPosts();
            }
        });

        b.show();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class PushNewUserPostToDBTask extends AsyncTask<DBUserPost, Void, DBUserPost> {
        @Override
        protected DBUserPost doInBackground(DBUserPost... params) {
            // Save new post to DBUserPosts
            DBUserPost newPost= params[0];
            dynamoDBHelper.saveDBObject(newPost);

            // Update UserProfile post count
            DBUserProfile userProfile = dynamoDBHelper.loadDBUserProfile(newPost.getUsername());
            int curPostCount = userProfile.getPostsCount();
            userProfile.setPostsCount(curPostCount + 1);
            dynamoDBHelper.saveDBObject(userProfile);

            return newPost;
        }

        @Override
        protected void onPostExecute(DBUserPost newPost) {
            adapterAddItemTop(newPost);
        }
    }
}
