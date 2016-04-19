package com.peprally.jeremy.peprally;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

import java.util.ArrayList;
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

    private static final String TAG = ProfileEditFragment.class.getSimpleName();

    private TextWatcher nicknameTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
            if (!s.toString().equals(localNickname)) {
                Log.d(TAG, s.toString());
                Log.d(TAG, localNickname);
                new CheckUniqueNicknameDBTask().execute(s.toString());
                nicknameChanged = true;
            }
            else {
                nicknameChanged = false;
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
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

        editTextNickname = (EditText) view.findViewById(R.id.profile_edit_nickname);
        editTextNickname.addTextChangedListener(nicknameTextWatcher);
        editTextNickname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (nicknameChanged && !nicknameTaken) {
                        new PushNewNicknameToDBTask().execute(editTextNickname.getText().toString().trim());
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
            TextView textViewFirstName = (TextView) view.findViewById(R.id.profile_edit_name_age);
            TextView textViewFavTeam = (TextView) view.findViewById(R.id.profile_edit_fav_team);
            TextView textViewFavPlayer = (TextView) view.findViewById(R.id.profile_edit_fav_player);
            EditText editTextPepTalk = (EditText) view.findViewById(R.id.profile_edit_pep_talk);
            EditText editTextTrashTalk = (EditText) view.findViewById(R.id.profile_edit_trash_talk);

            textViewFirstName.setText(UPB.getString("FIRST_NAME") + ", " + Integer.toString(23));
            editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            editTextNickname.setText(UPB.getString("NICKNAME"));
            if (UPB.getString("NICKNAME") == null) {
                editTextNickname.setText(Html.fromHtml("<i>"
                        + getResources().getString(R.string.default_nickname)
                        + "</i>"));
            }
            else {
                editTextNickname.setText(UPB.getString("NICKNAME"));
            }

            if (UPB.getString("FAVORITE_TEAM") == null) {
                textViewFavTeam.setText(Html.fromHtml("<i>"
                        + getResources().getString(R.string.default_fav_team)
                        + "</i>"));
            }
            else {
                textViewFavTeam.setText(UPB.getString("FAVORITE_TEAM"));
            }

            if (UPB.getString("FAVORITE_PLAYER") == null) {
                textViewFavPlayer.setText(Html.fromHtml("<i>"
                        + getResources().getString(R.string.default_fav_player)
                        + "</i>"));
            }
            else {
                textViewFavPlayer.setText(UPB.getString("FAVORITE_PLAYER"));
            }

            if (UPB.getString("PEP_TALK") == null) {
                editTextPepTalk.setText(Html.fromHtml("<i>"
                        + getResources().getString(R.string.default_pep_talk)
                        + "</i>"));
            }
            else {
                editTextPepTalk.setText(UPB.getString("PEP_TALK"));
            }

            if (UPB.getString("TRASH_TALK") == null) {
                editTextTrashTalk.setText(Html.fromHtml("<i>"
                        + getResources().getString(R.string.default_trash_talk)
                        + "</i>"));
            }
            else {
                editTextTrashTalk.setText(UPB.getString("TRASH_TALK"));
            }
        }
    }

    private void updateUserProfileBundleData(View view) {
        EditText editTextNickname = (EditText) view.findViewById(R.id.profile_edit_nickname);
        String nickname = editTextNickname.getText().toString();
        EditText editTextPepTalk = (EditText) view.findViewById(R.id.profile_edit_pep_talk);
        String pepTalk = editTextPepTalk.getText().toString();
        EditText editTextTrashTalk = (EditText) view.findViewById(R.id.profile_edit_trash_talk);
        String trashTalk = editTextTrashTalk.getText().toString();

        if (!nickname.isEmpty() && !nicknameTaken) {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("NICKNAME", localNickname);
        }
        if (pepTalk.isEmpty() || pepTalk.equals(getResources().getString(R.string.default_pep_talk))) {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("PEP_TALK", null);
        }
        else {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("PEP_TALK", editTextPepTalk.getText().toString().trim());
        }
        if (trashTalk.isEmpty() || trashTalk.equals(getResources().getString(R.string.default_trash_talk))) {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("TRASH_TALK", null);
        }
        else {
            ((ProfileActivity) getActivity()).updateUserProfileBundleString("TRASH_TALK", editTextTrashTalk.getText().toString().trim());
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
        nicknameTaken = true;
        editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_error,0);
    }

    private void showNicknameAvailable() {
        nicknameTaken = false;
        editTextNickname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_check,0);
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
    }

    @Override
    public void onStop() {
        super.onStop();
        updateUserProfileBundleData(getView());
    }

    /********************************** AsyncTasks **********************************/

    private class CheckUniqueNicknameDBTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            DBUserNickname userNickname = mapper.load(DBUserNickname.class, params[0]);
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

    private class PushNewNicknameToDBTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String nickname = params[0];
            DBUserNickname oldUserNickname = mapper.load(DBUserNickname.class, localNickname);
            if (oldUserNickname != null) {
                mapper.delete(oldUserNickname);
            }
            HashMap<String, AttributeValue> primaryKey = new HashMap<>();
            primaryKey.put("Nickname", new AttributeValue().withS(nickname));
            primaryKey.put("CognitoID", new AttributeValue().withS(credentialsProvider.getIdentityId()));
            primaryKey.put("FacebookID", new AttributeValue().withS(getArguments().getString("FACEBOOK_ID")));
            ddbClient.putItem(new PutItemRequest().withTableName("UserNicknames").withItem(primaryKey));
            // Update new localNickname
            localNickname = params[0];
            return null;
        }
    }
}