package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.winwin.winwin.entity.OrgSpiDataMapping;

/**
 * @author ArvindK
 *
 */
public interface OrgSpiDataMapRepository extends JpaRepository<OrgSpiDataMapping, Long> {
	@Query(value = "select * from org_spi_mapping order by dimension_id", nativeQuery = true)
	List<OrgSpiDataMapping> findAllSpiMappedData();
}
