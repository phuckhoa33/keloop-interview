package com.keyloop.interview.appointment.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keyloop.interview.appointment.domain.Appointment;
import com.keyloop.interview.appointment.domain.AppointmentStatus;
import com.keyloop.interview.appointment.dto.AppointmentDtos.AppointmentResponse;
import com.keyloop.interview.appointment.dto.AppointmentDtos.BaySummary;
import com.keyloop.interview.appointment.dto.AppointmentDtos.BookingRequest;
import com.keyloop.interview.appointment.dto.AppointmentDtos.DealershipSummary;
import com.keyloop.interview.appointment.dto.AppointmentDtos.PersonSummary;
import com.keyloop.interview.appointment.dto.AppointmentDtos.ServiceTypeSummary;
import com.keyloop.interview.appointment.dto.AppointmentDtos.VehicleSummary;
import com.keyloop.interview.appointment.events.AppointmentCommittedEvent;
import com.keyloop.interview.appointment.repository.AppointmentRepository;
import com.keyloop.interview.auth.domain.AppUser;
import com.keyloop.interview.auth.domain.UserRole;
import com.keyloop.interview.auth.repository.UserRepository;
import com.keyloop.interview.common.exception.DomainException;
import com.keyloop.interview.common.exception.ResourceNotFoundException;
import com.keyloop.interview.customer.domain.Customer;
import com.keyloop.interview.customer.domain.Vehicle;
import com.keyloop.interview.customer.repository.CustomerRepository;
import com.keyloop.interview.customer.repository.VehicleRepository;
import com.keyloop.interview.customer.service.VehicleOwnershipService;
import com.keyloop.interview.dealership.domain.Dealership;
import com.keyloop.interview.dealership.domain.ServiceBay;
import com.keyloop.interview.dealership.domain.ServiceType;
import com.keyloop.interview.dealership.domain.Technician;
import com.keyloop.interview.dealership.repository.DealershipRepository;
import com.keyloop.interview.dealership.repository.ServiceBayRepository;
import com.keyloop.interview.dealership.repository.ServiceTypeRepository;
import com.keyloop.interview.dealership.repository.TechnicianRepository;
import com.keyloop.interview.dealership.service.AvailabilityService;
import com.keyloop.interview.dealership.service.BookingReferenceGenerator;
import com.keyloop.interview.infrastructure.redis.RedisLockService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentOrchestrationService {

	private final VehicleRepository vehicles;
	private final VehicleOwnershipService ownership;
	private final CustomerRepository customers;
	private final UserRepository users;
	private final ServiceTypeRepository serviceTypes;
	private final AvailabilityService availability;
	private final AppointmentRepository appointments;
	private final BookingReferenceGenerator bookingRefs;
	private final RedisLockService redisLock;
	private final DealershipRepository dealerships;
	private final TechnicianRepository technicians;
	private final ServiceBayRepository baysRepo;
	private final ApplicationEventPublisher events;

	@Transactional
	public AppointmentResponse book(BookingRequest req, UUID actingUserId) {
		AppUser acting = users.findById(actingUserId).orElseThrow();

		Vehicle vehicle = vehicles.findById(req.vehicleId())
				.orElseThrow(() -> new DomainException("VEHICLE_NOT_FOUND", "Vehicle not found"));
		UUID ownerCustomerId = vehicle.getCustomer().getId();

		if (acting.getRole() == UserRole.CUSTOMER) {
			Customer me = customers.findByUser_Id(actingUserId)
					.orElseThrow(() -> new DomainException("CUSTOMER_PROFILE_MISSING", "Customer profile missing"));
			if (!me.getId().equals(ownerCustomerId)) {
				throw new DomainException("VEHICLE_NOT_OWNED", "You do not own this vehicle.");
			}
		}

		if (!ownership.owns(req.vehicleId(), ownerCustomerId)) {
			throw new DomainException("VEHICLE_NOT_OWNED", "Vehicle/customer linkage invalid.");
		}

		ServiceType svcType = serviceTypes.findById(req.serviceTypeId())
				.orElseThrow(() -> new DomainException("SERVICE_TYPE_MISSING", "Service type missing"));
		if (!svcType.isActive()) {
			throw new DomainException("SERVICE_TYPE_INACTIVE", "Service type is inactive.");
		}

		Instant start = req.requestedStartTime();
		Instant end = start.plus(svcType.getDurationMinutes(), ChronoUnit.MINUTES);

		String bayTypePref = "STANDARD";
		String lockKey = req.dealershipId() + ":" + start.truncatedTo(ChronoUnit.HOURS);

		try (var lock = redisLock.acquire(lockKey, Duration.ofSeconds(30))) {

			ServiceBay bay = availability.findAvailableBay(req.dealershipId(), start, end, bayTypePref)
					.or(() -> availability.findAvailableBay(req.dealershipId(), start, end, "LIFT"))
					.orElseThrow(() -> new DomainException("NO_BAY_AVAILABLE", "No service bay available."));

			Technician technician = availability
					.findAvailableTechnician(req.dealershipId(), start, end, svcType.getRequiredSkill())
					.orElseThrow(() -> new DomainException("NO_TECHNICIAN_AVAILABLE",
							"No qualified technician available for the requested time slot."));

			String ref = bookingRefs.nextBookingReference(start);
			Customer owner = customers.findById(ownerCustomerId).orElseThrow();

			Appointment appt = Appointment.builder()
					.id(UUID.randomUUID())
					.bookingReference(ref)
					.customerId(ownerCustomerId)
					.vehicleId(vehicle.getId())
					.dealershipId(req.dealershipId())
					.serviceTypeId(svcType.getId())
					.technicianId(technician.getId())
					.serviceBayId(bay.getId())
					.requestedTime(req.requestedStartTime())
					.startTime(start)
					.endTime(end)
					.status(AppointmentStatus.CONFIRMED)
					.notes(req.notes())
					.totalPrice(svcType.getBasePrice())
					.createdAt(Instant.now())
					.updatedAt(Instant.now())
					.createdBy(actingUserId)
					.build();

			appointments.save(appt);

			UUID notifyUser = owner.getUser().getId();
			events.publishEvent(AppointmentCommittedEvent.from(appt, notifyUser));

			return map(appt);
		}
	}

	@Transactional(readOnly = true)
	public AppointmentResponse get(UUID id) {
		return map(appointments.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found")));
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> list(UUID actingUserId) {
		AppUser u = users.findById(actingUserId).orElseThrow();

		return switch (u.getRole()) {
			case ADMIN -> appointments.findAllByOrderByStartTimeDesc().stream().map(this::map).toList();
			case ADVISOR -> {
				if (u.getDealershipId() == null)
					yield List.of();
				yield appointments.findByDealershipIdOrderByStartTimeDesc(u.getDealershipId()).stream()
						.map(this::map).toList();
			}
			case CUSTOMER -> customers.findByUser_Id(actingUserId)
					.map(c -> appointments.findByCustomerIdOrderByStartTimeDesc(c.getId()).stream().map(this::map)
							.toList())
					.orElseThrow(() -> new ResourceNotFoundException("Customer profile missing"));
		};
	}

	@Transactional
	public void cancel(UUID id, UUID actingUserId, String reasonOptional) {
		AppUser u = users.findById(actingUserId).orElseThrow();
		Appointment a = appointments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

		if (a.getStatus() == AppointmentStatus.CANCELLED) {
			return;
		}

		if (u.getRole() == UserRole.CUSTOMER) {
			Instant now = Instant.now();
			if (a.getStartTime().isAfter(now)) {
				long hours = Duration.between(now, a.getStartTime()).toHours();
				if (hours < 24) {
					throw new DomainException("CANCELLATION_WINDOW", "Cancellation is blocked within 24 hours of start.");
				}
			}
		}

		if (reasonOptional != null && !reasonOptional.isBlank()) {
			a.setCancellationReason(reasonOptional);
		}

		a.setStatus(AppointmentStatus.CANCELLED);
		a.setUpdatedAt(Instant.now());
	}

	private AppointmentResponse map(Appointment a) {

		Vehicle v = vehicles.findById(a.getVehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
		ServiceType svc = serviceTypes.findById(a.getServiceTypeId())
				.orElseThrow(() -> new ResourceNotFoundException("Service type not found"));
		Technician tech = technicians.findById(a.getTechnicianId())
				.orElseThrow(() -> new ResourceNotFoundException("Technician not found"));
		ServiceBay bay = baysRepo.findById(a.getServiceBayId())
				.orElseThrow(() -> new ResourceNotFoundException("Service bay not found"));
		Dealership d = dealerships.findById(a.getDealershipId())
				.orElseThrow(() -> new ResourceNotFoundException("Dealership not found"));

		var ds = new DealershipSummary(d.getId(), d.getName());

		return new AppointmentResponse(a.getId(), a.getBookingReference(), a.getStatus().name(),
				new VehicleSummary(v.getId(), v.getMake(), v.getModel(), v.getYear()), ds,
				new ServiceTypeSummary(svc.getId(), svc.getName(), svc.getDurationMinutes()),
				new PersonSummary(tech.getId(), tech.getFirstName(), tech.getLastName()),
				new BaySummary(bay.getId(), bay.getBayNumber(), bay.getBayType()), a.getStartTime(), a.getEndTime(),
				a.getTotalPrice(), a.getCreatedAt());
	}
}
