package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
	@Query(value = "select * from organization where id = :id", nativeQuery = true)
	Organization findOrgById(@Param("id") Long id); 

	@Query(value = "select * from organization where is_Active = true", nativeQuery = true)
	List<Organization> findAllOrganizationList();

	@Query(value = "select * from organization ORDER BY id DESC LIMIT 1", nativeQuery = true)
	Organization findLastOrg();
}
