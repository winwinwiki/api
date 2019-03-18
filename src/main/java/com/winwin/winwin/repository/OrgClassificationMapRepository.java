package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.OrgClassificationMapping;

public interface OrgClassificationMapRepository extends JpaRepository<OrgClassificationMapping, Long> {
	@Query(value = "select * from org_classification_mapping where org_id = :id", nativeQuery = true)
	OrgClassificationMapping findMappingForOrg(@Param("id") Long id);
}	
