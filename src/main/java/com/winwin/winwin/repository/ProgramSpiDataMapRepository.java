package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.ProgramSpiData;

@Transactional
@Repository
public interface ProgramSpiDataMapRepository extends JpaRepository<ProgramSpiData, Long> {

	@Query(value = "select * from program_spi_mapping where program_id = :programId AND is_checked = true", nativeQuery = true)
	List<ProgramSpiData> getProgramSpiMapDataByOrgId(@Param(value = "programId") Long programId);

	@Query(value = "select * from program_spi_mapping where id = :id", nativeQuery = true)
	ProgramSpiData findSpiSelectedTagsById(@Param("id") Long id);
}
