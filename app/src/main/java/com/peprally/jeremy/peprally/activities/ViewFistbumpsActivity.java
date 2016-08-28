package com.peprally.jeremy.peprally.activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.FistbumpedUserCardAdapter;
import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;
import com.peprally.jeremy.peprally.utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ViewFistbumpsActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI  Variables
    private RecyclerView recyclerView;

    // AWS Variables
    private DynamoDBHelper dynamoDBHelper;

    // General Variables
    private UserProfileParcel userProfileParcel;
    private DBUserPost mainPost;
    private int commentIndex;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_fistbumps);

        dynamoDBHelper = new DynamoDBHelper(this);

        // initialize local variables
        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        userProfileParcel.setCurrentActivity(ActivityEnum.VIEWFISTBUMPS);
        mainPost = getIntent().getParcelableExtra("USER_POST");
        commentIndex = getIntent().getIntExtra("COMMENT_INDEX", Constants.INTEGER_INVALID);

        ArrayList<String> fistbumpedUsers = getFistbumpedUsers(mainPost, commentIndex);

        new FetchFistbumpedUsersDBTask().execute(fistbumpedUsers);

        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Recycler View Setup
        recyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_fistbumped_users);
        final LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***********************************************************************************************
     **************************************** GENERAL_METHODS **************************************
     **********************************************************************************************/
    public void launchUserIsDeletedDialog(final String deletedUsername) {
        final AlertDialog.Builder dialogBuilderUserDeleted = new AlertDialog.Builder(ViewFistbumpsActivity.this);
        final View dialogViewConfirmDelete = View.inflate(ViewFistbumpsActivity.this, R.layout.dialog_confirm_delete, null);
        dialogBuilderUserDeleted.setView(dialogViewConfirmDelete);
        dialogBuilderUserDeleted.setMessage("Oops, looks like this user has deleted their account!");
        dialogBuilderUserDeleted.setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new RemoveFistbumpedUserAsyncTask().execute(deletedUsername);
            }
        });

        dialogBuilderUserDeleted.create().show();
    }

    private ArrayList<String> getFistbumpedUsers(DBUserPost userPost, int commentIndex) {
        if (commentIndex == Constants.INTEGER_INVALID) {    // post fistbumps
            return new ArrayList<>(userPost.getFistbumpedUsers());
        } else {                                            // comment fistbumps
            return new ArrayList<>(userPost.getComments().get(commentIndex).getFistbumpedUsers());
        }
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void initializeAdapter(List<DBUserProfile> results) {
        if (results != null && results.size() > 0) {
            FistbumpedUserCardAdapter fistbumpedUserCardAdapter = new FistbumpedUserCardAdapter(this, results, userProfileParcel);
            recyclerView.setAdapter(fistbumpedUserCardAdapter);
        }
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchFistbumpedUsersDBTask extends AsyncTask<ArrayList<String>, Void, List<DBUserProfile>> {
        boolean fistbumpedUsersNeedUpdate = false;
        @Override
        @SafeVarargs
        final protected List<DBUserProfile> doInBackground(ArrayList<String>... params) {
            ArrayList<String> fistbumpedUsers = params[0];
            if (fistbumpedUsers != null) {
                List<DBUserProfile> userProfiles = new ArrayList<>();
                for (int i = 0; i < fistbumpedUsers.size(); ++i) {
                    DBUserProfile userProfile = dynamoDBHelper.loadDBUserProfile(fistbumpedUsers.get(i));
                    if (userProfile != null) {
                        userProfiles.add(userProfile);
                    } else {
                        fistbumpedUsers.remove(i);
                        fistbumpedUsersNeedUpdate = true;
                    }
                }

                if (fistbumpedUsersNeedUpdate) {
                    if (commentIndex == Constants.INTEGER_INVALID) {
                        mainPost.setFistbumpedUsers(new HashSet<>(fistbumpedUsers));
                    } else {
                        mainPost.getComments().get(commentIndex).setFistbumpedUsers(new HashSet<>(fistbumpedUsers));
                    }
                    mainPost.setFistbumpsCount(fistbumpedUsers.size());
                    dynamoDBHelper.saveDBObject(mainPost);
                }

                return userProfiles;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<DBUserProfile> results) {
            initializeAdapter(results);
        }
    }

    @SuppressWarnings("unchecked")
    private class RemoveFistbumpedUserAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];
            if (commentIndex == Constants.INTEGER_INVALID) {
                mainPost.removeFistbumpedUser(username);
            } else {
                ArrayList<Comment> postComments = mainPost.getComments();
                postComments.get(commentIndex).removeFistbumpedUser(username);
                mainPost.setComments(postComments);
            }
            dynamoDBHelper.saveDBObject(mainPost);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new FetchFistbumpedUsersDBTask().execute(getFistbumpedUsers(mainPost, commentIndex));
        }
    }
}
