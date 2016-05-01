package com.peprally.jeremy.peprally.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserNickname;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class NewPostCardAdapter extends RecyclerView.Adapter<NewPostCardAdapter.NewPostHolder> {

    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient ddbClient;

    private List<DBUserPost> posts;

    private static final String TAG = "NewPostCardAdapter: ";

    public static class NewPostHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView profilePhoto;
        TextView nickname;
        TextView timeStamp;
        TextView postContent;

        public NewPostHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view_new_post);
            profilePhoto = (ImageView) itemView.findViewById(R.id.image_view_post_profile);
            nickname = (TextView) itemView.findViewById(R.id.text_view_post_nickname);
            timeStamp = (TextView) itemView.findViewById(R.id.text_view_post_time_stamp);
            postContent = (TextView) itemView.findViewById(R.id.text_view_post_content);
        }
    }

    public NewPostCardAdapter(Context callingContext, List<DBUserPost> posts) {
        this.posts = posts;
        credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,                             // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
    }

    @Override
    public NewPostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_new_post_container, parent, false);
        NewPostHolder newPostHolder = new NewPostHolder(view);
        return newPostHolder;
    }

    @Override
    public void onBindViewHolder(NewPostHolder newPostHolder, int position) {
        DBUserPost curPost = posts.get(position);
        new LoadFBProfilePictureTask().execute(new asyncTaskObject(curPost.getFacebookID(), newPostHolder.profilePhoto));
        newPostHolder.nickname.setText(curPost.getNickname());
        newPostHolder.postContent.setText(curPost.getTextContent());

        long tsLong = System.currentTimeMillis()/1000;
        long timeInSeconds = tsLong - curPost.getTimeInSeconds();
        if (timeInSeconds < 60) {
            newPostHolder.timeStamp.setText(String.valueOf(timeInSeconds) + "s");
        }
        else if (timeInSeconds < 60 * 60) {
            long timeInMins = timeInSeconds / 60;
            newPostHolder.timeStamp.setText(String.valueOf(timeInMins) + "m");
        }
        else if (timeInSeconds < 60 * 60 * 24) {
            long timeInHrs = timeInSeconds/60/60;
            newPostHolder.timeStamp.setText(String.valueOf(timeInHrs) + "h");
        }
        else {
            long timeInDays = timeInSeconds/60/60/24;
            newPostHolder.timeStamp.setText(String.valueOf(timeInDays) + "d");
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void addPost(String newPostText, Bundle bundle) {
        DBUserPost newPost = new DBUserPost();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        newPost.setNickname(bundle.getString("NICKNAME"));
        newPost.setTimeInSeconds(System.currentTimeMillis() / 1000);
        newPost.setCognitoID(credentialsProvider.getIdentityId());
        newPost.setFacebookID(bundle.getString("FACEBOOK_ID"));
        newPost.setTimeStamp(df.format(c.getTime()));
        newPost.setTextContent(newPostText);
        new PushNewUserPostToDBTask().execute(newPost);
        posts.add(0, newPost);
        notifyItemInserted(0);
    }

    private Bitmap getFacebookProfilePicture(String userID) throws IOException {
        URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=small");
        return BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
    }

    /********************************** AsyncTasks **********************************/

    private class asyncTaskObject {
        public String facebookID;
        public ImageView imageView;
        asyncTaskObject(String facebookID, ImageView imageView) {
            this.facebookID = facebookID;
            this.imageView = imageView;
        }
    }

    private class LoadFBProfilePictureTask extends AsyncTask<asyncTaskObject, Void, Bitmap> {
        private ImageView imageView;
        @Override
        protected Bitmap doInBackground(asyncTaskObject... params) {
            Bitmap profileBitmap = null;
            try {
                profileBitmap = getFacebookProfilePicture(params[0].facebookID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "COULD NOT GET USER PROFILE");
            }
            imageView = params[0].imageView;
            return profileBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private class PushNewUserPostToDBTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost newUserPost= params[0];
            HashMap<String, AttributeValue> newUserPostMap = new HashMap<>();
            newUserPostMap.put("Nickname", new AttributeValue().withS(newUserPost.getNickname()));
            newUserPostMap.put("TimeInSeconds", new AttributeValue().withN(String.valueOf(newUserPost.getTimeInSeconds())));
            newUserPostMap.put("CognitoID", new AttributeValue().withS(newUserPost.getCognitoID()));
            newUserPostMap.put("FacebookID", new AttributeValue().withS(newUserPost.getFacebookID()));
            newUserPostMap.put("TimeStamp", new AttributeValue().withS(newUserPost.getTimeStamp()));
            newUserPostMap.put("TextContent", new AttributeValue().withS(newUserPost.getTextContent()));
            ddbClient.putItem(new PutItemRequest().withTableName("UserPosts").withItem(newUserPostMap));
            return null;
        }
    }
}
