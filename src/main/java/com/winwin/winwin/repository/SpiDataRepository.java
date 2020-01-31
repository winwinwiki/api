/**
 * 
 */
package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.SpiData;

/**
 * @author ArvindKhatik
 *
 */

@Repository
public interface SpiDataRepository extends JpaRepository<SpiData, Long> {
	@Query(value = "select * from spi_data where dimension_id = :dId AND component_id = :cId AND indicator_id = :indId AND is_active = true", nativeQuery = true)
	SpiData findSpiObjByIds(@Param("dId") Long dId, @Param("cId") String cId, @Param("indId") String indId);

	@Query(value = "select * from spi_data where id = :id", nativeQuery = true)
	SpiData findSpiObjById(@Param("id") Long id);

	@Query(value = "select * from spi_data where is_active = true", nativeQuery = true)
	List<SpiData> findAllActiveSpiData();

	@Query(value = "select * from spi_data", nativeQuery = true)
	List<SpiData> findAllSpiData();

}
