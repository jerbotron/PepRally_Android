package com.peprally.jeremy.peprally.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.adapter.EmptyAdapter;
import com.peprally.jeremy.peprally.adapter.NewPostCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;

import java.util.ArrayList;
import java.util.List;

public class ProfilePostsFragment extends Fragment {

    private List<DBUserPost> posts;
    private LinearLayout profilePostsContainer;
    private NewPostCardAdapter newPostCardAdapter;
    private RecyclerView recyclerView;
    private TextView emptyMsgView;
    private boolean dataFetched = false, emptyPosts = false;

    private Bundle userProfileBundle;
    private static final String TAG = ProfilePostsFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_posts, container, false);
        userProfileBundle = getArguments();

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_profile_posts);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        emptyMsgView = (TextView) view.findViewById(R.id.profile_posts_empty_text);
        profilePostsContainer = (LinearLayout) view.findViewById(R.id.container_profile_posts);

        if (userProfileBundle.getInt("POSTS_COUNT") == 0) {
            emptyPosts = true;
            if (userProfileBundle.getBoolean("SELF_PROFILE")) {
                emptyMsgView.setText("You have not created any posts yet!");
            }
            else {
                emptyMsgView.setText(userProfileBundle.getString("FIRST_NAME") + " has not created any posts yet!");
            }
        }
        else {
            profilePostsContainer.removeView(emptyMsgView);
            new FetchUserPostsTask().execute(userProfileBundle.getString("NICKNAME"));
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initializeAdapter(List<DBUserPost> result) {
        posts = new ArrayList<>();
        for (DBUserPost userPost : result) {
            posts.add(userPost);
        }
        newPostCardAdapter = new NewPostCardAdapter(getActivity(), posts);
        recyclerView.setAdapter(newPostCardAdapter);
    }

    public void addPostToAdapter(String newPostText) {
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userProfileBundle.getString("NICKNAME"));
        bundle.putString("FACEBOOK_ID", userProfileBundle.getString("FACEBOOK_ID"));
        bundle.putString("FIRST_NAME", userProfileBundle.getString("FIRST_NAME"));
        if (newPostCardAdapter == null) {
            posts = new ArrayList<>();
            newPostCardAdapter = new NewPostCardAdapter(getActivity(), posts);
            recyclerView.setAdapter(newPostCardAdapter);
        }
        if (userProfileBundle.getInt("POSTS_COUNT") == 0) {
            profilePostsContainer.removeView(emptyMsgView);
        }
        newPostCardAdapter.addPost(newPostText, bundle);
    }

    public void refreshAdapter() {
        new FetchUserPostsTask().execute(userProfileBundle.getString("NICKNAME"));
    }

    /********************************** AsyncTasks **********************************/

    private class FetchUserPostsTask extends AsyncTask<String, Void, PaginatedQueryList<DBUserPost>> {
        @Override
        protected PaginatedQueryList<DBUserPost> doInBackground(String... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getActivity(),                            // Context
                    AWSCredentialProvider.IDENTITY_POOL_ID,   // Identity Pool ID
                    AWSCredentialProvider.COGNITO_REGION      // Region
            );
            String nickname = params[0];
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            s3.setRegion(Region.getRegion(Regions.US_EAST_1));

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            DBUserPost userPost = new DBUserPost();
            userPost.setNickname(nickname);
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(userPost)
                    .withConsistentRead(false)
                    .withScanIndexForward(false);
            PaginatedQueryList<DBUserPost> result = mapper.query(DBUserPost.class, queryExpression);
            return result;
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBUserPost> result) {
            dataFetched = true;
            initializeAdapter(result);
        }
    }
}
