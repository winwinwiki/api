package com.winwin.winwin.repository;

import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.OrganizationFilterPayload;

public interface OrganizationFilterRepository {
	List<Organization> filterOrganization(OrganizationFilterPayload payload, String type, Long orgId);
}
