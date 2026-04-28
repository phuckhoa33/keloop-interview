package com.keyloop.interview.dealership.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keyloop.interview.dealership.domain.AppointmentSlot;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {
}
