package com.bank.trustworthybank.service;

import com.bank.trustworthybank.common.ApiResponse;
import com.bank.trustworthybank.data.dto.request.LoginRequest;
import com.bank.trustworthybank.data.dto.request.RegistrationRequest;
import jakarta.mail.MessagingException;

public interface AuthenticationService {
	ApiResponse register(RegistrationRequest registrationRequest) throws MessagingException;
	
	ApiResponse login(LoginRequest loginRequest);
}
