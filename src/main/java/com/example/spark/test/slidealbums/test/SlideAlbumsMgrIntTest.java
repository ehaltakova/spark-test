package com.example.spark.test.slidealbums.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.spark.test.api.API;
import com.example.spark.test.api.Path;
import com.example.spark.test.slidealbums.SlideAlbum;
import com.example.spark.test.slidealbums.SlideAlbumsMgr;
import com.example.spark.test.util.JsonUtil;
import com.example.spark.test.util.HTTPUtil;
import com.example.spark.test.util.HTTPUtil.TestResponse;
import com.example.spark.test.util.Util;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import spark.Spark;

/**
 * Slide Albums Manager Tests
 * @author Elitza Haltakova
 *
 */
public class SlideAlbumsMgrIntTest {

	@BeforeClass
	public static void setUp() {
		API.main(null);
		createSlideAlbum("Eli Test 123", "Bosch", "Central Locking_01.svg");
	}
	
	@AfterClass
	public static void cleanUp() {
		deleteSlideAlbum("Eli Test 123", "Bosch");
		Spark.stop();
	}
	
	@Test
	public void createSlideAlbumMgrTest() {
		// prepare for the request - upload svg file to the upload directory
		String svg = "Electronic Control Module (ECM)_01.svg";
		File source = new File("src/test/resources/" + svg);
		File dest = new File(Util.uploadDirPath + "/" + svg);
		try {
			FileUtils.copyFile(source, dest);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		String title = "Eli Test 456";
		String customer = "Bosch";
		String fileName = "Electronic Control Module (ECM)_01";
		
		SlideAlbumsMgr mgr = new SlideAlbumsMgr();
		SlideAlbum album = mgr.createSlideAlbum(title, customer, svg);
		
		assertNotNull(album);
		assertNotNull(album.getTitle());
		assertEquals(title, album.getTitle());
		assertNotNull(album.getCustomer());
		assertEquals(customer, album.getCustomer());
		assertNotNull(album.getSvg());
		assertEquals(fileName, album.getSvg());
		assertNotNull(album.getFiles());
		assertTrue(album.getFiles().size() == 1);
		assertEquals(fileName, album.getFiles().get(0).getName());
		assertEquals("svg", album.getFiles().get(0).getExt());
		deleteSlideAlbum(title, customer);
	}
	
	@Test 
	public void createSlideAlbumMultiPartRequestTest() {
		
		String url = "http://localhost:4567" + Path.CREATE_SLIDEALBUM;
		String svg = "Anti-lock Brake System (ABS)_01.svg";
		String fileName = FilenameUtils.getBaseName(svg);
		String title = "Test Multipart Request";
		String customer = "Bosch";
		String sessionToken = "test123";
		File source = new File("src/test/resources/" + svg);
		File dest = new File(Util.uploadDirPath + "/" + svg);
		try {
			FileUtils.copyFile(source, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("title", title);
		data.put("customer", customer);
		data.put("sessionToken", sessionToken);
		data.put("files[]", dest);
		
		TestResponse response = HTTPUtil.postMultiPartRequest(url, data);
		assertEquals(200, response.status); 		
		String body = response.body;
		JsonObject jobj = JsonUtil.fromJsonToClass(body, JsonObject.class);
		sessionToken = jobj.get("sessionToken").getAsString();
		SlideAlbum album = JsonUtil.fromJsonElementToClass(jobj.get("slideAlbum"), SlideAlbum.class);
		
		assertNotNull(sessionToken);
		assertNotNull(album);
		assertNotNull(album.getTitle());
		assertEquals(title, album.getTitle());
		assertNotNull(album.getCustomer());
		assertEquals(customer, album.getCustomer());
		assertNotNull(album.getSvg());
		assertEquals(fileName, album.getSvg());
		assertNotNull(album.getFiles());
		assertTrue(album.getFiles().size() == 1);
		assertEquals(fileName, album.getFiles().get(0).getName());
		assertEquals("svg", album.getFiles().get(0).getExt());
		deleteSlideAlbum(title, customer);
	}
	
	@Test 
	public void createSlideAlbumBadRequestTest() {
		
		String url = "http://localhost:4567" + Path.CREATE_SLIDEALBUM;
		String svg = "Anti-lock Brake System (ABS)_01.svg";
		String customer = "Fake customer"; // non-existing customer
		String sessionToken = "test123";
		File source = new File("src/test/resources/" + svg);
		File dest = new File(Util.uploadDirPath + "/" + svg);
		try {
			FileUtils.copyFile(source, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HashMap<String, Object> data = new HashMap<String, Object>();
		// no title
		data.put("customer", customer);
		data.put("sessionToken", sessionToken);
		data.put("files[]", dest);
		
		TestResponse response = HTTPUtil.postMultiPartRequest(url, data);
		assertEquals(500, response.status); // exception when parsing multi part data	
	}
	
	@Test
	public void getSlideAlbumsTest() {
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("customers", Arrays.asList("Bosch"));
		data.put("sessionToken", "exampleSessionTokenHere");
		TestResponse response = HTTPUtil.postRequest(Path.GET_SLIDEALBUMS, data);
		assertEquals(200, response.status); 		
		String body = response.body;
		JsonObject jobj = JsonUtil.fromJsonToClass(body, JsonObject.class);
		String sessionToken = jobj.get("sessionToken").getAsString();
		String slideAlbumsAsSt = JsonUtil.toJson(jobj.get("slideAlbums"));
		Type collectionType = new TypeToken<List<SlideAlbum>>(){}.getType();
		@SuppressWarnings("unchecked")
		List<SlideAlbum> albums = (List<SlideAlbum>) JsonUtil.fromJsonToType(slideAlbumsAsSt, collectionType);
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
	public void getSlideAlbumsNoSlideAlbumsForCustomerTest() {
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("customers", Arrays.asList("Fake customer")); // there is no such customer folder
		data.put("sessionToken", "exampleSessionTokenHere");
		TestResponse response = HTTPUtil.postRequest(Path.GET_SLIDEALBUMS, data);
		assertEquals(200, response.status); 
		String body = response.body;
		assertNotNull(body); 
		Type collectionType = new TypeToken<List<SlideAlbum>>(){}.getType();
		JsonObject jobj = JsonUtil.fromJsonToClass(body, JsonObject.class);
		String sessionToken = jobj.get("sessionToken").getAsString();
		String slideAlbumsAsSt = JsonUtil.toJson(jobj.get("slideAlbums"));
		@SuppressWarnings("unchecked")
		List<SlideAlbum> albums = (List<SlideAlbum>) JsonUtil.fromJsonToType(slideAlbumsAsSt, collectionType);
		assertNotNull(sessionToken);
		assertNotNull(albums);
		assertTrue(albums.size() == 0);
	}
	
	@Test
	public void getSlideAlbumsInvalidTokenTest() {	
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("customers", Arrays.asList("Bosch"));
		TestResponse response = HTTPUtil.postRequest(Path.GET_SLIDEALBUMS, data); // request has no sessiontoken infor
		assertEquals(401, response.status); // no/invalid session token is passed, so request is unauthorized
		String body = response.body;
		assertNotNull(body); // error message
	}
	
	@Test
	public void getSlideAlbumTest() {	
		String title = "Eli Test 123";
		String customer = "Bosch";
		String svg = "Central Locking_01";
		String url = HTTPUtil.constructURL("http", "localhost", 4567, "/spark/api/test/slidealbum/" + customer + "/" + title);
		TestResponse response = HTTPUtil.getRequest(url);
		assertEquals(200, response.status); 
		String body = response.body;
		assertFalse(body == null || body.equals(""));
		SlideAlbum album = JsonUtil.fromJsonToClass(body, SlideAlbum.class);
		assertEquals(album.getTitle(), title);
		assertEquals(album.getCustomer(), customer);
		assertEquals(svg, album.getSvg());
		assertTrue(album.getFiles().size() == 1);
	}
	
	@Test
	public void getUnexistingSlideAlbumTest() {	
		String title = "I don't exist"; // request slide album with title that does not exst
		String customer = "Bosch";
		String url = HTTPUtil.constructURL("http", "localhost", 4567, "/spark/api/test/slidealbum/" + customer + "/" + title);
		TestResponse response = HTTPUtil.getRequest(url);
		System.out.println(response.body);
		assertEquals(400, response.status); 
		String body = response.body;
		assertNotNull(body); // error message
	}
	
	@Test
	public void getUnexistingSlideAlbum2Test() {	
		String title = "I don't exist";
		String customer = "Fake customer"; // request slide album with customer that does not exst
		String url = HTTPUtil.constructURL("http", "localhost", 4567, "/spark/api/test/slidealbum/" + customer + "/" + title);
		TestResponse response = HTTPUtil.getRequest(url);
		System.out.println(response.body);
		assertEquals(400, response.status); 
		String body = response.body;
		assertNotNull(body); // error message
	}
	
	@Test 
	public void deleteSlideAlbumTest() {
		String title = "Eli Test 789";
		String customer = "Bosch";
		String svg = "Anti-lock Brake System (ABS)_01.svg";
		createSlideAlbum(title, customer, svg);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("title", title);
		data.put("customer", customer);
		data.put("sessionToken", "exampleSessionTokenHere");
		TestResponse response = HTTPUtil.postRequest(Path.DELETE_SLIDEALBUM, data);
		assertEquals(200, response.status); 		
		String body = response.body;
		JsonObject jobj = JsonUtil.fromJsonToClass(body, JsonObject.class);
		String sessionToken = jobj.get("sessionToken").getAsString();
		assertNotNull(sessionToken);
		File slideAlbumDir = new File("D:/xampp-7/htdocs/workspaces/" + customer + "/" + title);
		assertTrue(!slideAlbumDir.exists());
	}
	
	@Test 
	public void deleteSlideAlbumBadRequestTest() {
		String title = "I don't exist!"; // request delete of a slide album that does not exist
		String customer = "Bosch";
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("title", title);
		data.put("customer", customer);
		data.put("sessionToken", "exampleSessionTokenHere");
		TestResponse response = HTTPUtil.postRequest(Path.DELETE_SLIDEALBUM, data);
		assertEquals(400, response.status); 		
		String body = response.body;
		assertNotNull(body); // error message
	}
	
	@Test 
	public void deleteSlideAlbumIllegalDataTest() {
		List<String> title = new ArrayList<>(); // title is not string but a list
		String customer = "Bosch";
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("title", title);
		data.put("customer", customer);
		data.put("sessionToken", "exampleSessionTokenHere");
		TestResponse response = HTTPUtil.postRequest(Path.DELETE_SLIDEALBUM, data);
		assertEquals(500, response.status); 		
		String body = response.body;
		assertNotNull(body); // error message
	}
	
	@Test 
	public void deleteSlideAlbumInvalidRequestTest() {
		// send request with no title
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("customer", "Bosch");
		data.put("sessionToken", "exampleSessionTokenHere");
		TestResponse response = HTTPUtil.postRequest(Path.DELETE_SLIDEALBUM, data);
		assertEquals(400, response.status); 		
		String body = response.body;
		assertNotNull(body); // error message
	}
	
	// helper method
	private static void deleteSlideAlbum(String title, String customer) {
		SlideAlbumsMgr mgr = new SlideAlbumsMgr();
		mgr.deleteSlideAlbum(title, customer);
	}
	
	// helper method
	private static void createSlideAlbum(String title, String customer, String svg) {
		File source = new File("src/test/resources/" + svg);
		File dest = new File(Util.uploadDirPath + "/" + svg);
		try {
			FileUtils.copyFile(source, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SlideAlbumsMgr mgr = new SlideAlbumsMgr();
		mgr.createSlideAlbum(title, customer, svg);
	}
}
