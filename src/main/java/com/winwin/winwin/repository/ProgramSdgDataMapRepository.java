package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.ProgramSdgData;

public interface ProgramSdgDataMapRepository extends JpaRepository<ProgramSdgData, Long> {

	@Query(value = "select * from program_sdg_mapping where program_id = :programId AND is_checked = true", nativeQuery = true)
	List<ProgramSdgData> getProgramSdgMapDataByOrgId(@Param("programId") Long programId);

	@Query(value = "select * from program_sdg_mapping where id = :id", nativeQuery = true)
	ProgramSdgData findSdgSelectedTagsById(@Param("id") Long id);
}
