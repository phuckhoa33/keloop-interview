package com.keyloop.interview.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration(proxyBeanMethods = false)
public class SecurityBeansConfig {

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter jac = new JwtAuthenticationConverter();
		jac.setJwtGrantedAuthoritiesConverter(new RolesClaimConverter());
		return jac;
	}

	static final class RolesClaimConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

		@Override
		public Collection<GrantedAuthority> convert(Jwt jwt) {
			List<String> roles = jwt.getClaimAsStringList("roles");
			if (roles == null)
				return List.of();
			return roles.stream()
					.map(r -> r.startsWith("ROLE_") ? r : ("ROLE_" + r))
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
		}
	}

}
