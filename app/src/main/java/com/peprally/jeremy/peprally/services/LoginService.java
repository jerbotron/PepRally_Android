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
	
	@GET("/login/cognito_id")
	Call<UserResponse> getUserProfileWithCognitoId(@Query("cognitoId") String cognitoId);
	
	@GET("/login/username")
	Call<UserResponse> getUserProfileWithUsername(@Query("username") String username);

	@GET("/login/player_profile")
	Call<UserResponse> getUserProfileWithPlayerProfile(@Query("firstname") String firstname,
                                                       @Query("lastname") String lastname);
	
	@GET("/login/verify_username")
	Call<UsernameResponse> verifyUsername(@Query("username") String username);
	
	@POST("/login/new_user")
	Call<BaseResponse> createNewUser(@Body UserProfile userProfile);
	
}
