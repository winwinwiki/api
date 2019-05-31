package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.RegionMaster;

/**
 * @author ArvindKhatik
 *
 */

@Transactional
@Repository
public interface RegionMasterRepository extends JpaRepository<RegionMaster, Long> {
	@Query(value = "select * from region_master where name ILIKE %:name% ORDER BY name", nativeQuery = true)
	List<RegionMaster> findRegionsByNameIgnoreCaseContaining(@Param("name") String name);

}
