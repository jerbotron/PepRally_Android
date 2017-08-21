package com.peprally.jeremy.peprally.model;

import com.peprally.jeremy.peprally.data.PlayerProfile;
import com.peprally.jeremy.peprally.data.UserProfile;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;

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
