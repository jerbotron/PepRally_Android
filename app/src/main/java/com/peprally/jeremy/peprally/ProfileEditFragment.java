package com.peprally.jeremy.peprally;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ProfileEditFragment extends Fragment {

    private static final String TAG = ProfileEditFragment.class.getSimpleName();

    public ProfileEditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        String userFirstName = getArguments().getString("firstName");
        Integer userAge = getArguments().getInt("age");
        TextView textViewFirstName = (TextView) view.findViewById(R.id.profile_edit_name_age);
        textViewFirstName.setText(userFirstName + ", " + Integer.toString(userAge));

        // Auto pop-up keyboard to show user edit is available
        EditText editText = (EditText) view.findViewById(R.id.profile_edit_motto);
        editText.requestFocus();
        return view;
    }
}