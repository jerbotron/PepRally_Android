package com.peprally.jeremy.peprally.activities;

import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.NotificationCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;
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
    private NotificationCardAdapter notificationCardAdapter;

    // AWS Variables
    private DynamoDBHelper dbHelper;

    // General Variables
    private UserProfileParcel userProfileParcel;
    private List<DBUserNotification> notifications;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        dbHelper = new DynamoDBHelper(this);

        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");

        new FetchUserNotificationsDBTask().execute();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Recycler View Setup
        recyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_notifications);
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
            notifications = new ArrayList<>();
            for (DBUserNotification userNotification : results) {
                notifications.add(userNotification);
            }
            // Reverse notifications so they are shown in ascending order w.r.t time stamp
            Collections.sort(notifications);
            Collections.reverse(notifications);
            notificationCardAdapter = new NotificationCardAdapter(this, notifications, userProfileParcel);
            recyclerView.setAdapter(notificationCardAdapter);
        }
        else {
            // no notifications
            LinearLayout activityContainer = (LinearLayout) findViewById(R.id.id_activity_notifications);
            TextView emptyAdapterText = new TextView(this);
            emptyAdapterText.setText(getResources().getString(R.string.placeholder_no_notifications));
            emptyAdapterText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            emptyAdapterText.setGravity(Gravity.CENTER);
            emptyAdapterText.setPadding(16,16,16,16);
            activityContainer.addView(emptyAdapterText);
        }
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
                    .withConsistentRead(false);
            return dbHelper.getMapper().query(DBUserNotification.class, queryExpression);
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBUserNotification> results) {
            initializeAdapter(results);
        }
    }
}
