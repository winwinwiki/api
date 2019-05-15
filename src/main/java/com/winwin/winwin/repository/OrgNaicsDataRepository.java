package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winwin.winwin.entity.NaicsData;

public interface OrgNaicsDataRepository extends JpaRepository<NaicsData, Long> {
	List<NaicsData> findByNameContainingIgnoreCase(String name);
}
