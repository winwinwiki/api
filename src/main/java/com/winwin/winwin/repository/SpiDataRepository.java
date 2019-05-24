/**
 * 
 */
package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.SpiData;

/**
 * @author ArvindKhatik
 *
 */

@Transactional
@Repository
public interface SpiDataRepository extends JpaRepository<SpiData, Long> {
	@Query(value = "select * from spi_data where dimension_id = :dId AND component_id = :cId AND indicator_id = :indId", nativeQuery = true)
	SpiData findSpiObjByIds(@Param("dId") Long dId, @Param("cId") String cId, @Param("indId") String indId);
	
	@Query(value = "select * from spi_data where id = :id", nativeQuery = true)
	SpiData findSpiObjById(@Param("id") Long id);

}
