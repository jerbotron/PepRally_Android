package com.peprally.jeremy.peprally.model;

import com.peprally.jeremy.peprally.data.PlayerProfile;
import com.peprally.jeremy.peprally.data.UserProfile;

public class UserResponse extends BaseResponse {
	
	UserProfile userProfile;
	PlayerProfile playerProfile;
	
	public UserProfile getUserProfile() {
		return userProfile;
	}
	
	public PlayerProfile getPlayerProfile() {
		return playerProfile;
	}
}
