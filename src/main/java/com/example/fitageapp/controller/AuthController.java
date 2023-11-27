package com.bank.trustworthybank.controller;

import com.bank.trustworthybank.common.ApiResponse;
import com.bank.trustworthybank.data.dto.request.LoginRequest;
import com.bank.trustworthybank.data.dto.request.RegistrationRequest;
import com.bank.trustworthybank.service.AuthenticationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {
	
	private final AuthenticationService service;
	
	@PostMapping("/registration")
	public ResponseEntity<ApiResponse> register(@RequestBody RegistrationRequest request) throws MessagingException {
		var response = service.register (request);
		return ResponseEntity.status (HttpStatus.CREATED).body (response);
	}
	
	@PostMapping("/login")
	public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
		var response = service.login (request);
		return ResponseEntity.status (HttpStatus.OK).body (response);
	}
}
