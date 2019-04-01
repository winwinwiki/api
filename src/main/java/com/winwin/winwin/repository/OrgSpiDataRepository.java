/**
 * 
 */
package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.winwin.winwin.entity.OrgSpiData;

/**
 * @author ArvindKhatik
 *
 */
public interface OrgSpiDataRepository extends JpaRepository<OrgSpiData, Long> {
	@Query(value = "select * from org_spi_data where dimension_id = :dId AND component_id = :cId AND indicator_id = :indId", nativeQuery = true)
	OrgSpiData findSpiObjByIds(@Param("dId") Long dId, @Param("cId") Long cId, @Param("indId") String indId);

}
