package com.peprally.jeremy.peprally;

import android.graphics.Typeface;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_view, container, false);
//        setupUserProfile(view, getArguments());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUserProfile(getView(), getArguments());
    }

    public void setupUserProfile(View view, Bundle UPB) {
        if (UPB != null) {
            TextView textViewFirstName = (TextView) view.findViewById(R.id.profile_view_name_age);
            TextView textViewMotto = (TextView) view.findViewById(R.id.profile_view_motto);
            TextView textViewFavTeam = (TextView) view.findViewById(R.id.profile_view_fav_team);
            TextView textViewFavPlayer = (TextView) view.findViewById(R.id.profile_view_fav_player);
            TextView textViewPepTalk = (TextView) view.findViewById(R.id.profile_view_pep_talk);
            TextView textViewTrashTalk = (TextView) view.findViewById(R.id.profile_view_trash_talk);

            textViewFirstName.setText(UPB.getString("FIRST_NAME") + ", " + Integer.toString(23));
            if (UPB.getString("MOTTO") == null) {
                textViewMotto.setTypeface(null, Typeface.ITALIC);
                textViewMotto.setText(getResources().getString(R.string.default_motto));
            } else {
                textViewMotto.setText(UPB.getString("MOTTO"));
            }
            if (UPB.getString("FAVORITE_TEAM") == null) {
                textViewFavTeam.setTypeface(null, Typeface.ITALIC);
                textViewFavTeam.setText(getResources().getString(R.string.default_fav_team));
            } else {
                textViewFavTeam.setText(UPB.getString("FAVORITE_TEAM"));
            }
            if (UPB.getString("FAVORITE_PLAYER") == null) {
                textViewFavPlayer.setTypeface(null, Typeface.ITALIC);
                textViewFavPlayer.setText(getResources().getString(R.string.default_fav_player));
            } else {
                textViewFavPlayer.setText(UPB.getString("FAVORITE_PLAYER"));
            }
            if (UPB.getString("PEP_TALK") == null) {
                textViewPepTalk.setTypeface(null, Typeface.ITALIC);
                textViewPepTalk.setText(getResources().getString(R.string.default_pep_talk));
            } else {
                textViewPepTalk.setText(UPB.getString("PEP_TALK"));
            }
            if (UPB.getString("TRASH_TALK") == null) {
                textViewTrashTalk.setTypeface(null, Typeface.ITALIC);
                textViewTrashTalk.setText(getResources().getString(R.string.default_trash_talk));
            } else {
                textViewTrashTalk.setText(UPB.getString("TRASH_TALK"));
            }
        }
    }
}