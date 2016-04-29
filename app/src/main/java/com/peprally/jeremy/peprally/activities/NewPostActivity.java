package com.peprally.jeremy.peprally.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.peprally.jeremy.peprally.R;

public class NewPostActivity extends AppCompatActivity {

    private ActionBar supportActionBar;
    private EditText editTextNewPost;
    private TextView textViewCharCount;

    private int charCount = 200;

    private static final String TAG = NewPostActivity.class.getSimpleName();

    private TextWatcher newPostTextWatcher = new TextWatcher() {
        int prev_length = 0;
        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            charCount -= (s.length() - prev_length);
            textViewCharCount.setText(String.valueOf(charCount));
            prev_length = s.length();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        supportActionBar = getSupportActionBar();
        supportActionBar.setTitle("New Post");
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        editTextNewPost = (EditText) findViewById(R.id.new_post_text_container);
        textViewCharCount = (TextView) findViewById(R.id.new_post_char_count);

        editTextNewPost.addTextChangedListener(newPostTextWatcher);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
