package com.peprally.jeremy.peprally;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

public class ProfileViewFragment extends Fragment {

    private static final String TAG = ProfileViewFragment.class.getSimpleName();

    public ProfileViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_view, container, false);
        String userFirstName = getArguments().getString("firstName");
        Integer userAge = getArguments().getInt("age");
        TextView textViewFirstName = (TextView) view.findViewById(R.id.profile_view_name_age);
        textViewFirstName.setText(userFirstName + ", " + Integer.toString(userAge));
        return view;
    }
}