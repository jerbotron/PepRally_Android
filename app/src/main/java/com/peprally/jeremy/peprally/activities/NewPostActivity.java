package com.peprally.jeremy.peprally.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.utils.Helpers;

import java.util.Random;

public class NewPostActivity extends AppCompatActivity {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // UI Variables
    private EditText editTextNewPost;
    private TextView textViewCharCount;

    // General Variables
//    private static final String TAG = NewPostActivity.class.getSimpleName();
    private int charCount = 200;

    /***********************************************************************************************
     *************************************** ACTIVITY METHODS **************************************
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        final NewPostHints newPostHints = new NewPostHints();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        editTextNewPost = (EditText) findViewById(R.id.id_edit_text_new_post);
        textViewCharCount = (TextView) findViewById(R.id.new_post_char_count);

        editTextNewPost.setHint(newPostHints.getRandomHint());
        editTextNewPost.addTextChangedListener(new TextWatcher() {
            int prev_length = 0;
            public void afterTextChanged(Editable s) {
                if (prev_length >= 200) {
                    textViewCharCount.setTextColor(Color.RED);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCount -= (s.length() - prev_length);
                textViewCharCount.setText(String.valueOf(charCount));
                prev_length = s.length();
            }
        });

        final Button newPostButton = (Button) findViewById(R.id.button_new_post);
        assert newPostButton != null;
        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextNewPost.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), "Post can't be empty!", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent();
                    intent.putExtra("NEW_POST_TEXT", editTextNewPost.getText().toString());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
//                overridePendingTransition(R.anim.top_in, R.anim.bottom_out);
                }
                // Hide soft keyboard if keyboard is up
                Helpers.hideSoftKeyboard(getApplicationContext(), editTextNewPost);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                // Hide soft keyboard if keyboard is up
                EditText et = (EditText) findViewById(R.id.id_edit_text_new_post);
                Helpers.hideSoftKeyboard(getApplicationContext(), et);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /***********************************************************************************************
     *********************************** GENERAL METHODS/INTERFACES ********************************
     **********************************************************************************************/
    private final class NewPostHints {
        private String new_post_hint_1 = getResources().getString(R.string.default_post_question_1);
        private String new_post_hint_2 = getResources().getString(R.string.default_post_question_2);
        private String new_post_hint_3 = getResources().getString(R.string.default_post_question_3);
        private String new_post_hint_4 = getResources().getString(R.string.default_post_question_4);
        private String new_post_hint_5 = getResources().getString(R.string.default_post_question_5);
        Random rand;

        NewPostHints() {
            rand = new Random();
        }

        private String getRandomHint() {
            int r = rand.nextInt(4);
            switch (r) {
                case 0:
                    return new_post_hint_1;
                case 1:
                    return new_post_hint_2;
                case 2:
                    return new_post_hint_3;
                case 3:
                    return new_post_hint_4;
                case 4:
                    return new_post_hint_5;
                default:
                    return null;
            }
        }
    }

}
