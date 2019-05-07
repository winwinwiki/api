package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.OrgChartPayload;
import com.winwin.winwin.payload.OrgHistoryPayload;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.payload.OrganizationRequestPayload;
import com.winwin.winwin.payload.SubOrganizationPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface IOrganizationService {
	Organization createOrganization(OrganizationRequestPayload organizationRequestPayload);

	Organization createProgram(OrganizationRequestPayload organizationRequestPayload);

	void deleteOrganization(Long id, String type);

	Organization updateOrgDetails(OrganizationRequestPayload organizationPayload, Organization organization,
			String type);

	List<Organization> getOrganizationList();

	List<Organization> getProgramList(Long orgId);

	OrgChartPayload getOrgCharts(Organization organization, Long orgId);

	Organization createSubOrganization(SubOrganizationPayload payload);

	List<OrgHistoryPayload> getOrgHistoryDetails(Long orgId);

	List<Organization> getOrganizationList(OrganizationFilterPayload payload);

	List<Organization> createOrganizations(List<OrganizationRequestPayload> organizationPayload);
}
