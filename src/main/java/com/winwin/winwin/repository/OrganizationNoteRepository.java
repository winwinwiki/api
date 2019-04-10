package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.winwin.winwin.entity.OrganizationNote;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationNoteRepository extends JpaRepository<OrganizationNote, Long> {
	@Query(value = "select * from org_note where organization_id = :orgId", nativeQuery = true)
	List<OrganizationNote> findAllOrgNotesList(@Param("orgId") Long orgId);

	@Query(value = "select * from org_note where id = :id", nativeQuery = true)
	OrganizationNote findOrgNoteById(@Param("id") Long id);
}
