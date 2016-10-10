package com.example.spark.test.util;

import static spark.Spark.staticFiles;

import java.io.File;

public class Util {

	public static String uploadDirPath = "upload";
			
	public static File configureUploadFilesDir() {
		File uploadDir = new File("upload");
	    uploadDir.mkdir(); // create the upload directory if it doesn't exist
	    staticFiles.externalLocation("upload");
	    uploadDirPath = uploadDir.getPath();
	    return uploadDir;
	}

}
