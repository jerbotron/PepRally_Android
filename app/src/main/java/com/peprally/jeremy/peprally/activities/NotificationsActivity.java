package com.peprally.jeremy.peprally.activities;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.NotificationCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private RecyclerView recyclerView;
    private SwipeRefreshLayout notificationsSwipeRefreshContainer;

    // AWS Variables
    private DynamoDBHelper dbHelper;

    // General Variables
    private UserProfileParcel userProfileParcel;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        dbHelper = new DynamoDBHelper(this);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");

        // setup home button on action bar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_notifications);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        // setup swipe refresh container
        notificationsSwipeRefreshContainer = (SwipeRefreshLayout) findViewById(R.id.id_container_swipe_refresh_notifications);
        notificationsSwipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAdapter();
            }
        });
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

    private void initializeAdapter(List<DBUserNotification> results) {
        if (results != null && results.size() > 0) {
            List<DBUserNotification> notifications = new ArrayList<>();
            for (DBUserNotification userNotification : results) {
                notifications.add(userNotification);
            }
            // Reverse notifications so they are shown in ascending order w.r.t time stamp
            Collections.sort(notifications);
            Collections.reverse(notifications);
            NotificationCardAdapter notificationCardAdapter = new NotificationCardAdapter(this, notifications);
            recyclerView.setAdapter(notificationCardAdapter);
        }
        else {
            recyclerView.setAdapter(new EmptyAdapter());
        }
    }

    private void refreshAdapter() {
        new FetchUserNotificationsDBTask().execute();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchUserNotificationsDBTask extends AsyncTask<Void, Void, PaginatedQueryList<DBUserNotification>> {
        @Override
        protected PaginatedQueryList<DBUserNotification> doInBackground(Void... params) {
            DBUserNotification userNotification = new DBUserNotification();
            userNotification.setNickname(userProfileParcel.getCurUserNickname());
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(userNotification)
                    .withConsistentRead(true);
            return dbHelper.getMapper().query(DBUserNotification.class, queryExpression);
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBUserNotification> results) {
            initializeAdapter(results);

            // stop refresh loading animation
            if (notificationsSwipeRefreshContainer.isRefreshing())
                notificationsSwipeRefreshContainer.setRefreshing(false);
        }
    }
}
