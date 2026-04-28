package com.keyloop.interview.customer.rest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keyloop.interview.common.exception.DomainException;
import com.keyloop.interview.common.exception.ResourceNotFoundException;
import com.keyloop.interview.customer.domain.Customer;
import com.keyloop.interview.customer.domain.Vehicle;
import com.keyloop.interview.customer.dto.VehiclesDto.VehicleResponse;
import com.keyloop.interview.customer.dto.VehiclesDto.VehicleUpsert;
import com.keyloop.interview.customer.repository.CustomerRepository;
import com.keyloop.interview.customer.repository.VehicleRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleRestController {

	private final VehicleRepository vehicles;
	private final CustomerRepository customers;

	@GetMapping
	@PreAuthorize("hasRole('CUSTOMER')")
	public List<VehicleResponse> mine(Authentication authentication) {
		UUID userId = UUID.fromString(authentication.getName());
		Customer c = customers.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile"));
		return vehicles.findByCustomerId(c.getId()).stream().map(VehicleRestController::toResponse).toList();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public VehicleResponse one(@PathVariable UUID id, Authentication authentication) {
		UUID userId = UUID.fromString(authentication.getName());
		Customer c = customers.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile"));
		Vehicle v = vehicles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle"));
		if (!v.getCustomer().getId().equals(c.getId())) {
			throw new DomainException("FORBIDDEN", "Not your vehicle.");
		}
		return toResponse(v);
	}

	@PostMapping
	@PreAuthorize("hasRole('CUSTOMER')")
	public VehicleResponse create(@Valid @RequestBody VehicleUpsert body, Authentication authentication) {
		UUID userId = UUID.fromString(authentication.getName());
		Customer c = customers.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile"));
		if (vehicles.findByVin(body.vin()).isPresent()) {
			throw new DomainException("VIN_EXISTS", "VIN already registered.");
		}
		Instant now = Instant.now();
		Vehicle v = Vehicle.builder().id(UUID.randomUUID()).customer(c).vin(body.vin()).make(body.make())
				.model(body.model()).year(body.year()).licensePlate(body.licensePlate()).color(body.color())
				.mileage(body.mileage()).createdAt(now).updatedAt(now).build();
		return toResponse(vehicles.save(v));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public VehicleResponse update(@PathVariable UUID id, @Valid @RequestBody VehicleUpsert body,
			Authentication authentication) {
		UUID userId = UUID.fromString(authentication.getName());
		Customer c = customers.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile"));
		Vehicle v = vehicles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle"));
		if (!v.getCustomer().getId().equals(c.getId())) {
			throw new DomainException("FORBIDDEN", "Not your vehicle.");
		}
		v.setMake(body.make());
		v.setModel(body.model());
		v.setYear(body.year());
		v.setLicensePlate(body.licensePlate());
		v.setColor(body.color());
		v.setMileage(body.mileage());
		v.setVin(body.vin());
		v.setUpdatedAt(Instant.now());
		return toResponse(vehicles.save(v));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public void delete(@PathVariable UUID id, Authentication authentication) {
		UUID userId = UUID.fromString(authentication.getName());
		Customer c = customers.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile"));
		Vehicle v = vehicles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle"));
		if (!v.getCustomer().getId().equals(c.getId())) {
			throw new DomainException("FORBIDDEN", "Not your vehicle.");
		}
		vehicles.deleteById(id);
	}

	private static VehicleResponse toResponse(Vehicle v) {
		return new VehicleResponse(v.getId(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getLicensePlate(),
				v.getColor(), v.getMileage());
	}
}
