package com.example.spark.test.api;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.spark.test.slidealbums.SlideAlbum;
import com.example.spark.test.slidealbums.SlideAlbumsMgr;
import com.google.gson.Gson;

/**
 * Main API class defining all the end points
 * @author Elitza Haltakova
 *
 */
public class API {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		 
		SlideAlbumsMgr slideAlbumsMgr = new SlideAlbumsMgr();
		
		before(Filters.ensureSessionTokenIsValid);
		after(Filters.addResponseHeaders);
		after(Filters.regenerateSessionToken);
		
		get("/hello", (req, res) -> "Hello World");
		 
		post("/spark/api/slidealbums", (request, response) -> {
			HashMap<String, Object> data = new Gson().fromJson(request.body(), HashMap.class);
			List<String> customers = data.get("customers") != null ? (ArrayList<String>) data.get("customers") : new ArrayList<String>();			
			List<SlideAlbum> slideAlbums = slideAlbumsMgr.getSlideAlbums(customers);
			HashMap<String, Object> responseData = new HashMap<String, Object>();
			responseData.put("slideAlbums", slideAlbums);
			return new Gson().toJson(responseData);
		});		 
	}
	
}
