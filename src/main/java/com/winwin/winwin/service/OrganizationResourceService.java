package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.payload.OrganizationResourcePayload;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationResourceService {
	OrganizationResource createOrUpdateOrganizationResource(OrganizationResourcePayload organizationResourcePayLoad);

	void removeOrganizationResource(Long resourceId);

	List<OrganizationResource> getOrganizationResourceList(Long organizationId);

	OrganizationResource getOrgResourceById(Long id);

	ResourceCategory getResourceCategoryById(Long categoryId);

	List<ResourceCategory> getResourceCategoryList();

}
