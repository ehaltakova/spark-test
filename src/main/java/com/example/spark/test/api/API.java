package com.example.spark.test.api;

import static spark.Spark.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.example.spark.test.auth.AuthenticationMgr;
import com.example.spark.test.slidealbums.SlideAlbum;
import com.example.spark.test.slidealbums.SlideAlbumsMgr;
import com.example.spark.test.util.CORSUtil;
import com.example.spark.test.util.JsonUtil;
import com.example.spark.test.util.Util;

/**
 * Main API class defining all the end points
 * @author Elitza Haltakova
 *
 */
public class API {

	public static void main(String[] args) {
		 
		SlideAlbumsMgr slideAlbumsMgr = new SlideAlbumsMgr();
		
		File uploadDir = Util.configureUploadFilesDir();
		CORSUtil.enableCORS();

		before("/spark/api/public/*", Filters.ensureSessionTokenIsValid);
		before(Filters.addResponseHeaders);
		after("/spark/api/public/*", Filters.regenerateSessionToken);
		 
		post("/spark/api/public/slidealbums", (request, response) -> {
			HashMap<String, Object> data = JsonUtil.fromJson(request.body());
			@SuppressWarnings("unchecked")
			List<String> customers = data.get("customers") != null ? (List<String>) data.get("customers") : new ArrayList<String>();			
			List<SlideAlbum> slideAlbums = slideAlbumsMgr.getSlideAlbums(customers);
			HashMap<String, Object> responseData = new HashMap<String, Object>();
			responseData.put("slideAlbums", slideAlbums);
			return JsonUtil.toJson(responseData);
		});		
		
		post("/spark/api/slidealbums/create", (request, response) -> {
			
			// apache commons-fileupload to handle file upload
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setRepository(uploadDir);
			ServletFileUpload fileUpload = new ServletFileUpload(factory);
			List<FileItem> items = fileUpload.parseRequest(request.raw());

			String title =  items.stream().filter(e -> "title".equals(e.getFieldName())).findFirst().get().getString();
			String customer =  items.stream().filter(e -> "customer".equals(e.getFieldName())).findFirst().get().getString();
			String sessionToken =  items.stream().filter(e -> "sessionToken".equals(e.getFieldName())).findFirst().get().getString();
			FileItem item = items.stream().filter(e -> "files[]".equals(e.getFieldName())).findFirst().get();
			String fileName = item.getName();
			item.write(new File(uploadDir, fileName));
			
			SlideAlbum slidealbum = slideAlbumsMgr.createSlideAlbum(title, customer, fileName);
			if(slidealbum == null) {
				halt(500, "Error occured. Slide albums was not created.");
			}
			AuthenticationMgr authMgr = new AuthenticationMgr();
			sessionToken = authMgr.regenerateSessionToken(sessionToken);
			HashMap<String, Object> responseData = new HashMap<String, Object>();
			responseData.put("sessionToken", sessionToken);
			responseData.put("slideAlbum", slidealbum);
			return JsonUtil.toJson(responseData);
		});
		
		post("/spark/api/public/slidealbums/delete", (request, response) -> {
			HashMap<String, Object> data = JsonUtil.fromJson(request.body());
			String title = data.get("title").toString();
			String customer = data.get("customer").toString();
			boolean success = slideAlbumsMgr.deleteSlideAlbum(title, customer);
			if(!success) {
				halt(500, "Error occured. Slide album was not found or wasn't deleted successfully.");
			} 
			return "";
		});		
		
		// test route
		get("/hello", (req, res) -> "Hello World");

		// test route
		get("/test/api/slidealbum/*/*", (request, response) -> {
			String customer = request.splat()[0];
			String title = request.splat()[1];
			SlideAlbum slideAlbum = slideAlbumsMgr.getSlideAlbum(title, customer);
			if (slideAlbum == null) {
				halt(404, "No such slide album is found");
			}
			return JsonUtil.toJson(slideAlbum);
		});
	}
	
}
