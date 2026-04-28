package com.keyloop.interview.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.keyloop.interview.config.JwtProperties;

@Configuration(proxyBeanMethods = false)
public class JwtBlacklistAwareDecoder {

	@Bean
	@Primary
	public JwtDecoder jwtDecoder(JwtTokenProvider jwtTokenProvider,
			JwtBlacklist blacklist,
			JwtProperties jwtProperties) {
		NimbusJwtDecoder delegate = NimbusJwtDecoder.withPublicKey(jwtTokenProvider.verifyingPublicKey()).build();
		delegate.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.getIssuer()));
		return token -> {
			Jwt j = delegate.decode(token);
			String jwtId = j.getId();
			if (jwtId != null && blacklist.isListed(jwtId)) {
				throw new BadJwtException("Token revoked");
			}
			return j;
		};
	}
}
