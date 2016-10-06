package com.example.spark.test.api;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.spark.test.slidealbums.SlideAlbumsMgr;
import com.google.gson.Gson;

import spark.ResponseTransformer;

public class Main {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		 
		SlideAlbumsMgr slideAlbumsMgr = new SlideAlbumsMgr();
		
		after((request, response) -> {
			response.header("Content-Type", "application/json");
			response.header("Access-Control-Allow-Origin", "http://localhost:1818");
		});
		
		get("/hello", (req, res) -> "Hello World");
		 
		post("/spark/api/slidealbums", (request, response) -> {
			HashMap<String, Object> data = new Gson().fromJson(request.body(), HashMap.class);
			List<String> customers = data.get("customers") != null ? (ArrayList<String>) data.get("customers") : new ArrayList<String>();			
			return slideAlbumsMgr.getSlideAlbums(customers);
		}, new ResponseTransformer() {	
			@Override
			public String render(Object arg0) throws Exception {
				return new Gson().toJson(arg0);
			}
		});		 
	}
	

}
