package com.example.spark.test.slidealbums;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class SlideAlbumsMgr {

	private String workspacesDir;
	
	public SlideAlbumsMgr() {
		workspacesDir = "D:/xampp-7/htdocs/workspaces";
	}
	
	public List<SlideAlbum> getSlideAlbums(List<String> customers) {	
		List<SlideAlbum> slideAlbums = new ArrayList<SlideAlbum>();
		for(String customer : customers) {
			File customerDir = new File(workspacesDir + "/" + customer);
			File[] slideAlbumDirs = customerDir.listFiles() != null ? customerDir.listFiles() : new File[0];
			for(File slideAlbumDir : slideAlbumDirs) {
				if(slideAlbumDir.isDirectory()) {	
					SlideAlbum.Builder slideAlbumBuilder = new SlideAlbum.Builder(slideAlbumDir.getName(), customer);
					slideAlbumBuilder = slideAlbumBuilder.modificationDate(slideAlbumDir.lastModified());
					File[] slideAlbumFiles = slideAlbumDir.listFiles();
					List<SlideAlbumFile> files = new ArrayList<SlideAlbumFile>();
					for(File file : slideAlbumFiles) {
						String ext = FilenameUtils.getExtension(file.getName());
						String name = FilenameUtils.getBaseName(file.getName());
						if(ext.equals("txt")) {
							String lockedByUsr = name.split("_")[name.split("_").length-1];
							slideAlbumBuilder = slideAlbumBuilder.lockedBy(lockedByUsr);
						} else {
							files.add(new SlideAlbumFile(ext, name));
							if(ext.equals("svg")) {
								slideAlbumBuilder = slideAlbumBuilder.svg(name);
							}
						}
					}
					slideAlbumBuilder = slideAlbumBuilder.files(files);
					slideAlbums.add(slideAlbumBuilder.build());
				}
			}
		}
		return slideAlbums;
	}
	
	// for test purposes
	public static void main(String[] args) {
		System.out.println(new SlideAlbumsMgr().getSlideAlbums(Arrays.asList("Bosch", "Harley Davidson")));
	}
}
