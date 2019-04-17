package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.OrganizationHistory;

/**
 * @author ArvindKhatik
 *
 */
public interface OrgHistoryRepository extends JpaRepository<OrganizationHistory, Long> {

	@Query(value = " select * from org_history where organization_id = :orgId", nativeQuery = true)
	List<OrganizationHistory> findOrgHistoryDetails(@Param("orgId") Long orgId);

}