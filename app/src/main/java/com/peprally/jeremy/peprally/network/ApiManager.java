package com.peprally.jeremy.peprally.network;


import com.peprally.jeremy.peprally.services.PushNotificationService;
import com.peprally.jeremy.peprally.utils.Constants;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    private static ApiManager instance = null;
    private Retrofit retrofit;

    private PushNotificationService pushNotificationService;

    private ApiManager() {
        OkHttpClient client = new OkHttpClient();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        pushNotificationService = retrofit.create(PushNotificationService.class);
    }

    public static ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    public PushNotificationService getPushNotificationService() {
        return pushNotificationService;
    }
}
