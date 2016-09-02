package com.peprally.jeremy.peprally.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
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
import com.peprally.jeremy.peprally.custom.ui.CircleImageTransformation;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class Helpers {

    /**
     * UI Helpers
     */
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

    public static Spanned getAPICompatHtml(String htmlText) {
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(htmlText);
        }
    }

    public static Drawable getAPICompatVectorDrawable(Context callingContext, int resource_id) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            return ContextCompat.getDrawable(callingContext, resource_id);
        } else {
            return VectorDrawableCompat.create(callingContext.getResources(), resource_id, callingContext.getTheme());
        }
    }

    /**
     * General Helpers
     */
    public static String getFacebookProfilePictureURL(String facebookId, int size) {
        String type;
        switch (size) {
            case 0:
                type = "type=small";
                break;
            case 1:
                type = "type=normal";
                break;
            case 2:
                type = "type=album";
                break;
            case 3:
                type = "type=large";
                break;
            case 4:
                type = "type=square";
                break;
            case 5:
            default:
                type = "width=9999";
                break;
        }
        return "https://graph.facebook.com/" + facebookId + "/picture?" + type;
    }

    public static Bitmap getFacebookProfilePictureBitmap(String facebookId, Context callingContext){
        try {
            URL imageURL = new URL("https://graph.facebook.com/" + facebookId + "/picture?type=large");
            return BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return BitmapFactory.decodeResource(callingContext.getResources(), R.drawable.logo_push_ut);
        }
    }

    public static String getFavPlayerString(String firstName, String lastName, int number, String team) {
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

    public static Long getTimestampSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static String getTimestampString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return df.format(c.getTime());
    }

    public static String getTimetampString(Long timestampInSeconds, boolean shortSuffix) {
        long tsLongNow = getTimestampSeconds();
        long timeInSeconds = tsLongNow - timestampInSeconds;
        String timestampString, suffix;
        if (timeInSeconds < 60) {
            suffix = (shortSuffix) ? "s" : " seconds ago";
            timestampString = String.valueOf(timeInSeconds) + suffix;
        }
        else if (timeInSeconds < 60 * 60) {
            suffix = (shortSuffix) ? "m" : " minutes ago";
            timestampString = String.valueOf(timeInSeconds / 60) + suffix;
        }
        else if (timeInSeconds < 60 * 60 * 24) {
            suffix = (shortSuffix) ? "h" : " hours ago";
            timestampString = String.valueOf(timeInSeconds/60/60) + suffix;
        }
        else {
            suffix = (shortSuffix) ? "d" : " days ago";
            timestampString = String.valueOf(timeInSeconds/60/60/24) + suffix;
        }

        return timestampString;
    }

    public static String getPostCommentIdString(String username, Long timestamp) {
        return username + "_" + timestamp.toString();
    }

    public static int generateRandomInteger() {
        Random random = new Random(getTimestampSeconds());

        return random.nextInt(1000000000);
    }

    public static boolean isValidEmail(CharSequence targetEmail) {
        return !TextUtils.isEmpty(targetEmail) && android.util.Patterns.EMAIL_ADDRESS.matcher(targetEmail).matches();
    }


    /**
     * Network Helpers
     */
    public static boolean checkIfNetworkConnectionAvailable(ConnectivityManager connManager) {
        boolean isConnected = false;
        NetworkInfo mWifi = connManager.getActiveNetworkInfo();
        if (mWifi!=null) isConnected = mWifi.isConnected();
        return isConnected;
    }

    public static void setFacebookProfileImage(Context callingContext,
                                               ImageView imageView,
                                               String facebookId,
                                               int size,
                                               boolean rounded) {
        if (facebookId != null && !facebookId.isEmpty()) {
            if (rounded) {
                Picasso.with(callingContext)
                        .load(getFacebookProfilePictureURL(facebookId, size))
                        .placeholder(R.drawable.img_default_profile)
                        .error(R.drawable.img_default_profile)
                        .transform(new CircleImageTransformation())
                        .into(imageView);
            } else {
                Picasso.with(callingContext)
                        .load(getFacebookProfilePictureURL(facebookId, size))
                        .placeholder(R.drawable.img_default_profile)
                        .error(R.drawable.img_default_profile)
                        .into(imageView);
            }
        }
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
            return instanceId.getToken();
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
                uri = Uri.parse("http://www.facebook.com/" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/peprallyapp"));
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

    /**
     * Physical Helpers
     */
    public static void vibrateDeviceNotification(Context callingContext) {
        Vibrator v = (Vibrator) callingContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }
}
