package com.keyloop.interview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private final Internal internal = new Internal();

	@Getter
	@Setter
	public static class Internal {
		private String secret = "dev-internal-secret-change-me";
	}
}
