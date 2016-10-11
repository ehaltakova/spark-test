package com.example.spark.test.slidealbums;

public class SlideAlbumFile {
	String ext;
	String name;
	
	public SlideAlbumFile(String extension, String name) {
		this.ext = extension;
		this.name = name;
	}

	public String getExt() {
		return ext;
	}

	public String getName() {
		return name;
	}
}