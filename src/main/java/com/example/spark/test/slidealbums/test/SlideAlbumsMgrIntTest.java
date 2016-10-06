package com.example.spark.test.slidealbums.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.spark.test.api.Main;
import com.example.spark.test.slidealbums.SlideAlbum;
import com.example.spark.test.util.TestUtil;
import com.example.spark.test.util.TestUtil.TestResponse;
import com.google.gson.Gson;

import spark.Spark;

public class SlideAlbumsMgrIntTest {

	@BeforeClass
	public static void beforeClass() {
		Main.main(null);
	}
	
	@AfterClass
	public static void afterClass() {
		Spark.stop();
	}
	
	@Test
	public void getSlideAlbumsTest() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("customers", Arrays.asList("Harley Davidson"));
		TestResponse response = TestUtil.postRequest("/spark/api/slidealbums", data);
		assertEquals(200, response.status);
		String body = response.body;
		SlideAlbum[] albums = new Gson().fromJson(body, SlideAlbum[].class);
		assertTrue(albums.length > 0);
		SlideAlbum album = albums[0];
		assertNotNull(album);
		assertNotNull(album.getTitle());
		assertNotNull(album.getCustomer());
		assertNotNull(album.getModificationDate());
		assertNotNull(album.getFiles());
		assertTrue(album.getFiles().size()>=1);
		assertNotNull(album.getSvg());
	}
	
	
}
