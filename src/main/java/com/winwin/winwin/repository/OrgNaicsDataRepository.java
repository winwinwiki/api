package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winwin.winwin.entity.OrgNaicsData;

public interface OrgNaicsDataRepository extends JpaRepository<OrgNaicsData, Long> {
	List<OrgNaicsData> findByNameContainingIgnoreCase(String name);
}
