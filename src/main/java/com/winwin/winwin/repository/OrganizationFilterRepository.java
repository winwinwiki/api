package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.OrganizationFilterPayload;

/**
 * @author ArvindKhatik
 *
 */
@Transactional
@Repository
public interface OrganizationFilterRepository {
	List<Organization> filterOrganization(OrganizationFilterPayload payload, String type, Long orgId, Integer pageNo,
			Integer pageSize);

	Integer getFilterOrganizationCount(OrganizationFilterPayload payload, String type, Long orgId);
}
