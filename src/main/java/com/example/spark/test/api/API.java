package com.example.spark.test.api;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.spark.test.slidealbums.SlideAlbum;
import com.example.spark.test.slidealbums.SlideAlbumsMgr;
import com.example.spark.test.util.JsonUtil;

/**
 * Main API class defining all the end points
 * @author Elitza Haltakova
 *
 */
public class API {

	public static void main(String[] args) {
		 
		SlideAlbumsMgr slideAlbumsMgr = new SlideAlbumsMgr();
		
		before("/spark/api/*", Filters.ensureSessionTokenIsValid);
		after("/spark/api/*", Filters.addResponseHeaders);
		after("/spark/api/*", Filters.regenerateSessionToken);
		
		get("/hello", (req, res) -> "Hello World");
		 
		post("/spark/api/slidealbums", (request, response) -> {
			HashMap<String, Object> data = JsonUtil.fromJson(request.body());
			@SuppressWarnings("unchecked")
			List<String> customers = data.get("customers") != null ? (List<String>) data.get("customers") : new ArrayList<String>();			
			List<SlideAlbum> slideAlbums = slideAlbumsMgr.getSlideAlbums(customers);
			HashMap<String, Object> responseData = new HashMap<String, Object>();
			responseData.put("slideAlbums", slideAlbums);
			return JsonUtil.toJson(responseData);
		});		
		
		// test route
		get("/test/api/slidealbum/*/*", (request, response) -> {
			String customer = request.splat()[0];
			String title = request.splat()[1];
			SlideAlbum slideAlbum = slideAlbumsMgr.getSlideAlbum(title, customer);
			if(slideAlbum == null) {
				halt(404, "No such slide album is found");
			}
			return JsonUtil.toJson(slideAlbum);
		});		
	}
	
}
