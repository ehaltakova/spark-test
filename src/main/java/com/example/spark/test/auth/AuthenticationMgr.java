package com.example.spark.test.auth;

/**
 * Authentication Manager - calls web services of the Authentication API
 * @author Elitza Haltakova
 *
 */
public class AuthenticationMgr {

	// TODO
	public boolean isSessionTokenValid(String sessionToken) {
		if(sessionToken != null && !sessionToken.equals("")) {
			return true;
		}
		return false;
	}
	
	// TODO
	public String regenerateSessionToken(String oldSessionToken) {
		return oldSessionToken;
	}
}
