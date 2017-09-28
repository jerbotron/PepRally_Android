package com.peprally.jeremy.peprally.network;


import android.util.Log;

import com.peprally.jeremy.peprally.services.LoginService;
import com.peprally.jeremy.peprally.services.NotificationService;
import com.peprally.jeremy.peprally.services.PostService;
import com.peprally.jeremy.peprally.utils.Constants;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    private static ApiManager instance = null;
    private Retrofit retrofit;

    private PostService postService;
    private LoginService loginService;
	private NotificationService notificationService;

    private ApiManager() {
        OkHttpClient client = new OkHttpClient();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        postService = retrofit.create(PostService.class);
	    loginService = retrofit.create(LoginService.class);
	    notificationService = retrofit.create(NotificationService.class);
    }

    public static ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    public PostService getPostService() {
        return postService;
    }
    
    public LoginService getLoginService() {
	    return loginService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }
	
	public static void handleCallbackFailure(Throwable throwable) {
		StackTraceElement[] arr = throwable.getStackTrace();
		for (StackTraceElement s : arr) {
			Log.d("Error stack", s.toString());
		}
		Log.d("Network failure", "error msg = " + throwable.getMessage());
	}
}