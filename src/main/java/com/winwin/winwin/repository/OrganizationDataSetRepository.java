package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.OrganizationDataSet;

/**
 * @author ArvindK
 *
 */
public interface OrganizationDataSetRepository extends JpaRepository<OrganizationDataSet, Long> {
	@Query(value = "select * from org_dataset", nativeQuery = true)
	List<OrganizationDataSet> findAllOrgDataSet();

	@Query(value = "select * from org_dataset where id = :id", nativeQuery = true)
	OrganizationDataSet findOrgDataSetById(@Param("id") Long id);

}
