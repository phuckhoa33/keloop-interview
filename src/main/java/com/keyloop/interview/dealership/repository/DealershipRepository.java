package com.keyloop.interview.dealership.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keyloop.interview.dealership.domain.Dealership;

public interface DealershipRepository extends JpaRepository<Dealership, UUID> {
}
