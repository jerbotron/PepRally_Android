package com.peprally.jeremy.peprally.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;


public class ProfileEditFragment extends Fragment {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private EditText editTextPepTalk, editTextTrashTalk;
    private TextView textViewUsername, textViewFirstName, textViewFavTeam, textViewFavPlayer;

    // General Variables
    private static final String TAG = ProfileEditFragment.class.getSimpleName();
    private UserProfileParcel userProfileParcel;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get copy of userProfileParcel from ProfileActivity
        userProfileParcel = ((ProfileActivity) getActivity()).getUserProfileParcel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "edit fragment view created");
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        textViewUsername = (TextView) view.findViewById(R.id.id_text_view_profile_edit_username);
        textViewFirstName = (TextView) view.findViewById(R.id.id_text_view_profile_edit_name_age);
        textViewFavTeam = (TextView) view.findViewById(R.id.id_text_view_profile_edit_fav_team);
        textViewFavPlayer = (TextView) view.findViewById(R.id.id_text_view_profile_edit_fav_player);
        editTextPepTalk = (EditText) view.findViewById(R.id.id_edit_text_profile_edit_pep_talk);
        editTextTrashTalk = (EditText) view.findViewById(R.id.id_edit_text_profile_edit_trash_talk);

        textViewFavTeam.setHint(R.string.default_fav_team);
        textViewFavPlayer.setHint(R.string.default_fav_player);
        editTextPepTalk.setHint(R.string.default_pep_talk);
        editTextTrashTalk.setHint(R.string.default_trash_talk);

        // Handle editing favorite team/player options
        textViewFavTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProfileActivity) getActivity()).editFavoriteTeam();
            }
        });
        textViewFavPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProfileActivity) getActivity()).editFavoritePlayer();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "edit fragment resumed");
        setupUserProfile();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "edit fragment paused");
        updateUserProfileBundleData();
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private void updateUserProfileBundleData() {
        String pepTalk = editTextPepTalk.getText().toString().trim();
        String trashTalk = editTextTrashTalk.getText().toString().trim();

        if (pepTalk.isEmpty() || pepTalk.equals(getResources().getString(R.string.default_pep_talk))) {
            userProfileParcel.setPepTalk(null);
        }
        else {
            userProfileParcel.setPepTalk(pepTalk);
        }
        if (trashTalk.isEmpty() || trashTalk.equals(getResources().getString(R.string.default_trash_talk))) {
            userProfileParcel.setTrashTalk(null);
        }
        else {
            userProfileParcel.setTrashTalk(trashTalk);
        }
    }

    /***********************************************************************************************
     ****************************************** UI METHODS *****************************************
     **********************************************************************************************/
    private void setupUserProfile() {
        // TODO: CALCULATE USER AGE FROM FB DATA
        textViewFirstName.setText(userProfileParcel.getFirstname()); // + ", " + Integer.toString(23));
        textViewUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        textViewUsername.setText("@"+userProfileParcel.getProfileUsername());
        textViewFavTeam.setText(userProfileParcel.getFavoriteTeam());
        textViewFavPlayer.setText(userProfileParcel.getFavoritePlayer());
        editTextPepTalk.setText(userProfileParcel.getPepTalk());
        editTextTrashTalk.setText(userProfileParcel.getTrashTalk());
    }

    public void setFavTeam(String favoriteTeam) {
        textViewFavTeam.setText(favoriteTeam);
    }

    public String getFavTeam() {
        return textViewFavTeam.getText().toString();
    }

    public void setFavPlayer(String favoritePlayer) {
        textViewFavPlayer.setText(favoritePlayer);
    }
}