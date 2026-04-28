package com.keyloop.interview.dealership.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.keyloop.interview.dealership.domain.BlockedSlot;

public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, UUID> {

	@Query("""
			select b from BlockedSlot b where b.resourceType = :rtype and b.resourceId = :rid
			and b.startTime < :end and b.endTime > :start
			""")
	List<BlockedSlot> overlapping(@Param("rtype") String rtype, @Param("rid") UUID rid, @Param("start") Instant start,
			@Param("end") Instant end);
}
