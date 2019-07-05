package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.NaicsData;

@Repository
public interface NaicsDataRepository extends JpaRepository<NaicsData, Long> {
	List<NaicsData> findByNameContainingIgnoreCase(String name);

	NaicsData findByCode(String code);

	@Query(value = "select * from naics_data", nativeQuery = true)
	@Cacheable("naics_data_result")
	List<NaicsData> findAllNaicsData();
}
