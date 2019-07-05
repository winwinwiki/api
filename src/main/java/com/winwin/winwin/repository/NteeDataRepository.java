package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.winwin.winwin.entity.NteeData;

@Repository
public interface NteeDataRepository extends JpaRepository<NteeData, Long> {
	NteeData findByCode(String code);

	@Query(value = "select * from ntee_data", nativeQuery = true)
	@Cacheable("ntee_data_result")
	List<NteeData> findAllNteeData();

}
