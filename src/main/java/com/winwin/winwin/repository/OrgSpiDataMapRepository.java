package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.OrganizationSpiData;

/**
 * @author ArvindKhatik
 *
 */

@Transactional
@Repository
public interface OrgSpiDataMapRepository extends JpaRepository<OrganizationSpiData, Long> {

	@Query(value = "select * from org_spi_mapping where organization_id = :orgId AND is_checked = true", nativeQuery = true)
	List<OrganizationSpiData> getOrgSpiMapDataByOrgId(@Param(value = "orgId") Long orgId);

	@Query(value = "select * from org_spi_mapping where id = :id", nativeQuery = true)
	OrganizationSpiData findSpiSelectedTagsById(@Param("id") Long id);
}
