package com.peprally.jeremy.peprally.network.callbacks;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.PostCommentActivity;
import com.peprally.jeremy.peprally.activities.ProfileActivity;
import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.custom.UserProfileParcel;
import com.peprally.jeremy.peprally.data.PlayerProfile;
import com.peprally.jeremy.peprally.data.UserProfile;
import com.peprally.jeremy.peprally.enums.ActivityEnum;
import com.peprally.jeremy.peprally.model.UserResponse;
import com.peprally.jeremy.peprally.network.ApiManager;
import com.peprally.jeremy.peprally.network.DynamoDBHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserResponsePostCommentCallback implements Callback<UserResponse> {

	private Context context;
	private String currentUsername;
	private DynamoDBHelper.AsyncTaskCallback callback;
	
	public UserResponsePostCommentCallback(Context callingContext,
										   String currentUsername,
	                                       DynamoDBHelper.AsyncTaskCallback callback) {
		this.context = callingContext;
		this.currentUsername = currentUsername;
		this.callback = callback;
	}
	
	@Override
	public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
		UserResponse userResponse = response.body();
		if (userResponse != null) {
			Intent intent = new Intent(context, ProfileActivity.class);
			UserProfile userProfile = userResponse.getUserProfile();
			PlayerProfile playerProfile = userResponse.getPlayerProfile();
			UserProfileParcel userProfileParcel = null;
			if (userProfile != null) {
				userProfileParcel = new UserProfileParcel(ActivityEnum.PROFILE,
														userProfile,
														playerProfile);
				if (currentUsername != null && !userProfile.getUsername().equals(currentUsername)) {
					userProfileParcel.setIsSelfProfile(false);
					userProfileParcel.setCurrentUsername(currentUsername);
				}
			} else if (playerProfile != null) {
				userProfileParcel = new UserProfileParcel(ActivityEnum.PROFILE,
															currentUsername,
															playerProfile.getFirstName(),
															playerProfile.getTeam(),
															playerProfile.getIndex(),
															false);
			}
			intent.putExtra("USER_PROFILE_PARCEL", userProfileParcel);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			((AppCompatActivity) context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
		} else {
			if (callback != null)
				callback.onTaskDone();
		}
	}
	
	@Override
	public void onFailure(Call<UserResponse> call, Throwable throwable) {
		ApiManager.handleCallbackFailure(throwable);
	}
}
