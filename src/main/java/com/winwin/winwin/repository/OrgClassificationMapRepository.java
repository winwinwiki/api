package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.OrganizationClassification;

@Repository
public interface OrgClassificationMapRepository extends JpaRepository<OrganizationClassification, Long> {
	@Query(value = "select * from org_classification_mapping where org_id = :id", nativeQuery = true)
	OrganizationClassification findMappingForOrg(@Param("id") Long id);
}
