package com.peprally.jeremy.peprally.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.peprally.jeremy.peprally.activities.HomeActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.PostCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrendingFragment extends Fragment {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private PostCardAdapter postCardAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout trendingSwipeRefreshContainer;

    // General Variables
    private List<DBUserPost> posts;
    private UserProfileParcel userProfileParcel;

    /***********************************************************************************************
     *************************************** FRAGMENT METHODS **************************************
     **********************************************************************************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trending, container, false);
        userProfileParcel = ((HomeActivity) getActivity()).getUserProfileParcel();

        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView = (RecyclerView) view.findViewById(R.id.id_recycler_view_trending_posts);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        // setup swipe refresh container
        trendingSwipeRefreshContainer = (SwipeRefreshLayout) view.findViewById(R.id.container_swipe_refresh_trending_posts);
        trendingSwipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAdapter();
            }
        });

        // new post fab onclick handler
        FloatingActionButton actionFAB = (FloatingActionButton) view.findViewById(R.id.fab_trending_action);
        actionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) getActivity()).launchNewPostActivity();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAdapter();
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void initializeAdapter(List<DBUserPost> result) {
        if (result != null && result.size() > 0) {
            posts = new ArrayList<>();
            for (DBUserPost userPost : result) {
                posts.add(userPost);
            }
            // Reverse Posts so they are shown in ascending order w.r.t time stamp
            Collections.sort(posts);
            Collections.reverse(posts);
            postCardAdapter = new PostCardAdapter(getActivity(), posts, userProfileParcel);
            recyclerView.setAdapter(postCardAdapter);
        }
        else {
            recyclerView.setAdapter(new EmptyAdapter());
        }
    }

    public void addPostToAdapter(String newPostText) {
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userProfileParcel.getProfileNickname());
        bundle.putString("FACEBOOK_ID", userProfileParcel.getFacebookID());
        bundle.putString("FIRST_NAME", userProfileParcel.getFirstname());
        if (postCardAdapter == null) {
            posts = new ArrayList<>();
            postCardAdapter = new PostCardAdapter(getActivity(), posts, userProfileParcel);
            recyclerView.setAdapter(postCardAdapter);
        }
        postCardAdapter.addPost(newPostText, bundle);
    }

    private void refreshAdapter() {
        new FetchTrendingPostsTask().execute();
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchTrendingPostsTask extends AsyncTask<Void, Void, PaginatedScanList<DBUserPost>> {
        @Override
        protected PaginatedScanList<DBUserPost> doInBackground(Void... params) {
            DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(getActivity().getApplicationContext());
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            return dynamoDBHelper.getMapper().scan(DBUserPost.class, scanExpression);
        }

        @Override
        protected void onPostExecute(PaginatedScanList<DBUserPost> result) {
            initializeAdapter(result);

            // stop refresh loading animation
            if (trendingSwipeRefreshContainer.isRefreshing())
                trendingSwipeRefreshContainer.setRefreshing(false);
        }
    }
}