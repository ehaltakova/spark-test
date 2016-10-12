package com.example.spark.test.util;

import static spark.Spark.before;
import static spark.Spark.options;

/**
 * Utility class to enable cross-origin request support
 * @author Elitza Haltakova
 *
 */
public class CORSUtil {

	public static void enableCORS() {
		
		options("/*", (request, response) -> {
	        String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
	        if (accessControlRequestHeaders != null) {
	            response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
	        }
	        String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
	        if (accessControlRequestMethod != null) {
	            response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
	        }
	        return "OK";
	    });
		
		before((request, response) -> {
			response.header("Access-Control-Allow-Origin", "*");
			response.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");			
			response.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-Codingpedia");
		});
	}
}
