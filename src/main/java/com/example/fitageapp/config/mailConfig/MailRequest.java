package com.bank.trustworthybank.config.javaMail;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailRequest {
	private final String from = "noreply@TWBank.com";
	private String to;
	private  String message;
	private final String subject = "Trust worthy Bank";
}
