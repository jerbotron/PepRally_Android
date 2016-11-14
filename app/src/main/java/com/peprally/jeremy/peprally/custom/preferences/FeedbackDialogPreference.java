package com.peprally.jeremy.peprally.custom.preferences;


import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.SettingsActivity;
import com.peprally.jeremy.peprally.custom.Feedback;
import com.peprally.jeremy.peprally.enums.FeedbackEnum;
import com.peprally.jeremy.peprally.enums.PlatformEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;

public class FeedbackDialogPreference extends DialogPreference {

    private EditText feedbackText;

    private static final String TAG = FeedbackDialogPreference.class.getSimpleName();

    public FeedbackDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.settings_feedback_layout);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        // initialize UI components
        feedbackText = (EditText) view.findViewById(R.id.id_edit_text_feedback);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (feedbackText != null) {
                    if (feedbackText.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), "Feedback box is empty!", Toast.LENGTH_SHORT).show();
                    } else {
                        sendFeedback(feedbackText.getText().toString().trim());
                        Toast.makeText(getContext(), "Feedback sent!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
        }
    }

    private void sendFeedback(String feedbackText) {
        final DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(getContext());
        final UserProfileParcel userProfileParcel = ((SettingsActivity) getContext()).getUserProfileParcel();
        dynamoDBHelper.createNewFeedback(new Feedback(userProfileParcel.getCurrentUsername(),
                                                      feedbackText,
                                                      Helpers.getTimestampSeconds(),
                                                      PlatformEnum.ANDROID,
                                                      FeedbackEnum.GENERAL));
    }
}
