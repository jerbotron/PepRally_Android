package com.peprally.jeremy.peprally.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.AsyncHelpers;
import com.peprally.jeremy.peprally.utils.Helpers;

public class NewPostCommentActivity extends AppCompatActivity {

    private Bundle postCommentBundle;

    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    private static final String TAG = NewPostCommentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post_comment);

        // Set up AWS members
        credentialsProvider = new CognitoCachingCredentialsProvider(
                this,                                       // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        // Set up toolbar buttons
        final ActionBar supportActionBar = getSupportActionBar();
        assert (supportActionBar != null);
        supportActionBar.setTitle("Comments");
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        // Set up main post container
        final ImageView mainPostProfileImage = (ImageView) findViewById(R.id.id_image_view_comment_main_post_profile);
        final TextView mainPostTimeStamp = (TextView) findViewById(R.id.id_text_view_comment_main_post_time_stamp);
        final TextView mainPostNickname = (TextView) findViewById(R.id.id_text_view_comment_main_post_nickname);
        final TextView mainPostTextContent = (TextView) findViewById(R.id.id_text_view_comment_main_post_content);
        final ImageButton mainPostThumbsUp = (ImageButton) findViewById(R.id.id_image_button_comment_main_post_thumbs_up);
        final ImageButton mainPostThumbsDown = (ImageButton) findViewById(R.id.id_image_button_comment_main_post_thumbs_down);
        final TextView mainPostLikesCount = (TextView) findViewById(R.id.id_text_view_comment_main_post_likes);
        final TextView mainPostCommentsCount = (TextView) findViewById(R.id.id_text_view_comment_main_post_comments);

        assert (mainPostNickname != null && mainPostTextContent != null && mainPostTimeStamp != null &&
                mainPostLikesCount != null && mainPostCommentsCount != null);

        postCommentBundle = getIntent().getBundleExtra("POST_COMMENT_BUNDLE");

        int likesCount = postCommentBundle.getInt("LIKES_COUNT");

        new AsyncHelpers.LoadFBProfilePictureTask().execute(new AsyncHelpers.asyncTaskObjectProfileImage(postCommentBundle.getString("FACEBOOK_ID"), mainPostProfileImage));

        long tsLong = System.currentTimeMillis()/1000;
        long timeInSeconds = tsLong - postCommentBundle.getLong("TIME_IN_SECONDS");
        if (timeInSeconds < 60) {
            mainPostTimeStamp.setText(String.valueOf(timeInSeconds) + "s");
        }
        else if (timeInSeconds < 60 * 60) {
            long timeInMins = timeInSeconds / 60;
            mainPostTimeStamp.setText(String.valueOf(timeInMins) + "m");
        }
        else if (timeInSeconds < 60 * 60 * 24) {
            long timeInHrs = timeInSeconds/60/60;
            mainPostTimeStamp.setText(String.valueOf(timeInHrs) + "h");
        }
        else {
            long timeInDays = timeInSeconds/60/60/24;
            mainPostTimeStamp.setText(String.valueOf(timeInDays) + "d");
        }

        mainPostNickname.setText(postCommentBundle.getString("NICKNAME"));
        mainPostTextContent.setText(postCommentBundle.getString("TEXT_CONTENT"));
        mainPostCommentsCount.setText(String.valueOf(postCommentBundle.getInt("COMMENTS_COUNT")));
        mainPostLikesCount.setText(String.valueOf(likesCount));
        if (likesCount > 0) {
            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
        }
        else if (likesCount < 0) {
            mainPostLikesCount.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
        }

        new AsyncHelpers.CheckIfUserLikedDislikedMainPost().execute(
                new AsyncHelpers.asyncTaskObjectThumbsUpDownButtons(mainPostThumbsUp,
                                                                    mainPostThumbsDown,
                                                                    getApplicationContext(),
                                                                    postCommentBundle,
                                                                    mapper));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Hide soft keyboard if keyboard is up
                EditText et = (EditText) findViewById(R.id.id_edit_text_new_comment);
                Helpers.hideSoftkeyboard(this, et);
                onBackPressed();
                return true;
            case R.id.id_item_delete_post:
                new DeletePostFromDBTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    /********************************** AsyncTasks **********************************/

    private class DeletePostFromDBTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            DBUserPost post = mapper.load(DBUserPost.class, postCommentBundle.getString("NICKNAME"), postCommentBundle.getLong("TIME_IN_SECONDS"));
            mapper.delete(post);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Hide soft keyboard if keyboard is up
            EditText et = (EditText) findViewById(R.id.id_edit_text_new_comment);
            Helpers.hideSoftkeyboard(NewPostCommentActivity.this, et);
            setResult(Activity.RESULT_OK);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }
}
