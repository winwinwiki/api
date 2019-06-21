package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.OrganizationResource;

/**
 * @author ArvindKhatik
 *
 */

@Repository
public interface OrganizationResourceRepository extends JpaRepository<OrganizationResource, Long> {
	@Query(value = "select * from org_resource where organization_id = :organization_id and is_Active = true", nativeQuery = true)
	List<OrganizationResource> findAllOrgResourceById(@Param("organization_id") Long organization_id);

	@Query(value = "select * from org_resource where id = :id", nativeQuery = true)
	OrganizationResource findOrgResourceById(@Param("id") Long id);

	@Query(value = "select * from org_resource ORDER BY id DESC LIMIT 1", nativeQuery = true)
	OrganizationResource findLastOrgResource();

}
