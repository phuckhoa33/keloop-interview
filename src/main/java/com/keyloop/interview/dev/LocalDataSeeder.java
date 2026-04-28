package com.keyloop.interview.dev;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.keyloop.interview.auth.domain.AppUser;
import com.keyloop.interview.auth.domain.UserRole;
import com.keyloop.interview.auth.repository.UserRepository;
import com.keyloop.interview.customer.domain.Customer;
import com.keyloop.interview.customer.domain.Vehicle;
import com.keyloop.interview.customer.repository.CustomerRepository;
import com.keyloop.interview.customer.repository.VehicleRepository;
import com.keyloop.interview.dealership.domain.Dealership;
import com.keyloop.interview.dealership.domain.ServiceBay;
import com.keyloop.interview.dealership.domain.ServiceType;
import com.keyloop.interview.dealership.domain.Technician;
import com.keyloop.interview.dealership.domain.TechnicianSchedule;
import com.keyloop.interview.dealership.repository.DealershipRepository;
import com.keyloop.interview.dealership.repository.ServiceBayRepository;
import com.keyloop.interview.dealership.repository.ServiceTypeRepository;
import com.keyloop.interview.dealership.repository.TechnicianRepository;
import com.keyloop.interview.dealership.repository.TechnicianScheduleRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Profile("local")
@Slf4j
public class LocalDataSeeder {

	@Bean
	CommandLineRunner seedSchedulerData(
			UserRepository users,
			PasswordEncoder passwordEncoder,
			CustomerRepository customers,
			VehicleRepository vehicles,
			DealershipRepository dealerships,
			ServiceTypeRepository serviceTypes,
			ServiceBayRepository bays,
			TechnicianRepository technicians,
			TechnicianScheduleRepository schedules) {

		return args -> {
			if (users.count() > 0) {
				log.info("Database already seeded — skipping LocalDataSeeder.");
				return;
			}

			Instant now = Instant.now();
			String demoPassword = passwordEncoder.encode("DemoPass123!");

			UUID dealershipId = UUID.fromString("30000000-0000-0000-0000-000000000001");
			Dealership dealership = Dealership.builder().id(dealershipId).name("Keyloop HCMC")
					.address("1 Example Street, District 1").phone("+84-28-0000-0000").email("service@keyloop.local")
					.timezone("Asia/Ho_Chi_Minh").active(true).createdAt(now).build();
			dealerships.save(dealership);

			UUID serviceTypeId = UUID.fromString("40000000-0000-0000-0000-000000000001");
			ServiceType oil = ServiceType.builder().id(serviceTypeId).name("Oil Change")
					.description("Synthetic oil and filter").durationMinutes(60).requiredSkill("MECHANICAL")
					.basePrice(new BigDecimal("150000")).active(true).build();
			serviceTypes.save(oil);

			ServiceBay bay1 = ServiceBay.builder().id(UUID.fromString("50000000-0000-0000-0000-000000000001"))
					.dealership(dealership).bayNumber("BAY-01").bayType("STANDARD").active(true).build();
			ServiceBay bay2 = ServiceBay.builder().id(UUID.fromString("50000000-0000-0000-0000-000000000002"))
					.dealership(dealership).bayNumber("BAY-02").bayType("STANDARD").active(true).build();
			bays.saveAll(List.of(bay1, bay2));

			UUID adminId = UUID.fromString("10000000-0000-0000-0000-000000000001");
			users.save(AppUser.builder().id(adminId).email("admin@keyloop.local").passwordHash(demoPassword)
					.fullName("System Admin").role(UserRole.ADMIN).dealershipId(null).active(true).emailVerified(true)
					.createdAt(now).updatedAt(now).build());

			users.save(AppUser.builder().id(UUID.fromString("10000000-0000-0000-0000-000000000002"))
					.email("advisor@keyloop.local").passwordHash(demoPassword).fullName("Chi Advisor").role(UserRole.ADVISOR)
					.dealershipId(dealershipId).active(true).emailVerified(true).createdAt(now).updatedAt(now).build());

			UUID customerUserId = UUID.fromString("10000000-0000-0000-0000-000000000003");
			AppUser custUser = AppUser.builder().id(customerUserId).email("customer@keyloop.local")
					.passwordHash(demoPassword).fullName("An Customer").role(UserRole.CUSTOMER).dealershipId(null).active(true)
					.emailVerified(true).createdAt(now).updatedAt(now).build();
			users.save(custUser);

			UUID customerId = UUID.fromString("20000000-0000-0000-0000-000000000001");
			Customer savedCustomer = customers.save(Customer.builder().id(customerId).user(custUser).firstName("An")
					.lastName("Customer").phone("+84-900-000-001").email("customer@keyloop.local").createdAt(now)
					.updatedAt(now).build());

			vehicles.save(Vehicle.builder().id(UUID.fromString("60000000-0000-0000-0000-000000000001")).customer(savedCustomer)
					.vin("JH4KA9650NC000001").make("Toyota").model("Camry").year(2020).licensePlate("51A-12345")
					.color("Silver").mileage(35000).createdAt(now).updatedAt(now).build());

			UUID techUserId = UUID.fromString("10000000-0000-0000-0000-000000000004");
			AppUser techUser = AppUser.builder().id(techUserId).email("tech@keyloop.local").passwordHash(demoPassword)
					.fullName("Binh Technician").role(UserRole.ADVISOR).dealershipId(dealershipId).active(true).emailVerified(true)
					.createdAt(now).updatedAt(now).build();
			users.save(techUser);

			UUID techId = UUID.fromString("70000000-0000-0000-0000-000000000001");
			Technician savedTech = technicians.save(Technician.builder().id(techId).user(techUser).dealershipId(dealershipId)
					.firstName("Binh").lastName("Tech").skills(List.of("MECHANICAL", "ELECTRIC")).active(true).createdAt(now)
					.build());

			LocalTime eight = LocalTime.of(8, 0);
			LocalTime seventeen = LocalTime.of(17, 0);

			for (int dow : List.of(0, 1, 2, 3, 4)) {
				TechnicianSchedule row = TechnicianSchedule.builder().id(UUID.randomUUID()).technician(savedTech)
						.dayOfWeek(dow).startTime(eight).endTime(seventeen).build();
				schedules.save(row);
			}

			log.info("Local seed complete. Demo login: customer@keyloop.local / DemoPass123! (dealershipId={}, serviceTypeId={})",
					dealershipId, serviceTypeId);
		};
	}
}
