package com.example.spark.test.slidealbums;

/**
 * Slide Album File model class
 * @author Elitza Haltakova
 *
 */
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