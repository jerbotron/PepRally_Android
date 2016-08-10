package com.peprally.jeremy.peprally.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.peprally.jeremy.peprally.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class Helpers {

    // CONSTANTS
    public final static Integer INTEGER_DEFAULT_COUNT = 0;
    public final static Integer INTEGER_INVALID = -1;

    public final static int FAV_TEAM_REQUEST_CODE = 0;
    public final static int FAV_PLAYER_REQUEST_CODE = 1;
    public final static int NEW_POST_REQUEST_CODE = 2;
    public final static int POST_COMMENT_REQUEST_CODE = 3;

    // UI Helpers
    public static boolean isKeyboardShown(View rootView) {
        // 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard
        final int SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128;

        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        // heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top)
        int heightDiff = rootView.getBottom() - r.bottom;
        // Threshold size: dp to pixels, multiply with display density
        boolean isKeyboardShown = heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density;

//        Log.d(TAG, "isKeyboardShown ? " + isKeyboardShown + ", heightDiff:" + heightDiff + ", density:" + dm.density
//                + "root view height:" + rootView.getHeight() + ", rect:" + r);

        return isKeyboardShown;
    }

    public static void hideSoftKeyboard(Context context, View view) {
        // Hide soft keyboard if keyboard is up
        if (isKeyboardShown(view.getRootView())) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    // General Helpers
    private static String getFacebookProfileImageURL(String facebookID, int size) {
        String type;
        switch (size) {
            case 0:
                type = "small";
                break;
            case 1:
                type = "normal";
                break;
            case 2:
                type = "album";
                break;
            case 4:
                type = "square";
                break;
            case 3:
            default:
                type = "large";
                break;
        }
        return "https://graph.facebook.com/" + facebookID + "/picture?type=" + type;
    }

    public static String getFavPlayerText(String firstName, String lastName, int number, String team) {
        switch (team) {
            case "Golf":
            case "Rowing":
            case "Swimming and Diving":
            case "Tennis":
            case "Track and Field":
                return firstName + " " + lastName;
            default:
                return "#" + number + " " + firstName + " " + lastName;
        }
    }

    public static Long getTimestampMiliseconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static String getTimestampString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return df.format(c.getTime());
    }

    public static String getTimetampString(Long timestampInSeconds) {
        long tsLongNow = getTimestampMiliseconds();
        long timeInSeconds = tsLongNow - timestampInSeconds;
        String timestampString;
        if (timeInSeconds < 60) {
            timestampString = String.valueOf(timeInSeconds) + "s";
        }
        else if (timeInSeconds < 60 * 60) {
            long timeInMins = timeInSeconds / 60;
            timestampString = String.valueOf(timeInMins) + "m";
        }
        else if (timeInSeconds < 60 * 60 * 24) {
            long timeInHrs = timeInSeconds/60/60;
            timestampString = String.valueOf(timeInHrs) + "h";
        }
        else {
            long timeInDays = timeInSeconds/60/60/24;
            timestampString = String.valueOf(timeInDays) + "d";
        }
        return timestampString;
    }

    public static int generateRandomInteger() {
        Random random = new Random(getTimestampMiliseconds());

        return random.nextInt(1000000000);
    }

    public static boolean isValidEmail(CharSequence targetEmail) {
        return !TextUtils.isEmpty(targetEmail) && android.util.Patterns.EMAIL_ADDRESS.matcher(targetEmail).matches();
    }


    // Network Helpers

    public static boolean checkIfNetworkConnectionAvailable(ConnectivityManager connManager) {
        boolean isConnected = false;
        NetworkInfo mWifi = connManager.getActiveNetworkInfo();
        if (mWifi!=null) isConnected = mWifi.isConnected();
        return isConnected;
    }

    public static void setFacebookProfileImage(Context callingContext,
                                        ImageView imageView,
                                        String facebookID,
                                        int size) {
        Picasso.with(callingContext)
                .load(getFacebookProfileImageURL(facebookID, size))
                .placeholder(R.drawable.img_default_profile)
                .error(R.drawable.img_default_profile)
                .into(imageView);
    }

    public static boolean checkGooglePlayServicesAvailable(Activity callingActivity)
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        final int status = googleAPI.isGooglePlayServicesAvailable(callingActivity.getApplicationContext());
        if (status == ConnectionResult.SUCCESS) { return true; }

        Log.e("GOOGLE_PLAY_SERVICES:", "Google Play Services not available: " + googleAPI.getErrorString(status));

        if (googleAPI.isUserResolvableError(status))
        {
            final Dialog errorDialog = googleAPI.getErrorDialog(callingActivity, status, 1);
            if (errorDialog != null)
            {
                errorDialog.show();
            }
        }

        return false;
    }

    public static String getFCMInstanceId(Activity callingActivity)
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        final int status = googleAPI.isGooglePlayServicesAvailable(callingActivity.getApplicationContext());
        if (status == ConnectionResult.SUCCESS)
        {
            FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();
            String token = instanceId.getToken();
            Log.d("HELPERS: ", "FCM reg token = " + token);
            return token;
        }
        return null;
    }

    public static Intent newInstagramProfileIntent(PackageManager pm, String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            if (pm.getPackageInfo("com.instagram.android", 0) != null) {
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                final String username = url.substring(url.lastIndexOf("/") + 1);
                // http://stackoverflow.com/questions/21505941/intent-to-open-instagram-user-profile-on-android
                intent.setData(Uri.parse("http://instagram.com/_u/" + username));
                intent.setPackage("com.instagram.android");
                return intent;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            ignored.printStackTrace();
        }
        intent.setData(Uri.parse(url));
        return intent;
    }

    public static Intent newFacebookProfileIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    public static Intent newTwitterProfileIntent(PackageManager pm, String userId) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            // get the Twitter app if possible
            pm.getPackageInfo("com.twitter.android", 0);
            intent.setData(Uri.parse("twitter://user?user_id=" + userId));
            intent.setPackage("com.twitter.android");
            return intent;
        } catch (PackageManager.NameNotFoundException ignored) {
            // no Twitter app, revert to browser
            intent.setData(Uri.parse("https://twitter.com/" + userId));
        }
        return intent;
    }

    // Physical Helpers

    public static void vibrateDeviceNotification(Context callingContext) {
        Vibrator v = (Vibrator) callingContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }
}
