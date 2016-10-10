package com.example.spark.test.slidealbums.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.spark.test.api.API;
import com.example.spark.test.slidealbums.SlideAlbum;
import com.example.spark.test.util.TestUtil;
import com.example.spark.test.util.TestUtil.TestResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import spark.Spark;

public class SlideAlbumsMgrIntTest {

	@BeforeClass
	public static void beforeClass() {
		API.main(null);
	}
	
	@AfterClass
	public static void afterClass() {
		Spark.stop();
	}
	
	@Test
	public void getSlideAlbumsTest() {
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("customers", Arrays.asList("Harley Davidson"));
		data.put("sessionToken", "exampleSessionTokenHere");
		TestResponse response = TestUtil.postRequest("/spark/api/slidealbums", data);
		assertEquals(200, response.status); 		
		String body = response.body;
		JsonObject jobj = new Gson().fromJson(body, JsonObject.class);
		String sessionToken = jobj.get("sessionToken").getAsString();
		String slideAlbumsAsSt = new Gson().toJson(jobj.get("slideAlbums"));
		Type collectionType = new TypeToken<List<SlideAlbum>>(){}.getType();
		List<SlideAlbum> albums = new Gson().fromJson(slideAlbumsAsSt, collectionType);
		assertNotNull(sessionToken);
		assertNotNull(albums);
		assertTrue(albums.size() > 0);
		SlideAlbum album = albums.get(1);
		assertNotNull(album);
		assertNotNull(album.getTitle());
		assertNotNull(album.getCustomer());
		assertNotNull(album.getModificationDate());
		assertNotNull(album.getFiles());
		assertTrue(album.getFiles().size()>=1);
		assertNotNull(album.getSvg());
	}
	
	@Test
	public void getSlideAlbumsTestInvalidToken() {
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("customers", Arrays.asList("Harley Davidson"));
		TestResponse response = TestUtil.postRequest("/spark/api/slidealbums", data);
		assertEquals(401, response.status); // no/invalid session token is passed, so request is unauthorized
		String body = response.body;
		assertTrue(body == null || body.equals(""));
	}
	
	@Test
	public void getSlideAlbum() {
		
		String title = "AC 2";
		String customer = "Bosch";
		
		String url = TestUtil.constructURL("http", "localhost", 4567, "/test/api/slidealbum/" + customer + "/" + title);
		TestResponse response = TestUtil.getRequest(url);
		assertEquals(200, response.status); 
		String body = response.body;
		assertFalse(body == null || body.equals(""));
		SlideAlbum album = new Gson().fromJson(body, SlideAlbum.class);
		assertEquals(album.getTitle(), title);
		assertEquals(album.getCustomer(), customer);
		assertEquals("AC2_updated", album.getSvg());
		assertTrue(album.getFiles().size() == 3);

	}
	
}
