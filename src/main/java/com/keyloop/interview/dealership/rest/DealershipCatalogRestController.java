package com.keyloop.interview.dealership.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.keyloop.interview.common.exception.ResourceNotFoundException;
import com.keyloop.interview.dealership.domain.Dealership;
import com.keyloop.interview.dealership.domain.ServiceType;
import com.keyloop.interview.dealership.repository.DealershipRepository;
import com.keyloop.interview.dealership.repository.ServiceTypeRepository;
import com.keyloop.interview.dealership.service.AvailabilityService;
import com.keyloop.interview.dealership.web.dto.AvailableSlotSuggestion;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DealershipCatalogRestController {

	private final DealershipRepository dealerships;
	private final ServiceTypeRepository serviceTypes;
	private final AvailabilityService availabilityService;

	@GetMapping("/api/v1/dealerships")
	@PreAuthorize("isAuthenticated()")
	public List<Dealership> listDealerships() {
		return dealerships.findAll();
	}

	@GetMapping("/api/v1/dealerships/{id}")
	@PreAuthorize("isAuthenticated()")
	public Dealership oneDealership(@PathVariable UUID id) {
		return dealerships.findById(id).orElseThrow(() -> new ResourceNotFoundException("Dealership"));
	}

	@GetMapping("/api/v1/service-types")
	@PreAuthorize("isAuthenticated()")
	public List<ServiceType> listServiceTypes() {
		return serviceTypes.findAll();
	}

	@GetMapping("/api/v1/service-types/{id}")
	@PreAuthorize("isAuthenticated()")
	public ServiceType oneServiceType(@PathVariable UUID id) {
		return serviceTypes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service type"));
	}

	@GetMapping("/api/v1/availability")
	@PreAuthorize("isAuthenticated()")
	public List<AvailableSlotSuggestion> availability(
			@RequestParam UUID dealershipId,
			@RequestParam UUID serviceTypeId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(defaultValue = "5") int count) {

		return availabilityService.suggestSlots(dealershipId, serviceTypeId, date, count);
	}
}
