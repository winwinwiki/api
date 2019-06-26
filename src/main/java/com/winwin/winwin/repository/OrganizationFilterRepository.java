package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.OrganizationFilterPayload;

/**
 * @author ArvindKhatik
 *
 */
@Repository
public interface OrganizationFilterRepository {
	List<Organization> filterOrganization(OrganizationFilterPayload payload, String type, Long orgId, Integer pageNo,
			Integer pageSize);

	Integer getFilterOrganizationCount(OrganizationFilterPayload payload, String type, Long orgId);
}
