package com.peprally.jeremy.peprally.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.HashMap;
import java.util.Map;

public class ProfileInfoFragment extends Fragment {

    private UserProfileParcel userProfileParcel;

    private boolean profileLoaded = false;

    private static final String TAG = ProfileInfoFragment.class.getSimpleName();

    Map<String, String>  baseballPositions = new HashMap<String, String>();
    Map<String, String>  basketballPositions = new HashMap<String, String>();
    Map<String, String>  footballPositions = new HashMap<String, String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get copy of userProfileParcel from ProfileActivity
        userProfileParcel = ((ProfileActivity) getActivity()).getUserProfileParcel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "profile view fragment created");
        View view = inflater.inflate(R.layout.fragment_profile_info, container, false);
        return view;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "profile info fragment resumed");
        super.onResume();
        refresh();
    }

    public void refresh() {
        initializePositionMaps();
        setupUserProfile(getView());
    }

    public void setupUserProfile(View view) {
        final LinearLayout parent_container = (LinearLayout) view.findViewById(R.id.profile_view_container);
        final TextView textViewFirstName = (TextView) view.findViewById(R.id.profile_view_name_age);
        final TextView textViewNickname = (TextView) view.findViewById(R.id.profile_view_nickname);
        final TextView textViewFavTeam = (TextView) view.findViewById(R.id.profile_view_fav_team);
        final TextView textViewFavPlayer = (TextView) view.findViewById(R.id.profile_view_fav_player);
        final TextView textViewPepTalk = (TextView) view.findViewById(R.id.profile_view_pep_talk);
        final TextView textViewTrashTalk = (TextView) view.findViewById(R.id.profile_view_trash_talk);

        textViewFirstName.setText("");    // set to empty so I can check later if populated under varsity profile

        if (userProfileParcel.getIsVarsityPlayer() && !profileLoaded) {
            String name = userProfileParcel.getFirstname() + " "
                            + userProfileParcel.getLastname() + " #"
                            + userProfileParcel.getNumber();
            textViewFirstName.setText(name);

            LinearLayout playerInfoLayout = new LinearLayout(getActivity());
            playerInfoLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            llparams.setMargins((int) getResources().getDimension(R.dimen.activity_horizontal_margin),
                    0,
                    (int) getResources().getDimension(R.dimen.activity_horizontal_margin),
                    (int) getResources().getDimension(R.dimen.activity_horizontal_margin));
            playerInfoLayout.setLayoutParams(llparams);

            final LinearLayout.LayoutParams tvparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvparams.setMargins(0, 0, 0, 4);
            if (userProfileParcel.getPosition() != null) {
                String text = "Position: <b>";
                String pos = userProfileParcel.getPosition();
                switch (userProfileParcel.getTeam()) {
                    case "Baseball":
                        if (pos.contains("/")) {
                            text = text + baseballPositions.get(pos.split("/")[0]) + "/"
                                        + baseballPositions.get(pos.split("/")[1]);
                        }
                        else {
                            text += baseballPositions.get(pos);
                        }
                        break;
                    case "Basketball":
                        if (pos.contains("/")) {
                            text = text + basketballPositions.get(pos.split("/")[0]) + "/"
                                        + basketballPositions.get(pos.split("/")[1]);
                        }
                        else {
                            text += basketballPositions.get(pos);
                        }
                        break;
                    case "Football":
                        if (pos.contains("/")) {
                            text = text + footballPositions.get(pos.split("/")[0]) + "/"
                                        + footballPositions.get(pos.split("/")[1]);
                        }
                        else {
                            text += footballPositions.get(pos);
                        }
                        break;
                    default:
                        text += pos;
                        break;
                }
                TextView tv_position = new TextView(getActivity());
                tv_position.setText(Html.fromHtml(text + "</b"));
                tv_position.setLayoutParams(tvparams);
                playerInfoLayout.addView(tv_position);
            }

            if (userProfileParcel.getHeight() != null) {
                TextView tv_height = new TextView(getActivity());
                tv_height.setText(Html.fromHtml("Height: <b>" + userProfileParcel.getHeight() + "</b>"));
                tv_height.setLayoutParams(tvparams);
                playerInfoLayout.addView(tv_height);
            }

            if (userProfileParcel.getWeight() != null) {
                TextView tv_weight = new TextView(getActivity());
                tv_weight.setText(Html.fromHtml("Weight: <b>" + userProfileParcel.getWeight() + "</b>"));
                tv_weight.setLayoutParams(tvparams);
                playerInfoLayout.addView(tv_weight);
            }

            if (userProfileParcel.getYear() != null) {
                TextView tv_year = new TextView(getActivity());
                tv_year.setText(Html.fromHtml("Year: <b>" + userProfileParcel.getYear() + "</b>"));
                tv_year.setLayoutParams(tvparams);
                playerInfoLayout.addView(tv_year);
            }

            if (userProfileParcel.getHometown() != null) {
                String[] sa = userProfileParcel.getHometown().split("/");
                TextView tv_hometown = new TextView(getActivity());
                tv_hometown.setText(Html.fromHtml("Hometown: <b>" + sa[0].substring(0, sa[0].length() - 1) + "</b>"));
                tv_hometown.setLayoutParams(tvparams);
                playerInfoLayout.addView(tv_hometown);

                TextView tv_highschool = new TextView(getActivity());
                tv_highschool.setText(Html.fromHtml("High School: <b>" + sa[1].substring(1) + "</b>"));
                tv_highschool.setLayoutParams(tvparams);
                playerInfoLayout.addView(tv_highschool);
            }
            if (!userProfileParcel.getHasUserProfile()) {
                TextView tv_no_profile = new TextView(getActivity());
                LinearLayout.LayoutParams msg_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                msg_params.setMargins(0,
                        (int) getResources().getDimension(R.dimen.activity_vertical_margin),
                        0,
                        0);
                tv_no_profile.setText(userProfileParcel.getFirstname() +
                        " has not made a Pep Rally profile yet. Tell your friends about Pep Rally!");
                tv_no_profile.setTypeface(null, Typeface.ITALIC);
                tv_no_profile.setLayoutParams(msg_params);
                tv_no_profile.setGravity(Gravity.CENTER_HORIZONTAL);
                playerInfoLayout.addView(tv_no_profile);
            }
            parent_container.addView(playerInfoLayout, 2);
            profileLoaded = true;
        }

        if (textViewFirstName.getText().toString().isEmpty()) {
            textViewFirstName.setText(userProfileParcel.getFirstname());// + ", " + Integer.toString(23));
        }
        if (userProfileParcel.getNickname() == null) {
            parent_container.removeView(textViewNickname);
        }
        else {
            textViewNickname.setText(userProfileParcel.getNickname());
        }

        if (userProfileParcel.getFavoriteTeam() == null) {
            parent_container.removeView(view.findViewById(R.id.profile_layout_fav_team));
        } else {
            textViewFavTeam.setText(userProfileParcel.getFavoriteTeam());
        }
        if (userProfileParcel.getFavoritePlayer() == null) {
            parent_container.removeView(view.findViewById(R.id.profile_layout_fav_player));
        } else {
            textViewFavPlayer.setText(userProfileParcel.getFavoritePlayer());
        }
        if (userProfileParcel.getPepTalk() == null) {
            parent_container.removeView(view.findViewById(R.id.profile_layout_pep_talk));
        } else {
            textViewPepTalk.setText(userProfileParcel.getPepTalk());
        }
        if (userProfileParcel.getTrashTalk() == null) {
            parent_container.removeView(view.findViewById(R.id.profile_layout_trash_talk));
        } else {
            textViewTrashTalk.setText(userProfileParcel.getTrashTalk());
        }

    }

    void initializePositionMaps() {
        // Baseball
        baseballPositions.put("C", "Catcher");
        baseballPositions.put("INF", "Infield");
        baseballPositions.put("OF", "Outfield");
        baseballPositions.put("RHP", "Right Handed Pitcher");
        baseballPositions.put("LHP", "Left Handed Pitcher");
        // Basketball
        basketballPositions.put("G", "Guard");
        basketballPositions.put("F", "Forward");
        basketballPositions.put("C", "Center");
        // Football
        footballPositions.put("QB", "Quarterback");
        footballPositions.put("RB", "Runningback");
        footballPositions.put("WR", "Wide Receiver");
        footballPositions.put("TE", "Tight End");
        footballPositions.put("OL", "Offensive Lineman");
        footballPositions.put("OG", "Offensive Guard");
        footballPositions.put("OT", "Offensive Tackle");
        footballPositions.put("CB", "Cornerback");
        footballPositions.put("DB", "Defensive Back");
        footballPositions.put("S", "Safety");
        footballPositions.put("LB", "Linebacker");
        footballPositions.put("DL", "Defensive Lineman");
        footballPositions.put("DT", "Defensive Tackle");
        footballPositions.put("DE", "Defensive End");
        footballPositions.put("DS", "Deep Safety");
        footballPositions.put("P", "Punter");
        footballPositions.put("PK", "Punter/Kicker");
    }
}