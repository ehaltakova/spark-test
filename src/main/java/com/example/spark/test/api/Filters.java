package com.example.spark.test.api;

import java.util.HashMap;

import com.example.spark.test.auth.AuthenticationMgr;
import com.google.gson.Gson;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Before and after filters for the API endpoints (middleware)
 * @author Elitza Haltakova
 *
 */
@SuppressWarnings("unchecked")
public class Filters {

	private static AuthenticationMgr authMgr = new AuthenticationMgr();

	/**
	 * Before filter
	 * Check if session token passed in the request is valid.
	 */
	public static Filter ensureSessionTokenIsValid = (Request request, Response response) -> {
		HashMap<String, Object> requestData = new Gson().fromJson(request.body(), HashMap.class);
		String sessionToken = requestData.get("sessionToken") != null ? (String) requestData.get("sessionToken") : null;
		if(sessionToken == null || !authMgr.isSessionTokenValid(sessionToken)) {
			Spark.halt(401);
		}
	};
	
	/**
	 * After filter
	 * Add content type and CORS needed headers to the response.
	 */
	public static Filter addResponseHeaders = (Request request, Response response) -> {
		response.header("Content-Type", "application/json");
		response.header("Access-Control-Allow-Origin", "http://localhost:1818");
	};
	
	/**
	 * After filter
	 * Regenerate session token and put the new one into the response
	 */
	public static Filter regenerateSessionToken = (Request request, Response response) -> {
		HashMap<String, Object> requestData = new Gson().fromJson(request.body(), HashMap.class);
		String oldSessionToken = requestData.get("sessionToken") != null ? (String) requestData.get("sessionToken") : null;
		String newSessionToken = authMgr.regenerateSessionToken(oldSessionToken);
		HashMap<String, Object> responseData = new Gson().fromJson(response.body(), HashMap.class);
		responseData.put("sessionToken", newSessionToken);
		response.body(new Gson().toJson(responseData));
	};
}
