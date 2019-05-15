/**
 * 
 */
package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.winwin.winwin.entity.SpiData;

/**
 * @author ArvindKhatik
 *
 */
public interface OrgSpiDataRepository extends JpaRepository<SpiData, Long> {
	@Query(value = "select * from org_spi_data where dimension_id = :dId AND component_id = :cId AND indicator_id = :indId", nativeQuery = true)
	SpiData findSpiObjByIds(@Param("dId") Long dId, @Param("cId") String cId, @Param("indId") String indId);

}
