/**
 * 
 */
package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.OrgSpiData;

/**
 * @author ArvindK
 *
 */
public interface OrgSpiDataRepository extends JpaRepository<OrgSpiData, Long> {
	@Query(value = "select * from org_spi_data where level = 1", nativeQuery = true)
	List<OrgSpiData> findAllSpiDimensionData();

	@Query(value = "select * from org_spi_data where level = 2", nativeQuery = true)
	List<OrgSpiData> findAllSpiComponentData();

	@Query(value = "select * from org_spi_data where level = 3", nativeQuery = true)
	List<OrgSpiData> findAllSpiIndicatorData();

	@Query(value = "select * from org_spi_data where level = 2 AND parent_id=:parentId", nativeQuery = true)
	List<OrgSpiData> findAllComponentsById(@Param("parentId") Long parentId);

	@Query(value = "select * from org_spi_data where level = 3 AND parent_id=:parentId", nativeQuery = true)
	List<OrgSpiData> findAllIndicatorsById(@Param("parentId") Long parentId);

}
