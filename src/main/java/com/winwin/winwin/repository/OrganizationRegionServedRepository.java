package com.winwin.winwin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.winwin.winwin.entity.OrganizationRegionServed;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationRegionServedRepository extends JpaRepository<OrganizationRegionServed, Long> {
	@Query(value = "select * from org_region_served where id = :id", nativeQuery = true)
	OrganizationRegionServed findOrgRegionById(@Param("id") Long id);

	@Query(value = "select * from org_region_served where org_id = :orgId AND is_Active = true", nativeQuery = true)
	List<OrganizationRegionServed> findAllOrgRegionsList(@Param("orgId")Long orgId);

	@Query(value = "select * from org_region_served ORDER BY id DESC LIMIT 1", nativeQuery = true)
	OrganizationRegionServed findLastOrgRegion();

	@Query(value = "select * from org_region_served where org_id = :orgId", nativeQuery = true)
	List<OrganizationRegionServed> findLastOrgRegions(@Param("orgId") Long orgId);

}