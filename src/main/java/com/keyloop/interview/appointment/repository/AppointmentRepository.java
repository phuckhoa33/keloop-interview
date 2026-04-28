package com.keyloop.interview.appointment.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.keyloop.interview.appointment.domain.Appointment;
import com.keyloop.interview.appointment.domain.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

	Optional<Appointment> findByBookingReference(String ref);

	boolean existsByServiceBayIdAndStatusNotAndStartTimeBeforeAndEndTimeAfter(UUID bayId,
			AppointmentStatus cancelled, Instant end, Instant start);

	boolean existsByTechnicianIdAndStatusNotAndStartTimeBeforeAndEndTimeAfter(UUID techId,
			AppointmentStatus cancelled, Instant end, Instant start);

	@Query("""
			select case when count(a) > 0 then true else false end from Appointment a where a.serviceBayId = :bay
			and a.status <> :ignore and ((a.startTime < :end) and (a.endTime > :start))
			""")
	boolean overlapsBay(@Param("bay") UUID bay, @Param("start") Instant start, @Param("end") Instant end,
			@Param("ignore") AppointmentStatus ignore);

	@Query("""
			select case when count(a) > 0 then true else false end from Appointment a where a.technicianId = :tech
			and a.status <> :ignore and ((a.startTime < :end) and (a.endTime > :start))
			""")
	boolean overlapsTechnician(@Param("tech") UUID tech, @Param("start") Instant start, @Param("end") Instant end,
			@Param("ignore") AppointmentStatus ignore);

	List<Appointment> findByCustomerIdOrderByStartTimeDesc(UUID customerId);

	List<Appointment> findByDealershipIdOrderByStartTimeDesc(UUID dealershipId);

	List<Appointment> findAllByOrderByStartTimeDesc();
}
