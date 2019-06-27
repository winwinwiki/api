package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.OrganizationChartPayload;
import com.winwin.winwin.payload.OrganizationHistoryPayload;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.payload.OrganizationRequestPayload;
import com.winwin.winwin.payload.SubOrganizationPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationService {
	Organization createOrganization(OrganizationRequestPayload organizationRequestPayload, ExceptionResponse response);

	Organization createProgram(OrganizationRequestPayload organizationRequestPayload);

	void deleteOrganization(Long id, String type, ExceptionResponse response);

	Organization updateOrgDetails(OrganizationRequestPayload organizationPayload, Organization organization,
			String type, ExceptionResponse response);

	List<Organization> getOrganizationList();

	List<Organization> getProgramList(Long orgId);

	OrganizationChartPayload getOrgCharts(Organization organization);

	Organization createSubOrganization(SubOrganizationPayload payload);

	List<OrganizationHistoryPayload> getOrgHistoryDetails(Long orgId);

	List<Organization> getOrganizationList(OrganizationFilterPayload payload, ExceptionResponse response);

	List<Organization> createOrganizations(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response);

	List<Organization> updateOrganizations(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response);

	Integer getOrgCounts(OrganizationFilterPayload payload, ExceptionResponse response);

}
