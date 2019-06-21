package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.OrganizationSdgData;

/**
 * @author ArvindKhatik
 *
 */

@Repository
public interface OrgSdgDataMapRepository extends JpaRepository<OrganizationSdgData, Long> {
	@Query(value = "select * from org_sdg_mapping where organization_id = :orgId AND is_checked = true", nativeQuery = true)
	List<OrganizationSdgData> getOrgSdgMapDataByOrgId(Long orgId);
	
	@Query(value = "select * from org_sdg_mapping where organization_id = :orgId", nativeQuery = true)
	List<OrganizationSdgData> getAllOrgSdgMapDataByOrgId(Long orgId);

	@Query(value = "select * from org_sdg_mapping where id = :id", nativeQuery = true)
	OrganizationSdgData findSdgSelectedTagsById(@Param("id") Long id);
	
	@Query(value = "select * from org_sdg_mapping where organization_id = :orgId AND sdg_id = :sdgId", nativeQuery = true)
	OrganizationSdgData findSdgSelectedTagsByOrgIdAndBySdgId(@Param(value = "orgId") Long orgId, @Param(value = "sdgId") Long sdgId);
}
