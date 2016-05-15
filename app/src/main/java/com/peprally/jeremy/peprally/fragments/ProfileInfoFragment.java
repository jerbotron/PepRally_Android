package com.peprally.jeremy.peprally.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

public class ProfileInfoFragment extends Fragment {

    private boolean profileLoaded = false;

    private static final String TAG = ProfileInfoFragment.class.getSimpleName();

    Map<String, String>  baseballPositions = new HashMap<String, String>();
    Map<String, String>  basketballPositions = new HashMap<String, String>();
    Map<String, String>  footballPositions = new HashMap<String, String>();

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
        setupUserProfile(getView(), getArguments());
    }

    public void setupUserProfile(View view, Bundle UPB) {
        if (UPB != null) {
            final LinearLayout parent = (LinearLayout) view.findViewById(R.id.profile_view_container);
            final LinearLayout ll_container = (LinearLayout) view.findViewById(R.id.profile_view_container);
            final TextView textViewFirstName = (TextView) view.findViewById(R.id.profile_view_name_age);
            final TextView textViewNickname = (TextView) view.findViewById(R.id.profile_view_nickname);
            final TextView textViewFavTeam = (TextView) view.findViewById(R.id.profile_view_fav_team);
            final TextView textViewFavPlayer = (TextView) view.findViewById(R.id.profile_view_fav_player);
            final TextView textViewPepTalk = (TextView) view.findViewById(R.id.profile_view_pep_talk);
            final TextView textViewTrashTalk = (TextView) view.findViewById(R.id.profile_view_trash_talk);

            textViewFirstName.setText("");    // set to empty so I can check later if populated under varsity profile

            if (UPB.getBoolean("IS_VARSITY_PLAYER") && !profileLoaded) {
                String name = UPB.getString("FIRST_NAME") + " "
                                + UPB.getString("LAST_NAME") + " #"
                                + String.valueOf(UPB.getInt("NUMBER"));
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
                if (UPB.getString("POSITION") != null) {
                    String text = "Position: <b>";
                    String pos = UPB.getString("POSITION");
                    switch (UPB.getString("TEAM")) {
                        case "Baseball":
                            if (pos.indexOf("/") != -1) {
                                text = text + baseballPositions.get(pos.split("/")[0]) + "/"
                                            + baseballPositions.get(pos.split("/")[1]);
                            }
                            else {
                                text += baseballPositions.get(pos).toString();
                            }
                            break;
                        case "Basketball":
                            if (pos.indexOf("/") != -1) {
                                text = text + basketballPositions.get(pos.split("/")[0]) + "/"
                                            + basketballPositions.get(pos.split("/")[1]);
                            }
                            else {
                                text += basketballPositions.get(pos).toString();
                            }
                            break;
                        case "Football":
                            if (pos.indexOf("/") != -1) {
                                text = text + footballPositions.get(pos.split("/")[0]) + "/"
                                            + footballPositions.get(pos.split("/")[1]);
                            }
                            else {
                                text += footballPositions.get(pos).toString();
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

                if (UPB.getString("HEIGHT") != null) {
                    TextView tv_height = new TextView(getActivity());
                    tv_height.setText(Html.fromHtml("Height: <b>" + UPB.getString("HEIGHT") + "</b>"));
                    tv_height.setLayoutParams(tvparams);
                    playerInfoLayout.addView(tv_height);
                }

                if (UPB.getString("WEIGHT") != null) {
                    TextView tv_weight = new TextView(getActivity());
                    tv_weight.setText(Html.fromHtml("Weight: <b>" + UPB.getString("WEIGHT") + "</b>"));
                    tv_weight.setLayoutParams(tvparams);
                    playerInfoLayout.addView(tv_weight);
                }

                if (UPB.getString("YEAR") != null) {
                    TextView tv_year = new TextView(getActivity());
                    tv_year.setText(Html.fromHtml("Year: <b>" + UPB.getString("YEAR") + "</b>"));
                    tv_year.setLayoutParams(tvparams);
                    playerInfoLayout.addView(tv_year);
                }

                if (UPB.getString("HOMETOWN") != null) {
                    String[] sa = UPB.getString("HOMETOWN").split("/");
                    TextView tv_hometown = new TextView(getActivity());
                    tv_hometown.setText(Html.fromHtml("Hometown: <b>" + sa[0].substring(0, sa[0].length() - 1) + "</b>"));
                    tv_hometown.setLayoutParams(tvparams);
                    playerInfoLayout.addView(tv_hometown);

                    TextView tv_highschool = new TextView(getActivity());
                    tv_highschool.setText(Html.fromHtml("High School: <b>" + sa[1].substring(1) + "</b>"));
                    tv_highschool.setLayoutParams(tvparams);
                    playerInfoLayout.addView(tv_highschool);
                }
                if (!UPB.getBoolean("HAS_USER_PROFILE")) {
                    TextView tv_no_profile = new TextView(getActivity());
                    LinearLayout.LayoutParams msg_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    msg_params.setMargins(0,
                            (int) getResources().getDimension(R.dimen.activity_vertical_margin),
                            0,
                            0);
                    tv_no_profile.setText(UPB.getString("FIRST_NAME") +
                            " has not made a Pep Rally profile yet. Tell your friends about Pep Rally!");
                    tv_no_profile.setTypeface(null, Typeface.ITALIC);
                    tv_no_profile.setLayoutParams(msg_params);
                    tv_no_profile.setGravity(Gravity.CENTER_HORIZONTAL);
                    playerInfoLayout.addView(tv_no_profile);
                }
                parent.addView(playerInfoLayout, 2);
                profileLoaded = true;
            }

            if (textViewFirstName.getText().toString().isEmpty()) {
                textViewFirstName.setText(UPB.getString("FIRST_NAME"));// + ", " + Integer.toString(23));
            }
            if (UPB.getString("NICKNAME") == null) {
                parent.removeView(textViewNickname);
            }
            else {
                textViewNickname.setText(UPB.getString("NICKNAME"));
            }

            if (UPB.getString("FAVORITE_TEAM") == null) {
                ll_container.removeView(view.findViewById(R.id.profile_layout_fav_team));
            } else {
                textViewFavTeam.setText(UPB.getString("FAVORITE_TEAM"));
            }
            if (UPB.getString("FAVORITE_PLAYER") == null) {
                ll_container.removeView(view.findViewById(R.id.profile_layout_fav_player));
            } else {
                textViewFavPlayer.setText(UPB.getString("FAVORITE_PLAYER"));
            }
            if (UPB.getString("PEP_TALK") == null) {
                ll_container.removeView(view.findViewById(R.id.profile_layout_pep_talk));
            } else {
                textViewPepTalk.setText(UPB.getString("PEP_TALK"));
            }
            if (UPB.getString("TRASH_TALK") == null) {
                ll_container.removeView(view.findViewById(R.id.profile_layout_trash_talk));
            } else {
                textViewTrashTalk.setText(UPB.getString("TRASH_TALK"));
            }

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