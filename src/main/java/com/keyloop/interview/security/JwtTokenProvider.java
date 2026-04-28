package com.keyloop.interview.security;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.keyloop.interview.auth.domain.UserRole;
import com.keyloop.interview.config.JwtProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;

	private final RSASSASigner signer;
	private final RSAPublicKey publicKey;

	JwtTokenProvider(JwtProperties jwtProperties) throws Exception {
		this.jwtProperties = jwtProperties;
		RsaKeys keys = RsaKeys.generate();
		this.publicKey = keys.getPublicKey();
		this.signer = new RSASSASigner(keys.getPrivateKey());
	}

	public SignedJWT mintAccessJwt(UUID subject, String email, UserRole role, UUID dealershipId) throws JOSEException {
		Date now = new Date();
		Date exp = Date.from(Instant.now().plus(jwtProperties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES));
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject(subject.toString())
				.issuer(jwtProperties.getIssuer())
				.issueTime(now)
				.expirationTime(exp)
				.jwtID(UUID.randomUUID().toString())
				.claim("email", email)
				.claim("roles", List.of(UserRole.toAuthority(role)))
				.claim("dealership_id", dealershipId == null ? null : dealershipId.toString())
				.build();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwtProperties.getKeyId()).type(JOSEObjectType.JWT)
				.build();
		SignedJWT jwt = new SignedJWT(header, claims);
		jwt.sign(signer);
		return jwt;
	}

	public RSAPublicKey verifyingPublicKey() {
		return publicKey;
	}

	public Map<String, Object> jwkSetDocument() {
		RSAKey rsa = new RSAKey.Builder(publicKey).keyID(jwtProperties.getKeyId()).build();
		JWKSet set = new JWKSet(rsa);
		return set.toJSONObject();
	}
}
