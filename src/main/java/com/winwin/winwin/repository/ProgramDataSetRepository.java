package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.ProgramDataSet;

public interface ProgramDataSetRepository extends JpaRepository<ProgramDataSet, Long> {
	@Query(value = "select * from program_dataset where program_id = :program_id and is_Active = true", nativeQuery = true)
	List<ProgramDataSet> findAllProgramDataSetList(@Param("program_id") Long program_id);

	@Query(value = "select * from program_dataset where id = :id", nativeQuery = true)
	ProgramDataSet findProgramDataSetById(@Param("id") Long id);

	@Query(value = "select * from program_dataset ORDER BY id DESC LIMIT 1", nativeQuery = true)
	ProgramDataSet findLastProgramDataSet();

}
