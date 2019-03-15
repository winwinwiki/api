package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.payload.OrganizationResourcePayLoad;

public interface IOrganizationResourceService {
	OrganizationResource createOrUpdateOrganizationResource(OrganizationResourcePayLoad organizationResourcePayLoad);

	void removeOrganizationResource(Long resourceId);

	List<OrganizationResource> getOrganizationResourceList(Long organizationId);

}
