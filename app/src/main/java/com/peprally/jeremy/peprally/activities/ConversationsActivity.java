package com.peprally.jeremy.peprally.activities;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.ConversationCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserConversation;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversationsActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private RecyclerView recyclerView;
    private SwipeRefreshLayout conversationSwipeRefreshContainer;

    // AWS Variables
    private DynamoDBHelper dynamoDBHelper;

    // General Variables
    private UserProfileParcel userProfileParcel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        dynamoDBHelper = new DynamoDBHelper(this);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        userProfileParcel.setCurrentActivity(ActivityEnum.CONVERSATIONS);

        // setup home button on action bar
        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_conversation);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        // setup swipe refresh container
        conversationSwipeRefreshContainer = (SwipeRefreshLayout) findViewById(R.id.id_container_swipe_refresh_conversation);
        conversationSwipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAdapter();
            }
        });

        // remove user new message alert
        new RemoveUserNewMessageAlertAsyncTask().execute(userProfileParcel.getCurrentUsername());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAdapter();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
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

    private void initializeAdapter(List<DBUserConversation> results) {
        if (results != null && results.size() > 0) {
            // Reverse notifications so they are shown in ascending order w.r.t time stamp
            Collections.sort(results);
            ConversationCardAdapter conversationCardAdapter = new ConversationCardAdapter(this, results, userProfileParcel);
            recyclerView.setAdapter(conversationCardAdapter);
        }
        else {
            recyclerView.setAdapter(new EmptyAdapter());
        }
    }

    private void refreshAdapter() {
        conversationSwipeRefreshContainer.post(new Runnable() {
            @Override
            public void run() {
                conversationSwipeRefreshContainer.setRefreshing(true);
            }
        });
        new FetchUserConversationsAsyncTask().execute(userProfileParcel.getCurrentUsername());
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    @SuppressWarnings("unchecked")
    private class FetchUserConversationsAsyncTask extends AsyncTask<String, Void, List<DBUserConversation>> {
        @Override
        protected List<DBUserConversation> doInBackground(String... usernames) {
            DBUserProfile userProfile = dynamoDBHelper.loadDBUserProfile(usernames[0]);
            List<DBUserConversation> userConversations = new ArrayList<>();
            if (userProfile != null && userProfile.getConversationIds().size() > 1) {   // set has a default value of "_"
                for (String conversationID : userProfile.getConversationIds()) {
                    if (!conversationID.equals("_")) {  // special case inside conversationID Set
                        DBUserConversation userConversation = new DBUserConversation();
                        userConversation.setConversationID(conversationID);
                        DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                                .withHashKeyValues(userConversation)
                                .withConsistentRead(true);

                        // query should just return a single conversation
                        List<DBUserConversation> convo = dynamoDBHelper.getMapper().query(DBUserConversation.class, queryExpression);
                        if (convo.size() == 1)
                            userConversations.add(convo.get(0));
                    }
                }
            }
            return userConversations;
        }

        @Override
        protected void onPostExecute(List<DBUserConversation> userConversations) {
            initializeAdapter(userConversations);

            // stop refresh loading animation
            if (conversationSwipeRefreshContainer.isRefreshing()) {
                conversationSwipeRefreshContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationSwipeRefreshContainer.setRefreshing(false);
                    }
                });
            }
        }
    }

    private class RemoveUserNewMessageAlertAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];
            DBUserProfile userProfile = dynamoDBHelper.loadDBUserProfile(username);
            if (userProfile != null) {
                userProfile.setHasNewMessage(false);
                dynamoDBHelper.saveDBObject(userProfile);
            }
            return null;
        }
    }
}
