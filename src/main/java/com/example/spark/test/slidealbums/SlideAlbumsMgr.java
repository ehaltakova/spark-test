package com.example.spark.test.slidealbums;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.example.spark.test.slidealbums.SlideAlbum.Builder;
import com.example.spark.test.util.Util;

public class SlideAlbumsMgr {

	private String workspacesDir;
	
	public SlideAlbumsMgr() {
		workspacesDir = "D:/xampp-7/htdocs/workspaces"; // TOOD: get from config
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
	
	public SlideAlbum getSlideAlbum(String title, String customer) {
		
		SlideAlbum slideAlbum = null;
		String pathToSlideAlbumDir = workspacesDir + "/" + customer + "/" + title;
		File slideAlbumDir = new File(pathToSlideAlbumDir);
		if(slideAlbumDir.exists()) {
			SlideAlbum.Builder builder = new Builder(title, customer);
			builder = builder.modificationDate(slideAlbumDir.lastModified());
			File[] files = slideAlbumDir.listFiles();
			List<SlideAlbumFile> slideAlbumFiles = new ArrayList<SlideAlbumFile>();
			for(File file : files) {
				String ext = FilenameUtils.getExtension(file.getName());
				String name = FilenameUtils.getBaseName(file.getName());
				if(ext.equals("txt")) {
					String lockedByUsr = name.split("_")[name.split("_").length-1];
					builder = builder.lockedBy(lockedByUsr);
				} else {
					slideAlbumFiles.add(new SlideAlbumFile(ext, name));
					if(ext.equals("svg")) {
						builder = builder.svg(name);
					}
				}
			}
			builder = builder.files(slideAlbumFiles);
			slideAlbum = builder.build();
		} 
		return slideAlbum;
	}
	
	public SlideAlbum createSlideAlbum(String title, String customer, String fileName) {

		SlideAlbum slideAlbum = null;
		
		File customerDir = new File(this.workspacesDir + "/" + customer);
		if(!customerDir.exists()) {
			customerDir.mkdir();
		}
		File slideAlbumDir = new File(this.workspacesDir + "/" + customer + "/" + title);
		slideAlbumDir.mkdir();
		File file = new File(Util.uploadDirPath + "/" + fileName);
		file.renameTo(new File(slideAlbumDir.getPath() + "/" + fileName));
		
		SlideAlbum.Builder builder = new Builder(title, customer).modificationDate(slideAlbumDir.lastModified()).svg(FilenameUtils.getBaseName(fileName));
		List<SlideAlbumFile> files = new ArrayList<SlideAlbumFile>(); 
		files.add(new SlideAlbumFile("svg", FilenameUtils.getBaseName(fileName)));
		slideAlbum = builder.files(files).build();
		
		return slideAlbum;
	}
	
	// for test purposes
	public static void main(String[] args) {
		System.out.println(new SlideAlbumsMgr().getSlideAlbums(Arrays.asList("Bosch", "Harley Davidson")));
		System.out.println(new SlideAlbumsMgr().getSlideAlbum("AC 2", "Bosch"));
		System.out.println(new SlideAlbumsMgr().createSlideAlbum("Eli test 1234567", "Bosch", "Central Locking_01.svg"));
		
	}

}
