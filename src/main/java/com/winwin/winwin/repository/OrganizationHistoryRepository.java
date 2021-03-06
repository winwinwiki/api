package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.OrganizationHistory;

/**
 * @author ArvindKhatik
 *
 */

@Repository
public interface OrganizationHistoryRepository extends JpaRepository<OrganizationHistory, Long> {

	@Query(value = " select * from org_history where organization_id = :orgId order by id desc", nativeQuery = true)
	List<OrganizationHistory> findOrgHistoryDetails(@Param("orgId") Long orgId);

	@Query(value = "select * from org_history where organization_id = :orgId order by id desc limit 1", nativeQuery = true)
	OrganizationHistory findLastUpdatedHistory(@Param("orgId") Long orgId);

}
