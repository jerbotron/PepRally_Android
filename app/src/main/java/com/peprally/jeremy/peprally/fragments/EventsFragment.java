package com.peprally.jeremy.peprally.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.peprally.jeremy.peprally.activities.HomeActivity;
import com.peprally.jeremy.peprally.R;

public class EventsFragment extends Fragment {

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "events fragment created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "events fragment created view");
        return inflater.inflate(R.layout.fragment_events, container, false);
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "events fragment resumed");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "events fragment paused");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "events fragment destroyed");
    }

}
