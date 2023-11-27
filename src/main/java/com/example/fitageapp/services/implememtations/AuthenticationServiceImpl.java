package com.bank.trustworthybank.service;

import com.bank.trustworthybank.common.ApiResponse;
import com.bank.trustworthybank.common.Message;
import com.bank.trustworthybank.config.app.BeanConfig;
import com.bank.trustworthybank.config.javaMail.JavaMailSenderService;
import com.bank.trustworthybank.config.javaMail.MailRequest;
import com.bank.trustworthybank.config.security.jwt.JwtGenerator;
import com.bank.trustworthybank.config.security.user.SecureUser;
import com.bank.trustworthybank.data.dto.request.LoginRequest;
import com.bank.trustworthybank.data.dto.request.RegistrationRequest;
import com.bank.trustworthybank.data.model.*;
import com.bank.trustworthybank.data.repository.AccountUserRepository;
import com.bank.trustworthybank.data.repository.SecurityQuestionRepository;
import com.bank.trustworthybank.exceptions.AlreadyExistsException;
import com.bank.trustworthybank.exceptions.InvalidLoginDetailsException;
import com.bank.trustworthybank.utils.AppUserUtils;
import com.bank.trustworthybank.utils.EncryptionUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.bank.trustworthybank.utils.AppUserUtils.INITIAL_BALANCE;
import static com.bank.trustworthybank.utils.AppUserUtils.generateAccountNumber;
import static com.bank.trustworthybank.utils.ResponseUtils.*;

@BeanConfig
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService{
	
	private final AuthenticationManager authenticationManager;
	private final JwtGenerator jwtService;
	private final PasswordEncoder passwordEncoder;
	private final AppUserService userService;
	private final SecurityDetailService securityDetailService;
	private final EncryptionUtils encryptionUtils;
	private final AccountUserRepository repository;
	private final AppUserUtils utils;
	private final JavaMailSenderService senderService;
	private final SecurityQuestionRepository securityQuestionRepository;
	
	@Override
	public ApiResponse register(RegistrationRequest registrationRequest) throws MessagingException {
		if (userService.findUserByEmail(registrationRequest.getEmail()) != null) {
			throw new AlreadyExistsException ("User with " + registrationRequest.getEmail() + " already exist");
		}
		AppUser savedUser = saveUser(registrationRequest);
		var saved = savedAccountUser(savedUser);
		MailRequest mailRequest = sendMail(savedUser, saved);
		var response = senderService.sendMail (mailRequest);
		String jwt = jwtService.generateToken(new SecureUser (savedUser));
		saveToken(jwt, savedUser);
		
		if (response == null) {
			return getFailureResponse ("Failed").getBody ();
		}
		return getCreatedResponse("Bearer " + jwt).getBody ();
	}

	@Override
	public ApiResponse login(LoginRequest loginRequest){
		authenticateUser(loginRequest);
		AppUser foundUser = userService.findUserByEmail(loginRequest.getEmail());
		if (foundUser != null) {
			String jwt = jwtService.generateToken(new SecureUser (foundUser));
			revokeAllUserToken(foundUser.getId());
			saveToken(jwt, foundUser);
			return getOkResponse("Bearer " + jwt).getBody ();
		}
		throw new InvalidLoginDetailsException (Message.LOGIN_FAILED, HttpStatus.BAD_REQUEST);
	}

	private MailRequest sendMail(AppUser savedUser, AccountUser saved) {
		return utils.buildAccCreation(
				savedUser.getEmail(),
				savedUser.getFirstname(),
				savedUser.getLastname(),
				saved.getAccountNumber(),
				saved.getBalance()
		);
	}

	@NotNull
	private AccountUser savedAccountUser(AppUser savedUser) {
		var accountUser = AccountUser.builder()
				.userDetails(savedUser)
				.accountNumber(generateAccountNumber())
				.level(AccountLevel.TIER_ONE)
				.balance(INITIAL_BALANCE)
				.build();
		return repository.save(accountUser);
	}


	private AppUser saveUser(RegistrationRequest registrationRequest) {
		List<Role> userRoles = new ArrayList<>();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		LocalDate birthDay = LocalDate.from(LocalDate.parse(registrationRequest.getBirthDate (),dateTimeFormatter).atStartOfDay ());
		userRoles.add(Role.USER);
		SecurityQuestion createSecurity = new SecurityQuestion();
		createSecurity.setQuestions(registrationRequest.getQuestion());
		createSecurity.setAnswer(registrationRequest.getAnswer());
		SecurityQuestion savedQuestion = securityQuestionRepository.save(createSecurity);
		AppUser user =  AppUser.builder()
				.email (registrationRequest.getEmail())
				.password(passwordEncoder.encode(registrationRequest.getPassword()))
				.role(userRoles)
				.firstname (registrationRequest.getFirstname ())
				.lastname (registrationRequest.getLastname ())
				.phoneNumber (registrationRequest.getPhoneNumber ())
				.questions(savedQuestion)
				.birthdate (birthDay)
				.age (calculateAge (LocalDate.parse (registrationRequest.getBirthDate (), dateTimeFormatter), LocalDate.now ()))
				.createdAt (LocalDateTime.now ())
				.enabled (true)
				.build();
		return userService.saveUser(user);
	}
	
	private static int calculateAge(LocalDate birthDate, LocalDate currentDate) {
		Period period = Period.between(birthDate, currentDate);
		return period.getYears();
	}
	
	private void saveToken(String jwt, AppUser user) {
		SecurityDetail securityDetail = SecurityDetail.builder()
				.token(encryptionUtils.encrypt(jwt))
				.isExpired(false)
				.isRevoked(false)
				.user(user)
				.build();
		securityDetailService.save(securityDetail);
	}
	
	private void revokeAllUserToken(Long userId) {
		var allUsersToken =  securityDetailService.findSecurityDetailByUserId(userId);
		
		if (allUsersToken.isEmpty())return;
		allUsersToken
				.forEach(securityDetail -> {
					securityDetail.setRevoked(true);
					securityDetail.setExpired(true);
					securityDetailService.save(securityDetail);
				});
	}
	
	private void authenticateUser(LoginRequest loginRequest) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken (
				loginRequest.getEmail(),
				loginRequest.getPassword()
		));
	}
}
