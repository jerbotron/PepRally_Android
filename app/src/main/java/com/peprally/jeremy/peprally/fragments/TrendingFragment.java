package com.peprally.jeremy.peprally.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.peprally.jeremy.peprally.activities.HomeActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;

public class TrendingFragment extends Fragment {

    CognitoCachingCredentialsProvider credentialsProvider;
    private Context callingContext;

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "trending fragment created");
        callingContext = this.getActivity();
        credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,                             // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "trending fragment created view");
        View view = inflater.inflate(R.layout.fragment_trending, container, false);
        // Create an S3 client
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));

//        java.util.Date expiration = new java.util.Date();
//        long msec = expiration.getTime();
//        msec += 1000 * 60; // 1 min
//        expiration.setTime(msec);
//
//        GeneratePresignedUrlRequest generatePresignedUrlRequest =
//                new GeneratePresignedUrlRequest("rosterphotos", "Baseball/barrera_tres_1.jpg");
//        generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
//        generatePresignedUrlRequest.setExpiration(expiration);
//
//        URL s = s3.generatePresignedUrl(generatePresignedUrlRequest);

        String s = "https://s3.amazonaws.com/rosterphotos/Swimming+and+Diving/anderson_mark.jpg";

        ImageView iv = (ImageView) view.findViewById(R.id.test_s3_image);

//        Picasso.with(callingContext).load(s).into(iv);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "trending fragment resumed");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "trending fragment paused");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "trending fragment destroyed");
    }
}