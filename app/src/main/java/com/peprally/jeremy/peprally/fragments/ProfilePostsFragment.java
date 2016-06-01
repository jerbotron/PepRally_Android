package com.peprally.jeremy.peprally.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.adapters.EmptyAdapter;
import com.peprally.jeremy.peprally.adapters.PostCardAdapter;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.List;

public class ProfilePostsFragment extends Fragment {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private LinearLayout postsContainer;
    private PostCardAdapter postCardAdapter;
    private RecyclerView recyclerView;
    private TextView noPostsText;

    // General Variables
    private static final String TAG = ProfilePostsFragment.class.getSimpleName();
    private List<DBUserPost> posts;
    private UserProfileParcel userProfileParcel;

    /***********************************************************************************************
     *************************************** FRAGMENT METHODS **************************************
     **********************************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get copy of userProfileParcel from ProfileActivity
        userProfileParcel = ((ProfileActivity) getActivity()).getUserProfileParcel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_posts, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_profile_posts);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        // Temporarily set recyclerView to an EmptyAdapter until we fetch real data
        recyclerView.setAdapter(new EmptyAdapter());
        recyclerView.setLayoutManager(rvLayoutManager);

        noPostsText = (TextView) view.findViewById(R.id.profile_posts_empty_text);
        postsContainer = (LinearLayout) view.findViewById(R.id.container_profile_posts);
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
    public void addPostToAdapter(String newPostText) {
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userProfileParcel.getNickname());
        bundle.putString("FACEBOOK_ID", userProfileParcel.getFacebookID());
        bundle.putString("FIRST_NAME", userProfileParcel.getFirstname());
        if (postCardAdapter == null) {
            posts = new ArrayList<>();
            postCardAdapter = new PostCardAdapter(getActivity(), posts, userProfileParcel);
            recyclerView.setAdapter(postCardAdapter);
        }
        // Checks is this post is the first user post
        if (userProfileParcel.getPostsCount() == 0) {
            postsContainer.removeView(noPostsText);
        }
        postCardAdapter.addPost(newPostText, bundle);
    }

    public void refreshAdapter() {
        if (userProfileParcel.getPostsCount() == 0) {
            if (userProfileParcel.getIsSelfProfile()) {
                noPostsText.setText("You have not created any posts yet!");
            }
            else {
                noPostsText.setText(userProfileParcel.getFirstname() + " has not created any posts yet!");
            }
        }
        else {
            postsContainer.removeView(noPostsText);
            new FetchUserPostsTask().execute(userProfileParcel.getNickname());
        }
    }

    private void initializeAdapter(List<DBUserPost> result) {
        posts = new ArrayList<>();
        for (DBUserPost userPost : result) {
            posts.add(userPost);
        }
        postCardAdapter = new PostCardAdapter(getActivity(), posts, userProfileParcel);
        recyclerView.setAdapter(postCardAdapter);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
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
            return mapper.query(DBUserPost.class, queryExpression);
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<DBUserPost> result) {
            initializeAdapter(result);
        }
    }
}
