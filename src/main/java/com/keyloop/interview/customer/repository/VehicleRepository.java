package com.keyloop.interview.customer.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keyloop.interview.customer.domain.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

	Optional<Vehicle> findByVin(String vin);

	List<Vehicle> findByCustomerId(UUID customerId);

	boolean existsByIdAndCustomerId(UUID id, UUID customerId);
}
