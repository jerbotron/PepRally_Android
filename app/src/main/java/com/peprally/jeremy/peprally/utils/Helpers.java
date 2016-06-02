package com.peprally.jeremy.peprally.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;

import java.util.Set;

public class Helpers {

    // CONSTANTS
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
}
