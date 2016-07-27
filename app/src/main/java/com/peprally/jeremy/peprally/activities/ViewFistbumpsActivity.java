package com.peprally.jeremy.peprally.activities;

import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.FistbumpedUserCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
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
    UserProfileParcel userProfileParcel;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_fistbumps);

        dynamoDBHelper = new DynamoDBHelper(this);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");

        List<String> fistbumpedUsers = getIntent().getStringArrayListExtra("FISTBUMPED_USERS");
        if (fistbumpedUsers != null)
            new FetchFistbumpedUsersDBTask().execute(fistbumpedUsers);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Recycler View Setup
        recyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_fistbumped_users);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
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
    private class FetchFistbumpedUsersDBTask extends AsyncTask<List<String>, Void, List<DBUserProfile>> {
        @Override
        @SafeVarargs
        final protected List<DBUserProfile> doInBackground(List<String>... params) {
            List<DBUserProfile> userProfiles = new ArrayList<>();
            for (String nickname : params[0]) {
                DBUserProfile userProfile = dynamoDBHelper.loadDBUserProfile(nickname);
                if (userProfile != null)
                    userProfiles.add(userProfile);
            }
            return userProfiles;
        }

        @Override
        protected void onPostExecute(List<DBUserProfile> results) {
            initializeAdapter(results);
        }
    }
}
