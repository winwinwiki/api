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
	@Query(value = "select * from region_master where name ILIKE %:name% order by "
			+ "case when name ILIKE :name% then 0 else 1 end, name", nativeQuery = true)
	List<RegionMaster> findRegionsByNameIgnoreCaseContaining(@Param("name") String name, Pageable pageable);

}
