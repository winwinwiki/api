package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.RegionMaster;

/**
 * @author ArvindKhatik
 *
 */

@Repository
public interface RegionMasterRepository extends JpaRepository<RegionMaster, Long> {
	// The below query returns all the regions contains the specified
	// pattern,populates records starting with the pattern first.
	@Query(value = "SELECT * FROM region_master WHERE name ILIKE %:name% ORDER BY CASE WHEN name ILIKE :nameWithOrderBy% THEN 0 ELSE 1 END,name", nativeQuery = true)
	List<RegionMaster> findRegionsByNameIgnoreCaseContaining(@Param("name") String name,@Param("nameWithOrderBy") String nameWithOrderBy, Pageable pageable);

}
