package com.keyloop.interview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	private String issuer = "https://keyloop-interview.local";

	private long accessTokenTtlMinutes = 15;

	private long refreshTokenTtlDays = 7;

	/** PEM path; empty generates ephemeral RSA at startup (dev). */
	private String privateKeyPath = "";

	private String publicKeyPath = "";

	private String keyId = "scheduler-key-1";
}
