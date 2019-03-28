package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.winwin.winwin.entity.OrgSdgDataMapping;

/**
 * @author ArvindKhatik
 *
 */
public interface OrgSdgDataMapRepository extends JpaRepository<OrgSdgDataMapping, Long> {
	@Query(value = "select * from org_sdg_mapping where organization_id = :orgId", nativeQuery = true)
	List<OrgSdgDataMapping> getOrgSdgMapDataByOrgId(Long orgId);
}
