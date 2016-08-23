package com.peprally.jeremy.peprally.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.peprally.jeremy.peprally.activities.HomeActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.PostCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.custom.UserPostComparator;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrendingFragment extends Fragment {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private PostCardAdapter postCardAdapter;
    private ImageButton imageButtonHottest, imageButtonLatest;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout trendingSwipeRefreshContainer;

    // General Variables
    private ArrayList<DBUserPost> posts;
    private UserProfileParcel userProfileParcel;
    private TrendingModeEnum trendingMode;

    private enum TrendingModeEnum {
        HOTTEST,
        LATEST
    }

    /***********************************************************************************************
     *************************************** FRAGMENT METHODS **************************************
     **********************************************************************************************/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userProfileParcel = ((HomeActivity) getActivity()).getUserProfileParcel();
        trendingMode = TrendingModeEnum.HOTTEST;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trending, container, false);

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

        imageButtonHottest = (ImageButton) view.findViewById(R.id.id_image_button_trending_hottest);
        imageButtonLatest = (ImageButton) view.findViewById(R.id.id_image_button_trending_latest);

        if (imageButtonHottest != null && imageButtonLatest != null) {
            imageButtonHottest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleTrendingModeIsHottestView(true);
                }
            });

            imageButtonLatest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleTrendingModeIsHottestView(false);
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAdapter();
    }

    /***********************************************************************************************
     *********************************** GENERAL_METHODS ********************************
     **********************************************************************************************/
    private void updateCachedPosts(List<DBUserPost> results) {
        posts = new ArrayList<>(results);
    }

    private ArrayList<DBUserPost> sortPosts(TrendingModeEnum trendingMode, ArrayList<DBUserPost> posts) {
        switch (trendingMode) {
            case HOTTEST:
                Collections.sort(posts, UserPostComparator.decending(UserPostComparator.getComparator(UserPostComparator.HOTTEST_SORT, UserPostComparator.LATEST_SORT)));
                break;
            case LATEST:
                // Reverse Posts so they are shown in ascending order w.r.t time stamp
                Collections.sort(posts, UserPostComparator.decending(UserPostComparator.getComparator(UserPostComparator.LATEST_SORT)));
                break;
        }
        return posts;
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void initializeAdapter(ArrayList<DBUserPost> userPosts) {
        if (userPosts != null && userPosts.size() > 0) {

            userPosts = sortPosts(trendingMode, userPosts);

            postCardAdapter = new PostCardAdapter(getActivity(), userPosts, userProfileParcel);
            recyclerView.swapAdapter(postCardAdapter, true);
        }
        else {
            recyclerView.swapAdapter(new EmptyAdapter(), true);
        }
    }

    public void addPostToAdapter(String newPostText) {
        Bundle bundle = new Bundle();
        bundle.putString("USERNAME", userProfileParcel.getProfileUsername());
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
        new FetchUserPostsTask().execute();
    }

    public void toggleTrendingModeIsHottestView(boolean isTrendingModeHottest) {
        if (imageButtonHottest != null && imageButtonLatest != null) {
            // start on load progress circle animation and get latest posts
            trendingSwipeRefreshContainer.setRefreshing(true);
            trendingMode = (isTrendingModeHottest) ? TrendingModeEnum.HOTTEST : TrendingModeEnum.LATEST;
            refreshAdapter();

            // update UI buttons
            if (isTrendingModeHottest) {
                imageButtonHottest.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_trending_on));
                imageButtonLatest.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_clock));
                imageButtonHottest.setClickable(false);
                imageButtonLatest.setClickable(true);
            } else {
                imageButtonHottest.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_trending));
                imageButtonLatest.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_clock_on));
                imageButtonHottest.setClickable(true);
                imageButtonLatest.setClickable(false);
            }
        }
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class FetchUserPostsTask extends AsyncTask<Void, Void, PaginatedScanList<DBUserPost>> {
        @Override
        protected PaginatedScanList<DBUserPost> doInBackground(Void... params) {
            DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(getActivity().getApplicationContext());
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            return dynamoDBHelper.getMapper().scan(DBUserPost.class, scanExpression);
        }

        @Override
        protected void onPostExecute(PaginatedScanList<DBUserPost> results) {
            updateCachedPosts(results);
            initializeAdapter(posts);

            // stop refresh loading animation
            if (trendingSwipeRefreshContainer.isRefreshing())
                trendingSwipeRefreshContainer.setRefreshing(false);
        }
    }
}