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
import com.peprally.jeremy.peprally.custom.UserProfileParcel;
import com.peprally.jeremy.peprally.enums.FeedbackEnum;
import com.peprally.jeremy.peprally.enums.PlatformEnum;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;

public class DeleteAccountDialogPreference extends DialogPreference {

    private EditText feedbackText;

    public DeleteAccountDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.settings_delete_account_layout);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        feedbackText = (EditText) view.findViewById(R.id.id_edit_text_feedback);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                if (feedbackText != null && !feedbackText.getText().toString().isEmpty()) {
                    sendFeedback(feedbackText.getText().toString().trim());
                }
                ((SettingsActivity) getContext()).deleteUserAccount();
                break;
            }
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
                                                      FeedbackEnum.ACCOUNT_DELETION));
    }
}
