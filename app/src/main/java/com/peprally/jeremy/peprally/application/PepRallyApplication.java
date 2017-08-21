package com.peprally.jeremy.peprally.application;

import android.app.Application;

import retrofit2.Retrofit;

public class PepRallyApplication extends Application {

    private static PepRallyApplication pepRallyApp;

    @Override
    public void onCreate() {
        super.onCreate();
        pepRallyApp = this;
        
    }

    public static synchronized PepRallyApplication getInstance() {
        return pepRallyApp;
    }


}
