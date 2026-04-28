package com.keyloop.interview.dealership.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keyloop.interview.dealership.domain.ServiceBay;

public interface ServiceBayRepository extends JpaRepository<ServiceBay, UUID> {

	List<ServiceBay> findByDealership_IdAndBayTypeAndActiveTrue(UUID dealershipId, String bayType);
}
