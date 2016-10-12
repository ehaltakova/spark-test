package com.example.spark.test.api;

import com.example.spark.test.util.JsonUtil;
import spark.ExceptionHandler;

/**
 * Exception handlers
 * @author Elitza Haltakova
 *
 */
public class ExceptionHandlers {

	/**
	 * Handle unchecked Exceptions (NullPointerException, IllegalArgumentException, ClassCastException, etc.)
	 */
	public static ExceptionHandler uncheckedExceptions = (e, request, response) -> {
		API.logger.error(e.getMessage(), e);
		response.status(500);
		response.body(JsonUtil.toJson(new ResponseError("An internal error occured. Please, contact your system administrator.").getMessage()));
	};
}
