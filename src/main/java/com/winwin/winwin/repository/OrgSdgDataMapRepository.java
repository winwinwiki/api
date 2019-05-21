package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.OrganizationSdgData;

/**
 * @author ArvindKhatik
 *
 */

@Transactional
@Repository
public interface OrgSdgDataMapRepository extends JpaRepository<OrganizationSdgData, Long> {
	@Query(value = "select * from org_sdg_mapping where organization_id = :orgId AND is_checked = true", nativeQuery = true)
	List<OrganizationSdgData> getOrgSdgMapDataByOrgId(Long orgId);

	@Query(value = "select * from org_sdg_mapping where id = :id", nativeQuery = true)
	OrganizationSdgData findSdgSelectedTagsById(@Param("id") Long id);
}
