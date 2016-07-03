package com.peprally.jeremy.peprally.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class Helpers {

    // CONSTANTS
    public final static Integer INTEGER_DEFAULT_COUNT = 0;
    public final static Integer INTEGER_INVALID = -1;

    public final static int FAV_TEAM_REQUEST_CODE = 0;
    public final static int FAV_PLAYER_REQUEST_CODE = 1;
    public final static int NEW_POST_REQUEST_CODE = 2;
    public final static int POST_COMMENT_REQUEST_CODE = 3;

    // Helper Functions
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

    public static void hideSoftKeyboard(Activity context, View view) {
        // Hide soft keyboard if keyboard is up
        if (isKeyboardShown(view.getRootView())) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

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

    public static boolean checkGooglePlayServicesAvailable(Activity callingActivity)
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        final int status = googleAPI.isGooglePlayServicesAvailable(callingActivity.getApplicationContext());
        if (status == ConnectionResult.SUCCESS)
        {
            FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();
            String token = instanceId.getToken();
            Log.d("HELPERS: ", "FMS reg token = " + token);
            return true;
        }

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

    public static String getFMSInstanceID(Activity callingActivity)
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        final int status = googleAPI.isGooglePlayServicesAvailable(callingActivity.getApplicationContext());
        String token = null;
        if (status == ConnectionResult.SUCCESS)
        {
            FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();
            token = instanceId.getToken();
            Log.d("HELPERS: ", "FMS reg token = " + token);
        }
        return token;
    }
}
