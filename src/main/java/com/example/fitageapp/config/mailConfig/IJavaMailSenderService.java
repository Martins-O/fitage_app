package com.bank.trustworthybank.config.javaMail;


import jakarta.mail.MessagingException;

public interface JavaMailSenderService {
	String sendMail(MailRequest request) throws MessagingException;
}
