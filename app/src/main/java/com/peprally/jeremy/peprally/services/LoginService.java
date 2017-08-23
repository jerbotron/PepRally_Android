package com.peprally.jeremy.peprally.services;

import com.peprally.jeremy.peprally.data.UserProfile;
import com.peprally.jeremy.peprally.model.BaseResponse;
import com.peprally.jeremy.peprally.model.UserResponse;
import com.peprally.jeremy.peprally.model.UsernameResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoginService {
	
	@GET("/login")
	Call<UserResponse> tryLogin(@Query("cognitoId") String cognitoId);
	
	@GET("/login/verify_username")
	Call<UsernameResponse> verifyUsername(@Query("username") String username);
	
	@POST("/login/new_user")
	Call<BaseResponse> createNewUser(@Body UserProfile userProfile);
	
}
