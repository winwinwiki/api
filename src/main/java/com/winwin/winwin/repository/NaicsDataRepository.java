package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.NaicsData;

@Repository
public interface NaicsDataRepository extends JpaRepository<NaicsData, Long> {
	List<NaicsData> findByNameContainingIgnoreCase(String name);
	NaicsData findByCode(String code);
}
