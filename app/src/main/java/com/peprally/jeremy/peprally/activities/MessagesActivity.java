package com.peprally.jeremy.peprally.activities;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;

public class MessagesActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private RecyclerView recyclerView;
    private SwipeRefreshLayout notificationsSwipeRefreshContainer;

    // AWS Variables
    private DynamoDBHelper dynamoDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        dynamoDBHelper = new DynamoDBHelper(this);
    }
}
