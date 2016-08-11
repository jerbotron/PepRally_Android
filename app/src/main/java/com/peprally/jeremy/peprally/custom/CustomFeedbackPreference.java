package com.peprally.jeremy.peprally.custom;


import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.peprally.jeremy.peprally.R;

public class CustomFeedbackPreference extends Preference {
    private static final String TAG = "FeedbackPreference";

    public CustomFeedbackPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.settings_feedback_layout, parent, false);

        EditText feedbackText = (EditText) view.findViewById(R.id.id_edit_text_feedback);

        if (feedbackText != null) {
            Log.d(TAG, "yay, it worked");
        }

        return view;
    }


}
