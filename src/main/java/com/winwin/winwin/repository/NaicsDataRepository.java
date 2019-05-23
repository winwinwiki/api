package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.NaicsData;

@Transactional
@Repository
public interface NaicsDataRepository extends JpaRepository<NaicsData, Long> {
	List<NaicsData> findByNameContainingIgnoreCase(String name);
	NaicsData findByCode(String code);
}
