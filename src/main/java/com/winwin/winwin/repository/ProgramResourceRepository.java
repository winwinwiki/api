package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.ProgramResource;

public interface ProgramResourceRepository extends JpaRepository<ProgramResource, Long> {
	@Query(value = "select * from program_resource where program_id = :program_id and is_Active = true", nativeQuery = true)
	List<ProgramResource> findAllProgramResourceById(@Param("program_id") Long programId);

	@Query(value = "select * from program_resource where id = :id", nativeQuery = true)
	ProgramResource findProgramResourceById(@Param("id") Long id);

	@Query(value = "select * from program_resource ORDER BY id DESC LIMIT 1", nativeQuery = true)
	ProgramResource findLastProgramResource();

}
