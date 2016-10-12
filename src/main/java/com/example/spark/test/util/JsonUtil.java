package com.example.spark.test.util;

import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * JSON util class using GSON library to manipulate JSOO data.
 * @author Elitza Haltakova
 *
 */
public class JsonUtil {
		
	public static HashMap<String, Object> fromJson(String json) {
		Type collectionType = new TypeToken<HashMap<String, Object>>(){}.getType();
		HashMap<String, Object> data = new Gson().fromJson(json, collectionType);
		if(data == null)
			data = new HashMap<String, Object>();
		return data;
	}
	
	public static <T> T fromJsonToClass(String json, Class<T> objClass) {
		T data = new Gson().fromJson(json, objClass);
		return data;
	}
	
	public static <T> T fromJsonElementToClass(JsonElement json, Class<T> objClass) {
		T data = new Gson().fromJson(json, objClass);
		return data;
	}
	
	public static <T> T fromJsonToType(String json, Type objType) {
		T data = new Gson().fromJson(json, objType);
		return data;
	}
	
	public static String toJson(Object data) {
		return new Gson().toJson(data);
	}

}
