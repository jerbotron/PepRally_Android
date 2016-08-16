package com.peprally.jeremy.peprally.activities;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

public class SettingsActivity extends AppCompatPreferenceActivity {

    // Network Variables
    private static DynamoDBHelper dynamoDBHelper;

    // General Variables
    private static String TAG = SettingsActivity.class.getSimpleName();
    private UserProfileParcel userProfileParcel;

    // Fragments
    private static MainPreferencesFragment mainPreferencesFragment;
    private static NotificationPreferenceFragment notificationPreferenceFragment;

    enum PreferenceFragmentEnum {
        MAIN,
        NOTIFICATION,
        FAQ,
        PRIVACY_POLICY
    }

    private static PreferenceFragmentEnum curFragmentEnum;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener preferenceOnChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("pref_key_my_account_email")) {
                    if (Helpers.isValidEmail(stringValue)) {
                        dynamoDBHelper.updateUserEmailPreferences(userProfileParcel.getCurUsername(), stringValue);
                        preference.setSummary(stringValue);
                    } else if (Helpers.isValidEmail(userProfileParcel.getEmail())) {
                        preference.setSummary(userProfileParcel.getEmail());
                        return false;
                    }
                    else {
                        preference.setSummary(getResources().getString(R.string.pref_my_account_email_summary));
                        if (!stringValue.equals(getResources().getString(R.string.pref_my_account_email_summary)))
                            makeToastNotification(getResources().getString(R.string.pref_my_account_email_invalid_msg));
                        return false;
                    }
                }
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value.
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(preferenceOnChangeListener);

        // Trigger the listener immediately with the preference's
        // current value.
        preferenceOnChangeListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        // initialize member variables
        userProfileParcel = getIntent().getParcelableExtra("USER_PROFILE_PARCEL");
        dynamoDBHelper = new DynamoDBHelper(this);

        mainPreferencesFragment = new MainPreferencesFragment();
        notificationPreferenceFragment = new NotificationPreferenceFragment();

        curFragmentEnum = PreferenceFragmentEnum.MAIN;
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mainPreferencesFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (curFragmentEnum == PreferenceFragmentEnum.MAIN) {
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        } else {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.left_in, R.animator.right_out)
                    .replace(android.R.id.content, mainPreferencesFragment)
                    .commit();
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Set action bar title
            actionBar.setTitle("Settings");
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || MainPreferencesFragment.class.getName().equals(fragmentName)
                || PrivacyPolicyPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    public UserProfileParcel getUserProfileParcel() {
        return userProfileParcel;
    }

    public void makeToastNotification(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    // just goes back to home activity which then calls the actual delete profile function
    public void deleteUserAccount() {
        Intent intent = new Intent();
        intent.putExtra("DELETE_PROFILE", true);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Main Preferences Fragment
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MainPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);
            setHasOptionsMenu(true);

            UserProfileParcel userProfileParcel = ((SettingsActivity) getActivity()).getUserProfileParcel();

            // My Account Preferences
            Preference namePref = findPreference("pref_key_my_account_name");
            Preference usernamePref = findPreference("pref_key_my_account_username");
            Preference dateJoinedPref = findPreference("pref_key_my_account_date_joined");
            EditTextPreference emailPref = (EditTextPreference) findPreference("pref_key_my_account_email");
            Preference notificationPref = findPreference("pref_key_my_account_notifications");

            namePref.setSummary(userProfileParcel.getFirstname());
            usernamePref.setSummary(userProfileParcel.getCurUsername());
            dateJoinedPref.setSummary(userProfileParcel.getDateJoined().split("\\s+")[0]);
            if (userProfileParcel.getEmail() != null && !userProfileParcel.getEmail().isEmpty())
                emailPref.setSummary(userProfileParcel.getEmail());

            // Bind the preference summary texts to their values
            ((SettingsActivity) getActivity()).bindPreferenceSummaryToValue(emailPref);

            // notification preferences onclick handler
            notificationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.animator.right_in, R.animator.left_out)
                            .replace(android.R.id.content, notificationPreferenceFragment)
                            .commit();
                    return true;
                }
            });

            // Support Preferences

            // Support Us Preferences
            Preference facebookPref = findPreference("pref_key_show_us_love_facebook");
            Preference instaPref = findPreference("pref_key_show_us_love_instagram");
            Preference twitterPref = findPreference("pref_key_show_us_love_twitter");

            facebookPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = Helpers.newFacebookProfileIntent(getActivity().getApplicationContext().getPackageManager(),
                            getResources().getString(R.string.label_app_facebook_link));
                    startActivity(intent);
                    return false;
                }
            });

            instaPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = Helpers.newInstagramProfileIntent(getActivity().getApplicationContext().getPackageManager(),
                            getResources().getString(R.string.label_app_instagram_link));
                    startActivity(intent);
                    return true;
                }
            });

            twitterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = Helpers.newTwitterProfileIntent(getActivity().getApplicationContext().getPackageManager(),
                            getResources().getString(R.string.label_app_twitter_link));
                    startActivity(intent);
                    return false;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.left_in, R.anim.right_out);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onResume() {
            super.onResume();
            curFragmentEnum = PreferenceFragmentEnum.MAIN;
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {

        private CheckBoxPreference notifyDirectFistbumpPref;
        private CheckBoxPreference notifyPostFistbumpPref;
        private CheckBoxPreference notifyCommentFistbumpPref;
        private CheckBoxPreference notifyNewCommentPref;
        private CheckBoxPreference notifyDirectMessagePref;

        private UserProfileParcel userProfileParcel;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            notifyDirectFistbumpPref = (CheckBoxPreference) findPreference("pref_notifications_direct_fistbump");
            notifyPostFistbumpPref = (CheckBoxPreference) findPreference("pref_notifications_post_fistbump");
            notifyCommentFistbumpPref = (CheckBoxPreference) findPreference("pref_notifications_comment_fistbump");
            notifyNewCommentPref = (CheckBoxPreference) findPreference("pref_notifications_new_comment");
            notifyDirectMessagePref = (CheckBoxPreference) findPreference("pref_notifications_direct_message");

            userProfileParcel = ((SettingsActivity) getActivity()).getUserProfileParcel();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.left_in, R.animator.right_out)
                        .replace(android.R.id.content, mainPreferencesFragment)
                        .commit();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onResume() {
            super.onResume();
            curFragmentEnum = PreferenceFragmentEnum.NOTIFICATION;
        }

        @Override
        public void onPause() {
            super.onPause();
            dynamoDBHelper.updateUserNotificationPreferences(
                    userProfileParcel.getCurUsername(),
                    notifyDirectFistbumpPref.isChecked(),
                    notifyPostFistbumpPref.isChecked(),
                    notifyCommentFistbumpPref.isChecked(),
                    notifyNewCommentPref.isChecked(),
                    notifyDirectMessagePref.isChecked());
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            ((SettingsActivity) getActivity()).makeToastNotification(getResources().getString(R.string.pref_notifications_pref_saved));
        }
    }

    /**
     * This fragment shows the Privacy Policy of the app
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrivacyPolicyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_privacy_policy);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.animator.left_in, R.animator.right_out)
                            .replace(android.R.id.content, mainPreferencesFragment)
                            .commit();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            curFragmentEnum = PreferenceFragmentEnum.PRIVACY_POLICY;
        }
    }
}
