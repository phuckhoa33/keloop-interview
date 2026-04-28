package com.keyloop.interview.customer.rest;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keyloop.interview.common.exception.ResourceNotFoundException;
import com.keyloop.interview.customer.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerRestController {

	private final CustomerRepository customers;

	@GetMapping("/me")
	@PreAuthorize("hasRole('CUSTOMER')")
	public CustomerMeResponse me(Authentication authentication) {
		UUID userId = UUID.fromString(authentication.getName());
		var customer = customers.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile"));
		return new CustomerMeResponse(customer.getId(), customer.getEmail(), customer.getFirstName(),
				customer.getLastName(), customer.getPhone());
	}

	public record CustomerMeResponse(UUID id, String email, String firstName, String lastName, String phone) {
	}

}
