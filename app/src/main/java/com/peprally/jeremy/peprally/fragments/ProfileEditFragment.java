package com.peprally.jeremy.peprally.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;


public class ProfileEditFragment extends Fragment {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private EditText editTextPepTalk, editTextTrashTalk;
    private TextView textViewUsername, textViewFirstName, textViewFavTeam, textViewFavPlayer;

    // General Variables
    private static final String TAG = ProfileEditFragment.class.getSimpleName();
    private boolean isFragmentReady;
    private UserProfileParcel userProfileParcel;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                ((ProfileActivity) getActivity()).launchFavoriteTeamActivity();
            }
        });
        textViewFavPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProfileActivity) getActivity()).launchFavoritePlayerActivity();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Get latest copy of userProfileParcel from ProfileActivity
        userProfileParcel = ((ProfileActivity) getActivity()).getUserProfileParcel();
        setupUserProfile();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateUserProfileBundleData();
    }

    /***********************************************************************************************
     *********************************** GENERAL_METHODS ********************************
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
        String usernameText = "@"+userProfileParcel.getProfileUsername();
        textViewUsername.setText(usernameText);
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
        if (favoritePlayer == null)
            textViewFavPlayer.setText("");
        else
            textViewFavPlayer.setText(favoritePlayer);
    }
}