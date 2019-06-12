package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.ProgramSdgData;

@Transactional
@Repository
public interface ProgramSdgDataMapRepository extends JpaRepository<ProgramSdgData, Long> {

	@Query(value = "select * from program_sdg_mapping where program_id = :programId AND is_checked = true", nativeQuery = true)
	List<ProgramSdgData> getProgramSdgMapDataByOrgId(@Param("programId") Long programId);

	@Query(value = "select * from program_sdg_mapping where program_id = :programId", nativeQuery = true)
	List<ProgramSdgData> getAllProgramSdgMapDataByOrgId(@Param("programId") Long programId);

	@Query(value = "select * from program_sdg_mapping where id = :id", nativeQuery = true)
	ProgramSdgData findSdgSelectedTagsById(@Param("id") Long id);

	@Query(value = "select * from program_sdg_mapping where program_id = :progId AND sdg_id = :sdgId", nativeQuery = true)
	ProgramSdgData findSdgSelectedTagsByProgramIdAndBySdgId(@Param(value = "progId") Long progId,
			@Param(value = "sdgId") Long sdgId);
}
