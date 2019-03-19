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
	@Query(value = "select * from org_dataset where organization_id = :organization_id and is_Active = true", nativeQuery = true)
	List<OrganizationDataSet> findAllOrgDataSetList(@Param("organization_id") Long organization_id);

	@Query(value = "select * from org_dataset where id = :id", nativeQuery = true)
	OrganizationDataSet findOrgDataSetById(@Param("id") Long id);

	@Query(value = "select * from org_dataset ORDER BY id DESC LIMIT 1", nativeQuery = true)
	OrganizationDataSet findLastOrgDataSet();

}
