package com.keyloop.interview.dealership.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keyloop.interview.appointment.domain.AppointmentStatus;
import com.keyloop.interview.appointment.repository.AppointmentRepository;
import com.keyloop.interview.common.exception.ResourceNotFoundException;
import com.keyloop.interview.dealership.domain.Dealership;
import com.keyloop.interview.dealership.domain.ServiceBay;
import com.keyloop.interview.dealership.domain.ServiceType;
import com.keyloop.interview.dealership.domain.Technician;
import com.keyloop.interview.dealership.domain.TechnicianSchedule;
import com.keyloop.interview.dealership.repository.BlockedSlotRepository;
import com.keyloop.interview.dealership.repository.DealershipRepository;
import com.keyloop.interview.dealership.repository.ServiceBayRepository;
import com.keyloop.interview.dealership.repository.ServiceTypeRepository;
import com.keyloop.interview.dealership.repository.TechnicianRepository;
import com.keyloop.interview.dealership.repository.TechnicianScheduleRepository;
import com.keyloop.interview.dealership.web.dto.AvailableSlotSuggestion;

@Service
public class AvailabilityService {

	private final AppointmentRepository appointments;
	private final ServiceBayRepository bays;
	private final TechnicianRepository technicians;
	private final TechnicianScheduleRepository schedules;
	private final BlockedSlotRepository blockedSlots;
	private final DealershipRepository dealerships;
	private final ServiceTypeRepository serviceTypes;

	private static final AppointmentStatus IGNORE = AppointmentStatus.CANCELLED;

	public AvailabilityService(AppointmentRepository appointments, ServiceBayRepository bays,
			TechnicianRepository technicians, TechnicianScheduleRepository schedules,
			BlockedSlotRepository blockedSlots,
			DealershipRepository dealerships,
			ServiceTypeRepository serviceTypes) {
		this.appointments = appointments;
		this.bays = bays;
		this.technicians = technicians;
		this.schedules = schedules;
		this.blockedSlots = blockedSlots;
		this.dealerships = dealerships;
		this.serviceTypes = serviceTypes;
	}

	@Transactional(readOnly = true)
	public Optional<ServiceBay> findAvailableBay(UUID dealershipId, Instant start, Instant end, String bayType) {
		List<ServiceBay> candidates = bays.findByDealership_IdAndBayTypeAndActiveTrue(dealershipId, bayType);
		candidates.sort(Comparator.comparing(ServiceBay::getBayNumber));
		for (ServiceBay b : candidates) {
			if (!blockedSlots.overlapping("BAY", b.getId(), start, end).isEmpty())
				continue;
			if (!appointments.overlapsBay(b.getId(), start, end, IGNORE))
				return Optional.of(b);
		}
		return Optional.empty();
	}

	@Transactional(readOnly = true)
	public Optional<Technician> findAvailableTechnician(UUID dealershipId, Instant start, Instant end,
			String requiredSkill) {

		ZonedDateTime zStart = start.atZone(ZoneId.of(dealershipZone(dealershipId)));
		int dow = mondayZeroWeekday(zStart);
		LocalTime tStart = zStart.toLocalTime();
		LocalTime tEnd = end.atZone(ZoneId.of(dealershipZone(dealershipId))).toLocalTime();

		List<Technician> all = technicians.findByDealershipIdAndActiveTrue(dealershipId);
		all.sort(Comparator.comparing(Technician::getLastName).thenComparing(Technician::getFirstName));

		for (Technician tech : all) {
			if (requiredSkill != null && !requiredSkill.isBlank()) {
				if (tech.getSkills() == null || !tech.getSkills().contains(requiredSkill))
					continue;
			}
			List<TechnicianSchedule> day = schedules.findByTechnician_IdAndDayOfWeek(tech.getId(), dow);
			if (!covers(day, tStart, tEnd))
				continue;
			if (!blockedSlots.overlapping("TECHNICIAN", tech.getId(), start, end).isEmpty())
				continue;
			if (!appointments.overlapsTechnician(tech.getId(), start, end, IGNORE))
				return Optional.of(tech);
		}
		return Optional.empty();
	}

	private String dealershipZone(UUID dealershipId) {
		return dealerships.findById(dealershipId).map(Dealership::getTimezone).orElse("UTC");
	}

	private static int mondayZeroWeekday(ZonedDateTime z) {
		return switch (z.getDayOfWeek()) {
			case MONDAY -> 0;
			case TUESDAY -> 1;
			case WEDNESDAY -> 2;
			case THURSDAY -> 3;
			case FRIDAY -> 4;
			case SATURDAY -> 5;
			case SUNDAY -> 6;
		};
	}

	private static boolean covers(List<TechnicianSchedule> daySlots, LocalTime start, LocalTime end) {
		if (daySlots.isEmpty())
			return false;
		for (TechnicianSchedule s : daySlots) {
			if (!start.isBefore(s.getStartTime()) && !end.isAfter(s.getEndTime()))
				return true;
		}
		return false;
	}

	@Transactional(readOnly = true)
	public List<AvailableSlotSuggestion> suggestSlots(UUID dealershipId, UUID serviceTypeId, LocalDate fromDate,
			int slotCount) {

		ServiceType serviceType = serviceTypes.findById(serviceTypeId)
				.orElseThrow(() -> new ResourceNotFoundException("Service type not found"));
		if (!serviceType.isActive())
			return List.of();

		List<AvailableSlotSuggestion> out = new ArrayList<>();
		ZoneId zoneId = ZoneId.of(dealershipZone(dealershipId));
		int duration = serviceType.getDurationMinutes();

		outer: for (LocalDate date = fromDate; date.isBefore(fromDate.plusDays(14)); date = date.plusDays(1)) {
			for (int h = 8; h <= 17 && out.size() < slotCount; h++) {
				Instant slotStart = ZonedDateTime.of(date, LocalTime.of(h, 0), zoneId).toInstant();
				Instant slotEnd = slotStart.plusSeconds(duration * 60L);

				Optional<ServiceBay> bay = findAvailableBay(dealershipId, slotStart, slotEnd, "STANDARD");
				if (bay.isEmpty())
					bay = findAvailableBay(dealershipId, slotStart, slotEnd, "LIFT");
				if (bay.isEmpty())
					continue;

				Optional<Technician> tech = findAvailableTechnician(dealershipId, slotStart, slotEnd,
						serviceType.getRequiredSkill());
				if (tech.isEmpty())
					continue;

				ServiceBay b = bay.get();
				Technician t = tech.get();

				out.add(new AvailableSlotSuggestion(slotStart, slotEnd, t.getId(),
						t.getFirstName() + " " + t.getLastName(), b.getId(), b.getBayNumber()));

				if (out.size() >= slotCount)
					break outer;
			}
		}

		return out;
	}

}
