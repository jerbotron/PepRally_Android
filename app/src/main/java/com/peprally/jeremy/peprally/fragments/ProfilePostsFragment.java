package com.peprally.jeremy.peprally.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.PostCardAdapter;
import com.peprally.jeremy.peprally.custom.ui.EmptyViewSwipeRefreshLayout;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.interfaces.PostContainerInterface;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

import java.util.ArrayList;
import java.util.List;

public class ProfilePostsFragment extends Fragment {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private PostCardAdapter postCardAdapter;
    private RecyclerView recyclerView;
    private TextView noPostsText;
    private EmptyViewSwipeRefreshLayout profilePostsSwipeRefreshContainer;

    // General Variables
//    private static final String TAG = ProfilePostsFragment.class.getSimpleName();
    private List<DBUserPost> posts;
    private UserProfileParcel userProfileParcel;

    /***********************************************************************************************
     *************************************** FRAGMENT METHODS **************************************
     **********************************************************************************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_posts, container, false);

        userProfileParcel = ((ProfileActivity) getActivity()).getUserProfileParcel();

        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_profile_posts);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        // setup swipe refresh container
        profilePostsSwipeRefreshContainer = (EmptyViewSwipeRefreshLayout) view.findViewById(R.id.container_swipe_refresh_profile_posts);
        profilePostsSwipeRefreshContainer.setRefreshing(true);
        profilePostsSwipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAdapter();
            }
        });

        noPostsText = (TextView) view.findViewById(R.id.profile_posts_empty_text);
        return view;
    }

    @Override
    public void onResume() {
        refreshAdapter();
        super.onResume();
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void initializeAdapter(List<DBUserPost> result) {
        posts = new ArrayList<>();
        for (DBUserPost userPost : result) {
            posts.add(userPost);
        }
        postCardAdapter = new PostCardAdapter(getActivity(), posts, userProfileParcel);
        recyclerView.swapAdapter(postCardAdapter, true);
    }

    public void addPostToAdapter(String newPostText) {
        Bundle bundle = new Bundle();
        bundle.putString("USERNAME", userProfileParcel.getProfileUsername());
        bundle.putString("FACEBOOK_ID", userProfileParcel.getFacebookID());
        bundle.putString("FIRST_NAME", userProfileParcel.getFirstname());
        if (postCardAdapter == null) {
            posts = new ArrayList<>();
            postCardAdapter = new PostCardAdapter(getActivity(), posts, userProfileParcel);
            recyclerView.swapAdapter(postCardAdapter, true);
        }
        postCardAdapter.addPost(newPostText, bundle);
    }

    public void refreshAdapter() {
        new FetchUserPostsTask().execute(userProfileParcel.getProfileUsername());
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    @SuppressWarnings("unchecked")
    private class FetchUserPostsTask extends AsyncTask<String, Void, PaginatedQueryList<DBUserPost>> {
        @Override
        protected PaginatedQueryList<DBUserPost> doInBackground(String... params) {
            String username = params[0];
            if (username != null) {
                DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(getActivity().getApplicationContext());
                DBUserPost userPost = new DBUserPost();
                userPost.setUsername(username);
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(userPost)
                        .withConsistentRead(true)
                        .withScanIndexForward(false);
                return dynamoDBHelper.getMapper().query(DBUserPost.class, queryExpression);
            }
            return null;
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBUserPost> result) {
            if (result != null && result.size() > 0) {
                userProfileParcel.setPostsCount(result.size());
                initializeAdapter(result);
            }
            else {
                recyclerView.swapAdapter(new EmptyAdapter(), true);
                if (userProfileParcel.isSelfProfile()) {
                    noPostsText.setText(getResources().getString(R.string.no_posts_message));
                }
                else {
                    String s = userProfileParcel.getFirstname() + " has not created any posts yet!";
                    noPostsText.setText(s);
                }
            }

            // stop refresh loading animation
            if (profilePostsSwipeRefreshContainer.isRefreshing())
                profilePostsSwipeRefreshContainer.setRefreshing(false);
        }
    }
}
