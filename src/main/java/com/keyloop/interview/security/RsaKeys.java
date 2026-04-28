package com.keyloop.interview.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RsaKeys {

	private final RSAPrivateKey privateKey;
	private final RSAPublicKey publicKey;

	public static RsaKeys generate() throws Exception {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair pair = gen.generateKeyPair();
		return new RsaKeys((RSAPrivateKey) pair.getPrivate(), (RSAPublicKey) pair.getPublic());
	}
}
