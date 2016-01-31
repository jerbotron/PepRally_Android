package com.peprally.jeremy.peprally;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_profile);
    }

    @Override
    public void onBackPressed() { }

    public void onLogoutButtonClick(View view) {
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
