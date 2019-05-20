package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.Program;

public interface ProgramRepository extends JpaRepository<Program, Long>, ProgramFilterRepository {

	@Query(value = "select * from program where id = :id", nativeQuery = true)
	Program findProgramById(@Param("id") Long id);

	@Query(value = "select * from program where  org_id = :orgId AND is_Active = true", nativeQuery = true)
	List<Program> findAllProgramList(@Param("orgId") Long orgId);

	@Query(value = "select * from program ORDER BY id DESC LIMIT 1", nativeQuery = true)
	Program findLastProgram();

	@Query(value = "select * from program where is_Active = true " + "  AND name ILIKE :name", nativeQuery = true)
	List<Program> findByNameIgnoreCaseContaining(@Param("name") String name);

	@Query(value = "select * from program where  is_Active = true "
			+ "AND name ILIKE :name and org_id = :orgId", nativeQuery = true)
	List<Program> findProgramByNameIgnoreCaseContaining(@Param("name") String name, @Param("orgId") Long orgId);

	@Query(value = "select * from program where  org_id = :orgId "
			+ " AND is_Active = true and name ILIKE :name", nativeQuery = true)
	List<Program> findProgramByNameIgnoreCaseContaining(@Param("orgId") Long orgId, @Param("name") String name);

}
