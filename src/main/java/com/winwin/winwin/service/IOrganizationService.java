package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.OrgChartPayload;
import com.winwin.winwin.payload.OrgHistoryPayload;
import com.winwin.winwin.payload.OrganizationPayload;
import com.winwin.winwin.payload.SubOrganizationPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface IOrganizationService {
	Organization createOrganization(OrganizationPayload organizationPayload);

	Organization createProgram(OrganizationPayload organizationPayload);

	void deleteOrganization(Long id, String type);

	Organization updateOrgDetails(OrganizationPayload organizationPayload, Organization organization, String type);

	List<Organization> getOrganizationList();

	List<Organization> getProgramList(Long orgId);

	OrgChartPayload getOrgCharts(Organization organization, Long orgId);

	Organization createSubOrganization(SubOrganizationPayload payload);

	List<OrgHistoryPayload> getOrgHistoryDetails(Long orgId);

}
