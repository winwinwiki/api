package com.winwin.winwin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.winwin.winwin.entity.OrgRegionServed;

public interface OrgRegionServedRepository extends JpaRepository<OrgRegionServed, Long> {
	@Query(value = "select * from org_region_served where id = :id", nativeQuery = true)
	OrgRegionServed findOrgRegionById(@Param("id") Long id);

	@Query(value = "select * from org_region_served where is_Active = true", nativeQuery = true)
	List<OrgRegionServed> findAllOrgRegionsList();

	@Query(value = "select * from org_region_served ORDER BY id DESC LIMIT 1", nativeQuery = true)
	OrgRegionServed findLastOrgRegion();

	@Query(value = "select * from org_region_served where org_id = :org_id", nativeQuery = true)
	List<OrgRegionServed> findLastOrgRegions(@Param("org_id") Long org_id);

}
