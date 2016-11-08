package com.example.spark.test.api;

import static spark.Spark.*;

import java.io.File;
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

import spark.Spark;
import spark.servlet.SparkApplication;

import org.apache.log4j.Logger;
/**
 * Main API class defining all the end points
 * @author Elitza Haltakova
 *
 */
public class API implements SparkApplication{ 
	
	final static Logger logger = Logger.getLogger(API.class);

	public static void main(String[] args) {
		API api = new API();
		api.init();		
	}

	@Override
	public void init() {
		
		// initialize business function mgrs
		SlideAlbumsMgr slideAlbumsMgr = new SlideAlbumsMgr();
		
		// configure static resources folder (used to store uploaded files)
		File uploadDir = Util.configureUploadFilesDir();
		
		// enable CORS
		CORSUtil.enableCORS();
	
		// set up before and after filters
		before("/spark/api/public/*", Filters.ensureSessionTokenIsValid);
		before(Filters.addResponseHeaders);
		after("/spark/api/public/*", Filters.regenerateSessionToken);
		after(Filters.logResponse);
		
		// register exception handling
		exception(Exception.class, ExceptionHandlers.uncheckedExceptions);
		
		// routes
		post(Path.GET_SLIDEALBUMS, (request, response) -> {	
			logger.debug(request.pathInfo() + "  Get Slidealbums");
			logger.debug(request.body());
			// handle request
			HashMap<String, Object> data = JsonUtil.fromJson(request.body());
			if(data == null || data.get("customers") == null) {
				response.status(400);
				return JsonUtil.toJson(new ResponseError("Invalid request. Please, contact your system administrator.").getMessage());
			}
			@SuppressWarnings("unchecked")
			List<String> customers = (List<String>) data.get("customers");			
			
			// call business function
			List<SlideAlbum> slideAlbums = slideAlbumsMgr.getSlideAlbums(customers);
			
			// handle response
			HashMap<String, Object> responseData = new HashMap<String, Object>();
			responseData.put("slideAlbums", slideAlbums);
			return JsonUtil.toJson(responseData);
		});		
		
		post(Path.CREATE_SLIDEALBUM, (request, response) -> {
			logger.debug(request.pathInfo() + "  Create Slidealbum");
			logger.debug(request.body());
			
			// apache commons-fileupload to handle file upload with multi part request
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setRepository(uploadDir);
			ServletFileUpload fileUpload = new ServletFileUpload(factory);
			List<FileItem> items = fileUpload.parseRequest(request.raw());
	
			String title = items.stream().filter(e -> "title".equals(e.getFieldName())).findFirst().get().getString();
			String customer = items.stream().filter(e -> "customer".equals(e.getFieldName())).findFirst().get().getString();
			String sessionToken = items.stream().filter(e -> "sessionToken".equals(e.getFieldName())).findFirst().get().getString();
			FileItem item = items.stream().filter(e -> "files[]".equals(e.getFieldName())).findFirst().get();
			String fileName = item.getName();
			item.write(new File(uploadDir, fileName));
			
			// handle request
			AuthenticationMgr authMgr = new AuthenticationMgr();
			if(sessionToken == null || !authMgr.isSessionTokenValid(sessionToken)) {
				Spark.halt(401, "Your session is invalid or expired. Please, login again.");
			}
			if(title == null || customer == null || fileName == null) {
				response.status(400);
				return JsonUtil.toJson(new ResponseError("Invalid request. Please, contact your system administrator.").getMessage());
			}
			
			// call business function
			SlideAlbum slidealbum = slideAlbumsMgr.createSlideAlbum(title, customer, fileName);
			
			// handle response
			if(slidealbum == null) {
				response.status(400);
				return JsonUtil.toJson(new ResponseError("An error occured. Slide album was not created successfully.").getMessage());
			}
			sessionToken = authMgr.regenerateSessionToken(sessionToken);
			HashMap<String, Object> responseData = new HashMap<String, Object>();
			responseData.put("sessionToken", sessionToken);
			responseData.put("slideAlbum", slidealbum);
			return JsonUtil.toJson(responseData);
		});
		
		post(Path.DELETE_SLIDEALBUM, (request, response) -> {
			logger.debug(request.pathInfo() + "  Delete Slidealbum");
			logger.debug(request.body());

			// handle request
			HashMap<String, Object> data = JsonUtil.fromJson(request.body());
			if(data == null || data.get("title") == null || data.get("customer") == null) {
				response.status(400);
				return JsonUtil.toJson(new ResponseError("Invalid request. Please, contact your system administrator.").getMessage());
			}
			String title = (String) data.get("title");
			String customer = (String) data.get("customer");
			
			// call business function
			boolean success = slideAlbumsMgr.deleteSlideAlbum(title, customer);
			
			// handle response
			if(!success) {
				response.status(400);
				return JsonUtil.toJson(new ResponseError("An error occured. Slide album was not deleted successfully. Please, contact your system administrator.").getMessage());	
			}
			return "";
		});		
	
		// test route
		get("/spark/api/test/slidealbum/*/*", (request, response) -> {
			logger.debug(request.pathInfo() + "  Get Slidealbum");
			logger.debug(request.body());
			
			// handle request
			if(request.splat() == null || request.splat().length < 2) {
				response.status(400);
				return JsonUtil.toJson(new ResponseError("Invalid request. Please, contact your administrator.").getMessage());
			}
			String customer = request.splat()[0];
			String title = request.splat()[1];
			// call business function
			SlideAlbum slideAlbum = slideAlbumsMgr.getSlideAlbum(title, customer);
			// handle response
			if (slideAlbum == null) {
				response.status(400);
				return JsonUtil.toJson(new ResponseError("No slide album with the title %s was found.", title).getMessage());
			}
			return JsonUtil.toJson(slideAlbum);
		});
		
		// test route
		get("/hello", (req, res) -> "Hello World");
	}	
}
