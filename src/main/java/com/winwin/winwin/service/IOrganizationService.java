package com.winwin.winwin.service;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.OrganizationPayload;

public interface IOrganizationService {
	void createOrganization(OrganizationPayload organizationPayload);
	void deleteOrganization(Long id);
	void updateOrgDetails(OrganizationPayload organizationPayload, Organization organization);
}
