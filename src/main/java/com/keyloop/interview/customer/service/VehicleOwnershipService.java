package com.keyloop.interview.customer.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.keyloop.interview.customer.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleOwnershipService {

	private final VehicleRepository vehicles;

	public boolean owns(UUID vehicleId, UUID customerId) {
		return vehicles.existsByIdAndCustomerId(vehicleId, customerId);
	}
}
