package com.bank.trustworthybank.utils;

import com.bank.trustworthybank.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtils {
	public static ResponseEntity<ApiResponse> getCreatedResponse(Object data) {
		var response = ApiResponse.builder ()
				.data (data)
				.httpStatus (HttpStatus.CREATED)
				.isSuccessful (true)
				.message ("Created successfully")
				.build ();
		return ResponseEntity.ok (response);
	}
	public static ResponseEntity<ApiResponse> getOkResponse(Object data) {
		var response = ApiResponse.builder ()
				.data (data)
				.httpStatus (HttpStatus.OK)
				.isSuccessful (true)
				.message ("Ok")
				.build ();
		return ResponseEntity.ok (response);
	}
	public static ResponseEntity<ApiResponse> getJustOkResponse() {
		var response = ApiResponse.builder ()
				.httpStatus (HttpStatus.OK)
				.isSuccessful (true)
				.message ("Ok")
				.build ();
		return ResponseEntity.ok (response);
	}
	
	public static ResponseEntity<ApiResponse> getFailureResponse(Object data) {
		var response = ApiResponse.builder ()
				.data (data)
				.httpStatus (HttpStatus.BAD_GATEWAY)
				.isSuccessful (true)
				.message ("Failed")
				.build ();
		return ResponseEntity.ok (response);
	}
	
}
