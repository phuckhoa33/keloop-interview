package com.keyloop.interview.customer.rest;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.keyloop.interview.customer.dto.VehiclesDto.OwnershipCheck;
import com.keyloop.interview.customer.service.VehicleOwnershipService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/vehicles")
@RequiredArgsConstructor
public class VehicleInternalRestController {

	private final VehicleOwnershipService ownership;

	@GetMapping("/{vehicleId}/owner-check")
	public OwnershipCheck check(@PathVariable UUID vehicleId, @RequestParam UUID customerId) {
		boolean owned = ownership.owns(vehicleId, customerId);
		return new OwnershipCheck(owned);
	}

}
