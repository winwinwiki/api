package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.OrganizationPayload;

public interface IOrganizationService {
	void createOrganization(OrganizationPayload organizationPayload);

	void deleteOrganization(Long id);

	void updateOrgDetails(OrganizationPayload organizationPayload, Organization organization);

	List<Organization> getOrganizationList();
}
