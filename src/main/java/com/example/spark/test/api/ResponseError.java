package com.example.spark.test.api;

/**
 * Response Error class.
 * @author Elitza Haltakova
 *
 */
public class ResponseError {
	
	private String message;

	public ResponseError(String message, String... args) {
		this.message = String.format(message, (Object[])args);
	}

	public ResponseError(Exception e) {
		this.message = e.getMessage();
	}

	public String getMessage() {
		return this.message;
	}
}