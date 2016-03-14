package com.peprally.jeremy.peprally;

import android.graphics.Typeface;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "edit fragment view created");
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

//        TextView textViewFirstName = (TextView) view.findViewById(R.id.profile_edit_name_age);
//        textViewFirstName.setText(userFirstName + ", " + Integer.toString(userAge));

        // Handle editing favorite team/player options
        TextView favTeam = (TextView) view.findViewById(R.id.profile_edit_fav_team);
        favTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProfileActivity) getActivity()).editFavoriteTeam();
            }
        });
        TextView favPlayer = (TextView) view.findViewById(R.id.profile_edit_fav_player);
        favPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProfileActivity) getActivity()).editFavoritePlayer();
            }
        });

        return view;
    }

    public void updateFavTeam(View view, String newFavTeam) {
        Log.d(TAG, "update edit fragment");
        if (view == null) {
            Log.d(TAG, "view is null");
            return;
        }
        Log.d(TAG, newFavTeam);
        TextView favTeam = (TextView) view.findViewById(R.id.profile_edit_fav_team);
        favTeam.setText(newFavTeam);
    }

    private void updateUserProfileBundleData(View view) {
        EditText editTextMotto = (EditText) view.findViewById(R.id.profile_edit_motto);
        String motto = editTextMotto.getText().toString();
        EditText editTextPepTalk = (EditText) view.findViewById(R.id.profile_edit_pep_talk);
        String pepTalk = editTextPepTalk.getText().toString();
        EditText editTextTrashTalk = (EditText) view.findViewById(R.id.profile_edit_trash_talk);
        String trashTalk = editTextTrashTalk.getText().toString();

        if (!motto.isEmpty() && !motto.equals(getResources().getString(R.string.default_motto))) {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("MOTTO", editTextMotto.getText().toString());
        }
        if (!pepTalk.isEmpty() && !pepTalk.equals(getResources().getString(R.string.default_pep_talk))) {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("PEP_TALK", editTextPepTalk.getText().toString());
        }
        if (!trashTalk.isEmpty() && !trashTalk.equals(getResources().getString(R.string.default_trash_talk))) {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("TRASH_TALK", editTextTrashTalk.getText().toString());
        }
    }

    public void setupUserProfile(View view, Bundle UPB) {
        if (UPB != null) {
            TextView textViewFirstName = (TextView) view.findViewById(R.id.profile_edit_name_age);
            EditText editTextMotto = (EditText) view.findViewById(R.id.profile_edit_motto);
            TextView textViewFavTeam = (TextView) view.findViewById(R.id.profile_edit_fav_team);
            TextView textViewFavPlayer = (TextView) view.findViewById(R.id.profile_edit_fav_player);
            EditText editTextPepTalk = (EditText) view.findViewById(R.id.profile_edit_pep_talk);
            EditText editTextTrashTalk = (EditText) view.findViewById(R.id.profile_edit_trash_talk);

            textViewFirstName.setText(UPB.getString("FIRST_NAME") + ", " + Integer.toString(23));
            editTextMotto.setText(UPB.getString("MOTTO"));
            textViewFavTeam.setText(UPB.getString("FAVORITE_TEAM"));
            textViewFavPlayer.setText(UPB.getString("FAVORITE_PLAYER"));
            editTextPepTalk.setText(UPB.getString("PEP_TALK"));
            editTextTrashTalk.setText(UPB.getString("TRASH_TALK"));
//            if (UPB.getString("MOTTO") == null) {
//                editTextMotto.setText(getResources().getString(R.string.default_motto));
//            } else {
//                editTextMotto.setText(UPB.getString("MOTTO"));
//            }
//            if (UPB.getString("FAVORITE_TEAM") == null) {
//                textViewFavTeam.setText(getResources().getString(R.string.default_fav_team));
//            } else {
//                textViewFavTeam.setText(UPB.getString("FAVORITE_TEAM"));
//            }
//            if (UPB.getString("FAVORITE_PLAYER") == null) {
//                textViewFavPlayer.setText(getResources().getString(R.string.default_fav_player));
//            } else {
//                textViewFavPlayer.setText(UPB.getString("FAVORITE_PLAYER"));
//            }
//            if (UPB.getString("PEP_TALK") == null) {
//                editTextPepTalk.setText(getResources().getString(R.string.default_pep_talk));
//            } else {
//                editTextPepTalk.setText(UPB.getString("PEP_TALK"));
//            }
//            if (UPB.getString("TRASH_TALK") == null) {
//                editTextTrashTalk.setText(getResources().getString(R.string.default_trash_talk));
//            } else {
//                editTextTrashTalk.setText(UPB.getString("TRASH_TALK"));
//            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "edit fragment resumed");
        setupUserProfile(getView(), getArguments());

        // Auto pop-up keyboard to show user edit is available
        EditText editText = (EditText) getView().findViewById(R.id.profile_edit_motto);
        editText.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "edit fragment paused");
    }

    @Override
    public void onStop() {
        super.onStop();
        updateUserProfileBundleData(getView());
    }
}