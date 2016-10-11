package com.example.spark.test.slidealbums.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.spark.test.api.API;
import com.example.spark.test.slidealbums.SlideAlbum;
import com.example.spark.test.slidealbums.SlideAlbumsMgr;
import com.example.spark.test.util.JsonUtil;
import com.example.spark.test.util.TestUtil;
import com.example.spark.test.util.TestUtil.TestResponse;
import com.example.spark.test.util.Util;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import spark.Spark;

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
	public void createSlideAlbumTest() {
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
	public void getSlideAlbumsTest() {
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("customers", Arrays.asList("Bosch"));
		data.put("sessionToken", "exampleSessionTokenHere");
		TestResponse response = TestUtil.postRequest("/spark/api/public/slidealbums", data);
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
	public void getSlideAlbumsTestInvalidToken() {	
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("customers", Arrays.asList("Bosch"));
		TestResponse response = TestUtil.postRequest("/spark/api/public/slidealbums", data);
		assertEquals(401, response.status); // no/invalid session token is passed, so request is unauthorized
		String body = response.body;
		assertTrue(body == null || body.equals(""));
	}
	
	@Test
	public void getSlideAlbum() {	
		String title = "Eli Test 123";
		String customer = "Bosch";
		String svg = "Central Locking_01";
		String url = Util.constructURL("http", "localhost", 4567, "/test/api/slidealbum/" + customer + "/" + title);
		TestResponse response = TestUtil.getRequest(url);
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
	public void deleteSlideAlbumTest() {
		String title = "Eli Test 789";
		String customer = "Bosch";
		String svg = "Anti-lock Brake System (ABS)_01.svg";
		createSlideAlbum(title, customer, svg);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("title", title);
		data.put("customer", customer);
		data.put("sessionToken", "exampleSessionTokenHere");
		TestResponse response = TestUtil.postRequest("/spark/api/public/slidealbums/delete", data);
		assertEquals(200, response.status); 		
		String body = response.body;
		JsonObject jobj = JsonUtil.fromJsonToClass(body, JsonObject.class);
		String sessionToken = jobj.get("sessionToken").getAsString();
		assertNotNull(sessionToken);
		File slideAlbumDir = new File("D:/xampp-7/htdocs/workspaces/" + customer + "/" + title);
		assertTrue(!slideAlbumDir.exists());
	}
	
	@Test 
	public void testMultiPartRequess() {
		
		String url = "http://localhost:4567/spark/api/slidealbums/create";
		String svg = "Anti-lock Brake System (ABS)_01.svg";
		String title = "Fucking test";
		String customer = "Bosch";
		String sessionToken = "test123";
		File source = new File("src/test/resources/" + svg);
		File dest = new File(Util.uploadDirPath + "/" + svg);
		try {
			FileUtils.copyFile(source, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(url);

		FileBody uploadFilePart = new FileBody(new File(Util.uploadDirPath + "/" + svg));
		MultipartEntity reqEntity = new MultipartEntity();
		try {
			reqEntity.addPart("title", new StringBody(title));
			reqEntity.addPart("customer", new StringBody(customer));
			reqEntity.addPart("sessionToken", new StringBody(sessionToken));
			reqEntity.addPart("files[]", uploadFilePart);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpPost.setEntity(reqEntity);
		try {
			HttpResponse resp = httpclient.execute(httpPost);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			String responseText = result.toString();
			System.out.println(new TestResponse(resp.getStatusLine().getStatusCode(), responseText));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void deleteSlideAlbum(String title, String customer) {
		SlideAlbumsMgr mgr = new SlideAlbumsMgr();
		mgr.deleteSlideAlbum(title, customer);
	}
	
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
