package com.keyloop.interview.dealership.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keyloop.interview.dealership.domain.Technician;

public interface TechnicianRepository extends JpaRepository<Technician, UUID> {

	List<Technician> findByDealershipIdAndActiveTrue(UUID dealershipId);
}
