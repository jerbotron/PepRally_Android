package com.peprally.jeremy.peprally;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class FavoritePlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_player);

        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setTitle("Pick a favorite team");
    }
}
