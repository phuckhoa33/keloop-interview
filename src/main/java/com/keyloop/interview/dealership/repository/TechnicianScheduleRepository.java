package com.keyloop.interview.dealership.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keyloop.interview.dealership.domain.TechnicianSchedule;

public interface TechnicianScheduleRepository extends JpaRepository<TechnicianSchedule, UUID> {

	List<TechnicianSchedule> findByTechnician_IdAndDayOfWeek(UUID technicianId, int dayOfWeek);
}
