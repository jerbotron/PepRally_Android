package com.peprally.jeremy.peprally.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.ProfileActivity;

import java.util.Random;

public class ProfilePostsFragment extends Fragment {

    private static final String TAG = ProfilePostsFragment.class.getSimpleName();

    private final class NewPostHint {
        private final String hint_1 = getResources().getString(R.string.default_post_question_1);
        private final String hint_2 = getResources().getString(R.string.default_post_question_2);
        private final String hint_3 = getResources().getString(R.string.default_post_question_3);
        private final String hint_4 = getResources().getString(R.string.default_post_question_4);
        private final String hint_5 = getResources().getString(R.string.default_post_question_5);

        private String getHint(int n) {
            switch (n) {
                case 0:
                    return hint_1;
                case 1:
                    return hint_2;
                case 2:
                    return hint_3;
                case 3:
                    return hint_4;
                case 4:
                    return hint_5;
                default:
                    return null;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_posts, container, false);
        Bundle UPB = getArguments();
        final TextView emptyMsgView = (TextView) view.findViewById(R.id.profile_posts_empty_text);
        if (UPB.getBoolean("SELF_PROFILE")) {
            emptyMsgView.setText("You have not created any posts yet!");
        }
        else {
            emptyMsgView.setText(UPB.getString("FIRST_NAME") + " has not created any posts yet!");
        }
        return view;
    }
}
