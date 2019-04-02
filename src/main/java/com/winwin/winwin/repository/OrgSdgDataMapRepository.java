package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.OrgSdgDataMapping;

/**
 * @author ArvindKhatik
 *
 */
public interface OrgSdgDataMapRepository extends JpaRepository<OrgSdgDataMapping, Long> {
	@Query(value = "select * from org_sdg_mapping where organization_id = :orgId AND is_checked = true", nativeQuery = true)
	List<OrgSdgDataMapping> getOrgSdgMapDataByOrgId(Long orgId);

	@Query(value = "select * from org_sdg_mapping where id = :id", nativeQuery = true)
	OrgSdgDataMapping findSdgSelectedTagsById(@Param("id") Long id);
}
