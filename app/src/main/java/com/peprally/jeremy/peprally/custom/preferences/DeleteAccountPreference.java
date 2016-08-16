package com.peprally.jeremy.peprally.custom.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.SettingsActivity;

public class DeleteAccountPreference extends DialogPreference {

    public DeleteAccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.settings_delete_account_layout);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                ((SettingsActivity) getContext()).deleteUserAccount();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
        }
    }
}
