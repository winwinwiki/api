package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.Organization;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationRepository extends JpaRepository<Organization, Long>, OrganizationFilterRepository {
	@Query(value = "select * from organization where id = :id", nativeQuery = true)
	Organization findOrgById(@Param("id") Long id);

	@Query(value = "select * from organization where type = 'organization' AND is_Active = true ", nativeQuery = true)
	List<Organization> findAllOrganizationList();

	@Query(value = "select * from organization where type = 'division' AND is_Active = true AND parent_id = :orgId", nativeQuery = true)
	List<Organization> findAllDivisionList(@Param("orgId") Long orgId);

	@Query(value = "select * from organization where type = 'department' AND is_Active = true ", nativeQuery = true)
	List<Organization> findAllDepartmentList();

	@Query(value = "select * from organization where type = 'program' AND parent_id = :orgId AND is_Active = true", nativeQuery = true)
	List<Organization> findAllProgramList(@Param("orgId") Long orgId);

	@Query(value = "select * from organization ORDER BY id DESC LIMIT 1", nativeQuery = true)
	Organization findLastOrg();

	@Query(value = "select * from organization where type = 'organization' "
			+ " AND is_Active = true AND name ILIKE :name", nativeQuery = true)
	List<Organization> findByNameIgnoreCaseContaining(@Param("name") String name);

	@Query(value = "select * from organization where type = 'program' AND parent_id = :orgId "
			+ " AND is_Active = true and name ILIKE :name", nativeQuery = true)
	List<Organization> findProgramByNameIgnoreCaseContaining(@Param("orgId") Long orgId, @Param("name") String name);
}
