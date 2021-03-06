package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.ProgramRegionServed;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Repository
public interface ProgramRegionServedRepository extends JpaRepository<ProgramRegionServed, Long> {

	@Query(value = "select * from program_region_served where id = :id", nativeQuery = true)
	ProgramRegionServed findProgramRegionById(@Param("id") Long id);

	@Query(value = "select * from program_region_served where program_id = :programId AND is_active = true", nativeQuery = true)
	List<ProgramRegionServed> findAllActiveProgramRegions(@Param("programId") Long programId);

	@Query(value = "select * from program_region_served where program_id = :programId", nativeQuery = true)
	List<ProgramRegionServed> findAllProgramRegions(@Param("programId") Long programId);

	@Query(value = "select * from program_region_served ORDER BY id DESC LIMIT 1", nativeQuery = true)
	ProgramRegionServed findLastProgramRegion();

	@Query(value = "select * from program_region_served where program_id = :programId", nativeQuery = true)
	List<ProgramRegionServed> findLastProgramRegions(@Param("programId") Long programId);

}
