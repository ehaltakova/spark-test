package com.example.spark.test.util;

import static spark.Spark.staticFiles;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.utils.URIBuilder;

public class Util {

	public static String uploadDirPath = "upload";
			
	public static File configureUploadFilesDir() {
		File uploadDir = new File("upload");
	    uploadDir.mkdir(); // create the upload directory if it doesn't exist
	    staticFiles.externalLocation("upload");
	    uploadDirPath = uploadDir.getPath();
	    return uploadDir;
	}

	public static String constructURL(String scheme, String host, int port, String path) {
		String urlStr = null;
		try {
			URIBuilder uriBuilder = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).setPath(path);
			URI uri = uriBuilder.build();
			URL url = uri.toURL();
			urlStr = url.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return urlStr;
	}
}
