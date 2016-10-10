package com.example.spark.test.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

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
	
	public static class TestResponse {
		
		public final String body;
		public final int status;

		public TestResponse(int status, String body) {
			this.status = status;
			this.body = body;
		}
	}
	
	public static String constructURL(String scheme, String host, int port, String path) {
		String urlStr = null;
		try {
			URIBuilder uriBuilder = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).setPath(path);
			URI uri = uriBuilder.build();			
			URL url = uri.toURL();
			urlStr = url.toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return urlStr;
	}
}

