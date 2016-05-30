package com.peprally.jeremy.peprally.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.utils.AWSCredentialProvider;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;


public class ProfileEditFragment extends Fragment {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient ddbClient;
    private DynamoDBMapper mapper;

    // UI Variables
    private EditText editTextPepTalk, editTextTrashTalk;
    private TextView textViewNickname, textViewFirstName, textViewFavTeam, textViewFavPlayer;

    // General Variables
    private static final String TAG = ProfileEditFragment.class.getSimpleName();
    private UserProfileParcel userProfileParcel;
//    private String localNickname;
//    private boolean nicknameChanged = false;
//    private boolean nicknameTaken = false;

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

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity(),                              // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,     // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION        // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

//        localNickname = userProfileParcel.getNickname();

        final InputFilter nicknameFilter = new InputFilter() {
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

        textViewNickname = (TextView) view.findViewById(R.id.id_text_view_profile_edit_nickname);
        textViewFirstName = (TextView) view.findViewById(R.id.id_text_view_profile_edit_name_age);
        textViewFavTeam = (TextView) view.findViewById(R.id.id_text_view_profile_edit_fav_team);
        textViewFavPlayer = (TextView) view.findViewById(R.id.id_text_view_profile_edit_fav_player);
        editTextPepTalk = (EditText) view.findViewById(R.id.id_edit_text_profile_edit_pep_talk);
        editTextTrashTalk = (EditText) view.findViewById(R.id.id_edit_text_profile_edit_trash_talk);

        textViewFavTeam.setHint(R.string.default_fav_team);
        textViewFavPlayer.setHint(R.string.default_fav_player);
        editTextPepTalk.setHint(R.string.default_pep_talk);
        editTextTrashTalk.setHint(R.string.default_trash_talk);

        // TODO: Might want to implement User Nickname change feature
//        editTextNickname.setFilters(new InputFilter[] {nicknameFilter});
//        editTextNickname.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
//        editTextNickname.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//                if (s.toString().trim().isEmpty()) {
//                    editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
//                }
//                else if (!s.toString().trim().replace(" ","_").toLowerCase().equals(localNickname.toLowerCase()) &&
//                        !s.toString().trim().equals(getResources().getString(R.string.default_nickname))) {
//                    new CheckUniqueNicknameDBTask().execute(s.toString().trim().replace(" ", "_"));
//                    nicknameChanged = true;
//                }
//                else {
//                    nicknameChanged = false;
//                }
//            }
//
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//        });
//        editTextNickname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    if (editTextNickname.getText().toString().isEmpty()) {
//                        editTextNickname.setText(localNickname);
//                    }
//                    if (nicknameChanged && !nicknameTaken) {
//                        List<String> nicknames = new ArrayList<>(Arrays.asList(editTextNickname.getText().toString().trim().replace(" ", "_"),
//                                                                                localNickname));
//                        new PushNewNicknameToDBTask().execute(nicknames);
//                        localNickname = editTextNickname.getText().toString().trim().replace(" ", "_");
//                    }
//                }
//            }
//        });

        // Handle editing favorite team/player options
        textViewFavTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProfileActivity) getActivity()).editFavoriteTeam();
            }
        });
        textViewFavTeam.setOnClickListener(new View.OnClickListener() {
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
    public void updateUserProfileBundleData() {
//        String nickname = editTextNickname.getText().toString().trim().replace(" ", "_");
        String pepTalk = editTextPepTalk.getText().toString().trim();
        String trashTalk = editTextTrashTalk.getText().toString().trim();

//        if (!nickname.isEmpty() && !nicknameTaken) {
//            userProfileParcel.setNickname(nickname);
//        }
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
    public void setupUserProfile() {
        // TODO: CALCULATE USER AGE FROM FB DATA
        textViewFirstName.setText(userProfileParcel.getFirstname()); // + ", " + Integer.toString(23));
        textViewNickname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        textViewNickname.setText(userProfileParcel.getNickname());
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

//    private void showNicknameTaken() {
//        if (!editTextNickname.getText().toString().trim().isEmpty()) {
//            nicknameTaken = true;
//            editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_error,0);
//        }
//    }
//
//    private void showNicknameAvailable() {
//        if (!editTextNickname.getText().toString().trim().isEmpty()) {
//            nicknameTaken = false;
//            editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_check,0);
//        }
//    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
//    private class CheckUniqueNicknameDBTask extends AsyncTask<String, Void, Boolean> {
//        @Override
//        protected Boolean doInBackground(String... params) {
//            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
//            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
//            DBUserNickname userNickname = mapper.load(DBUserNickname.class, params[0].toLowerCase());
//            if (userNickname == null) {
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean nicknameTaken) {
//            if (nicknameTaken) {
//                showNicknameTaken();
//            }
//            else {
//                showNicknameAvailable();
//            }
//        }
//    }
//
//    private class PushNewNicknameToDBTask extends AsyncTask<List<String>, Void, Void> {
//        @Override
//        protected Void doInBackground(List<String>... params) {
//            String newNickname = params[0].get(0).toLowerCase();
//            String oldNickname = params[0].get(1).toLowerCase();
//            DBUserNickname oldUserNickname = mapper.load(DBUserNickname.class, oldNickname);
//            if (oldUserNickname != null) {
//                mapper.delete(oldUserNickname);
//            }
//            HashMap<String, AttributeValue> primaryKey = new HashMap<>();
//            primaryKey.put("Nickname", new AttributeValue().withS(newNickname));
//            primaryKey.put("CognitoID", new AttributeValue().withS(credentialsProvider.getIdentityId()));
//            primaryKey.put("FacebookID", new AttributeValue().withS(userProfileParcel.getFacebookID()));
//            ddbClient.putItem(new PutItemRequest().withTableName("UserNicknames").withItem(primaryKey));
//            return null;
//        }
//    }
}