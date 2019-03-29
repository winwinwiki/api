package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.OrgSpiDataMapping;

/**
 * @author ArvindKhatik
 *
 */
public interface OrgSpiDataMapRepository extends JpaRepository<OrgSpiDataMapping, Long> {

	@Query(value = "select * from org_spi_mapping where organization_id = :orgId", nativeQuery = true)
	List<OrgSpiDataMapping> getOrgSpiMapDataByOrgId(@Param(value = "orgId") Long orgId);
}
