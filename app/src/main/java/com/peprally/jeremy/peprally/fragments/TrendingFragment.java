package com.peprally.jeremy.peprally.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.peprally.jeremy.peprally.activities.HomeActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.NewPostActivity;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.PostCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;

import java.util.ArrayList;
import java.util.List;

public class TrendingFragment extends Fragment {
    private List<DBUserPost> posts;
    private ScrollView postsContainer;
    private PostCardAdapter postCardAdapter;
    private RecyclerView recyclerView;

    private Bundle userProfileBundle;

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trending, container, false);
        userProfileBundle = getArguments();

        recyclerView = (RecyclerView) view.findViewById(R.id.id_recycler_view_trending_posts);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        postsContainer = (ScrollView) view.findViewById(R.id.container_trending_posts);

//        refreshAdapter();

        FloatingActionButton actionFAB = (FloatingActionButton) view.findViewById(R.id.fab_trending_action);
        actionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewPostActivity.class);
                startActivityForResult(intent, ProfileActivity.NEW_POST_REQUEST_CODE);
                getActivity().overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
            }
        });

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
        postCardAdapter = new PostCardAdapter(getActivity(), posts);
        recyclerView.setAdapter(postCardAdapter);
    }

    public void addPostToAdapter(String newPostText) {
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userProfileBundle.getString("NICKNAME"));
        bundle.putString("FACEBOOK_ID", userProfileBundle.getString("FACEBOOK_ID"));
        bundle.putString("FIRST_NAME", userProfileBundle.getString("FIRST_NAME"));
        if (postCardAdapter == null) {
            posts = new ArrayList<>();
            postCardAdapter = new PostCardAdapter(getActivity(), posts);
            recyclerView.setAdapter(postCardAdapter);
        }
        postCardAdapter.addPost(newPostText, bundle);
    }

    public void refreshAdapter() {
        new FetchTrendingPostsTask().execute();
    }

    /********************************** AsyncTasks **********************************/

    private class FetchTrendingPostsTask extends AsyncTask<Void, Void, PaginatedScanList<DBUserPost>> {
        @Override
        protected PaginatedScanList<DBUserPost> doInBackground(Void... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getActivity(),                            // Context
                    AWSCredentialProvider.IDENTITY_POOL_ID,   // Identity Pool ID
                    AWSCredentialProvider.COGNITO_REGION      // Region
            );
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            s3.setRegion(Region.getRegion(Regions.US_EAST_1));

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            return mapper.scan(DBUserPost.class, scanExpression);
        }

        @Override
        protected void onPostExecute(PaginatedScanList<DBUserPost> result) {
            initializeAdapter(result);
        }
    }
}