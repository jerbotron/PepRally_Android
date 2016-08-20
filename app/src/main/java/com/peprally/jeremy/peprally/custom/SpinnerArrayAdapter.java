package com.peprally.jeremy.peprally.custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.enums.SchoolsSupportedEnum;

public class SpinnerArrayAdapter extends ArrayAdapter<String> {

    private Context callingContext;
    private String[] dataArray;

    public SpinnerArrayAdapter(Context context, int layoutResourceId, String[] dataArray) {
        super(context, layoutResourceId, dataArray);
        this.callingContext = context;
        this.dataArray = dataArray;
    }

    private View getCustomDropDownView(int position, View convertView, ViewGroup parent) {
        if (parent != null) {
            parent.setBackgroundResource(R.color.colorBackground);
        }

        // Inflating the layout for the custom Spinner
        LayoutInflater inflater = (LayoutInflater) callingContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.spinner_login_dropdown_item, parent, false);

        // hide array first item text
        if (position == 0) {
            LinearLayout dropdownContainer = (LinearLayout) layout.findViewById(R.id.id_container_spinner_login_dropdown);
            dropdownContainer.removeAllViewsInLayout();
            dropdownContainer.setVisibility(View.GONE);
        } else {
            ImageView imageViewSchoolLogo = (ImageView) layout.findViewById(R.id.id_image_view_spinner_login_dropdown);
            TextView textViewSchoolName = (TextView) layout.findViewById(R.id.id_text_view_spinner_login_dropdown);

            switch (SchoolsSupportedEnum.fromString(dataArray[position])) {
                case PROMPT_TEXT:
                default:
                    break;
                case UT_AUSTIN:
                    imageViewSchoolLogo.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.logo_ut));
                    break;
                case TEXAS_STATE:
                    imageViewSchoolLogo.setImageDrawable(ContextCompat.getDrawable(callingContext, R.drawable.logo_texas_state));
                    break;
            }
            textViewSchoolName.setText(dataArray[position]);
        }

        return layout;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomDropDownView(position, convertView, parent);
    }
}
