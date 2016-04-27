package com.peprally.jeremy.peprally.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserNickname;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ProfileEditFragment extends Fragment {

    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient ddbClient;
    private DynamoDBMapper mapper;

    private boolean nicknameChanged = false;
    private boolean nicknameTaken = false;
    private String localNickname;
    private EditText editTextNickname;
    private TextView textViewFirstName;
    private TextView textViewFavTeam;
    private TextView textViewFavPlayer;
    private EditText editTextPepTalk;
    private EditText editTextTrashTalk;

    private static final String TAG = ProfileEditFragment.class.getSimpleName();

    private TextWatcher nicknameTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().isEmpty()) {
                editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
            }
            else if (!s.toString().trim().replace(" ","_").toLowerCase().equals(localNickname.toLowerCase()) &&
                     !s.toString().trim().equals(getResources().getString(R.string.default_nickname))) {
                new CheckUniqueNicknameDBTask().execute(s.toString().trim().replace(" ", "_"));
                nicknameChanged = true;
            }
            else {
                nicknameChanged = false;
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "edit fragment view created");
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity(),                              // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        localNickname = getArguments().getString("NICKNAME");

        InputFilter nicknameFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isSpaceChar(source.charAt(i))) {
                        return "_";
                    }
                    else if (!Character.isLetterOrDigit(source.charAt(i)) && !String.valueOf(source.charAt(i)).equals("_")) {
                        Toast.makeText(getActivity(), R.string.invalid_characters_message, Toast.LENGTH_SHORT).show();
                        return "";
                    }
                }
                return null;
            }
        };

        editTextNickname = (EditText) view.findViewById(R.id.profile_edit_nickname);
        textViewFirstName = (TextView) view.findViewById(R.id.profile_edit_name_age);
        textViewFavTeam = (TextView) view.findViewById(R.id.profile_edit_fav_team);
        textViewFavPlayer = (TextView) view.findViewById(R.id.profile_edit_fav_player);
        editTextPepTalk = (EditText) view.findViewById(R.id.profile_edit_pep_talk);
        editTextTrashTalk = (EditText) view.findViewById(R.id.profile_edit_trash_talk);

        editTextNickname.setHint(R.string.default_nickname);
        textViewFavTeam.setHint(R.string.default_fav_team);
        textViewFavPlayer.setHint(R.string.default_fav_player);
        editTextPepTalk.setHint(R.string.default_pep_talk);
        editTextTrashTalk.setHint(R.string.default_trash_talk);

        editTextNickname.setFilters(new InputFilter[] {nicknameFilter});
        editTextNickname.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        editTextNickname.addTextChangedListener(nicknameTextWatcher);
        editTextNickname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (editTextNickname.getText().toString().isEmpty()) {
                        editTextNickname.setText(localNickname);
                    }
                    if (nicknameChanged && !nicknameTaken) {
                        List<String> nicknames = new ArrayList<>(Arrays.asList(editTextNickname.getText().toString().trim().replace(" ", "_"),
                                                                                localNickname));
                        new PushNewNicknameToDBTask().execute(nicknames);
                        localNickname = editTextNickname.getText().toString().trim().replace(" ", "_");
                    }
                }
            }
        });

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

    public void setupUserProfile(View view, Bundle UPB) {
        if (UPB != null) {
            // TODO: CALCULATE USER AGE FROM FB DATA
            textViewFirstName.setText(UPB.getString("FIRST_NAME")); // + ", " + Integer.toString(23));
            editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            editTextNickname.setText(UPB.getString("NICKNAME"));
            textViewFavTeam.setText(UPB.getString("FAVORITE_TEAM"));
            textViewFavPlayer.setText(UPB.getString("FAVORITE_PLAYER"));
            editTextPepTalk.setText(UPB.getString("PEP_TALK"));
            editTextTrashTalk.setText(UPB.getString("TRASH_TALK"));
        }
    }

    public void updateUserProfileBundleData(View view) {
        EditText editTextNickname = (EditText) view.findViewById(R.id.profile_edit_nickname);
        String nickname = editTextNickname.getText().toString().trim().replace(" ", "_");
        EditText editTextPepTalk = (EditText) view.findViewById(R.id.profile_edit_pep_talk);
        String pepTalk = editTextPepTalk.getText().toString().trim();
        EditText editTextTrashTalk = (EditText) view.findViewById(R.id.profile_edit_trash_talk);
        String trashTalk = editTextTrashTalk.getText().toString().trim();

        if (!nickname.isEmpty() && !nicknameTaken) {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("NICKNAME", nickname);
        }
        if (pepTalk.isEmpty() || pepTalk.equals(getResources().getString(R.string.default_pep_talk))) {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("PEP_TALK", null);
        }
        else {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("PEP_TALK", pepTalk);
        }
        if (trashTalk.isEmpty() || trashTalk.equals(getResources().getString(R.string.default_trash_talk))) {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("TRASH_TALK", null);
        }
        else {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("TRASH_TALK", trashTalk);
        }
    }

    public void setFavTeam(View view, String favoriteTeam) {
        TextView favTeam = (TextView) view.findViewById(R.id.profile_edit_fav_team);
        favTeam.setText(favoriteTeam);
    }

    public String getFavTeam(View view) {
        TextView favTeam = (TextView) view.findViewById(R.id.profile_edit_fav_team);
        return favTeam.getText().toString();
    }

    public void setFavPlayer(View view, String favoritePlayer) {
        TextView favPlayer = (TextView) view.findViewById(R.id.profile_edit_fav_player);
        favPlayer.setText(favoritePlayer);
    }

    private void showNicknameTaken() {
        if (!editTextNickname.getText().toString().trim().isEmpty()) {
            nicknameTaken = true;
            editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_error,0);
        }
    }

    private void showNicknameAvailable() {
        if (!editTextNickname.getText().toString().trim().isEmpty()) {
            nicknameTaken = false;
            editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_check,0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "edit fragment resumed");
        setupUserProfile(getView(), getArguments());

        // Auto pop-up keyboard to show user edit is available
        EditText editText = (EditText) getView().findViewById(R.id.profile_edit_nickname);
        editText.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "edit fragment paused");
        updateUserProfileBundleData(getView());
    }

    /********************************** AsyncTasks **********************************/
    private class CheckUniqueNicknameDBTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            DBUserNickname userNickname = mapper.load(DBUserNickname.class, params[0].toLowerCase());
            if (userNickname == null) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean nicknameTaken) {
            if (nicknameTaken) {
                showNicknameTaken();
            }
            else {
                showNicknameAvailable();
            }
        }
    }

    private class PushNewNicknameToDBTask extends AsyncTask<List<String>, Void, Void> {
        @Override
        protected Void doInBackground(List<String>... params) {
            String newNickname = params[0].get(0).toLowerCase();
            String oldNickname = params[0].get(1).toLowerCase();
            DBUserNickname oldUserNickname = mapper.load(DBUserNickname.class, oldNickname);
            if (oldUserNickname != null) {
                mapper.delete(oldUserNickname);
            }
            HashMap<String, AttributeValue> primaryKey = new HashMap<>();
            primaryKey.put("Nickname", new AttributeValue().withS(newNickname));
            primaryKey.put("CognitoID", new AttributeValue().withS(credentialsProvider.getIdentityId()));
            primaryKey.put("FacebookID", new AttributeValue().withS(getArguments().getString("FACEBOOK_ID")));
            ddbClient.putItem(new PutItemRequest().withTableName("UserNicknames").withItem(primaryKey));
            return null;
        }
    }
}