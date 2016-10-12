package com.example.spark.test.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Utility class providing features for execuing POST and GET requests 
 * @author Elitza Haltakova
 *
 */
public class TestUtil {

	public static TestResponse postRequest(String path, Map<String, Object> data) {
		try {
			String url = "http://localhost:4567" + path;
			HttpClient client =  HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/x-www-form-urlencoded"); 
			post.setHeader("charset", "utf-8");
			StringEntity body = new StringEntity(JsonUtil.toJson(data));
			post.setEntity(body);

			HttpResponse response = client.execute(post);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			String responseText = result.toString();
			return new TestResponse(response.getStatusLine().getStatusCode(), responseText);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Sending request failed: " + e.getMessage());
			return null;
		}
	}
	
	public static TestResponse getRequest(String url) {
		try {
			HttpClient client =  HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(url);
			get.setHeader("Content-Type", "application/x-www-form-urlencoded"); 
			get.setHeader("charset", "utf-8");

			HttpResponse response = client.execute(get);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			String responseText = result.toString();
			return new TestResponse(response.getStatusLine().getStatusCode(), responseText);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Sending request failed: " + e.getMessage());
			return null;
		}
	}
	
	public static TestResponse postMultiPartRequest(String url, HashMap<String, Object> multipartData) {
		try {
			HttpClient httpclient = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(url);
			
			MultipartEntity reqEntity = new MultipartEntity();
			for(String key : multipartData.keySet()) {
				Object value = multipartData.get(key);
				if(value instanceof String) {
					reqEntity.addPart(key, new StringBody((String) value));
				} else if(value instanceof File) {
					reqEntity.addPart(key, new FileBody((File) value));
				}
			}
			httpPost.setEntity(reqEntity);
			
			HttpResponse resp = httpclient.execute(httpPost);
				
			BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			String responseText = result.toString();
			return new TestResponse(resp.getStatusLine().getStatusCode(), responseText);
		} catch (ClientProtocolException e) {
			fail("Sending request failed: " + e.getMessage());
			return null;
		} catch (IOException e) {
			fail("Sending request failed: " + e.getMessage());
			return null;
		}
	}
	
	public static class TestResponse {
		
		public final String body;
		public final int status;

		public TestResponse(int status, String body) {
			this.status = status;
			this.body = body;
		}
	}
}

