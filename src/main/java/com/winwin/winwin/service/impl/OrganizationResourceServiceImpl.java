package com.winwin.winwin.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.exception.ResourceCategoryException;
import com.winwin.winwin.exception.ResourceException;
import com.winwin.winwin.payload.OrganizationResourcePayload;
import com.winwin.winwin.payload.ResourceCategoryPayLoad;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.repository.ResourceCategoryRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationResourceService;
import com.winwin.winwin.service.UserService;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class OrganizationResourceServiceImpl implements OrganizationResourceService {
	@Autowired
	OrganizationResourceRepository organizationResourceRepository;

	@Autowired
	ResourceCategoryRepository resourceCategoryRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService orgHistoryService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationResourceServiceImpl.class);

	private final Long CATEGORY_ID = -1L;

	@Override
	@Transactional
	public OrganizationResource createOrUpdateOrganizationResource(OrganizationResourcePayload orgResourcePayLoad) {
		UserPayload user = userService.getCurrentUserDetails();
		OrganizationResource orgResource = null;
		try {
			if (null != orgResourcePayLoad && null != user) {
				orgResource = constructOrganizationResource(orgResourcePayLoad);
				orgResource = organizationResourceRepository.saveAndFlush(orgResource);

				if (null != orgResource && null != orgResource.getOrganizationId()) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

					orgHistoryService.createOrganizationHistory(user, orgResource.getOrganizationId(), sdf,
							formattedDte, OrganizationConstants.UPDATE, OrganizationConstants.RESOURCE,
							orgResource.getId(), orgResource.getDescription());
				}
			}
		} catch (Exception e) {
			if (null != orgResourcePayLoad.getId()) {
				LOGGER.error(customMessageSource.getMessage("org.resource.exception.updated"), e);
			} else {
				LOGGER.error(customMessageSource.getMessage("org.resource.exception.created"), e);
			}

		}
		return orgResource;

	}// end of method createOrUpdateOrganizationResource

	@Override
	@Transactional
	public void removeOrganizationResource(Long resourceId) {
		OrganizationResource resource = organizationResourceRepository.findOrgResourceById(resourceId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
		UserPayload user = userService.getCurrentUserDetails();
		try {
			if (null != resource && null != user) {
				resource.setUpdatedAt(sdf.parse(formattedDte));
				resource.setUpdatedBy(user.getEmail());
				resource.setIsActive(false);

				organizationResourceRepository.saveAndFlush(resource);

				if (null != resource) {
					orgHistoryService.createOrganizationHistory(user, resource.getOrganizationId(), sdf, formattedDte,
							OrganizationConstants.DELETE, "", resource.getId(), resource.getDescription());
				}
			}
		} catch (ParseException e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.error.deleted"), e);
		}

	}// end of method removeOrganizationResource

	/**
	 * @param orgResourcePayLoad
	 * @return
	 */
	private OrganizationResource constructOrganizationResource(OrganizationResourcePayload orgResourcePayLoad) {
		UserPayload user = userService.getCurrentUserDetails();
		OrganizationResource organizationResource = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = null;
		try {
			if (null != orgResourcePayLoad.getId() && null != user) {
				organizationResource = organizationResourceRepository.getOne(orgResourcePayLoad.getId());
			} else {
				organizationResource = new OrganizationResource();
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organizationResource.setCreatedAt(sdf.parse(formattedDte));
				organizationResource.setCreatedBy(user.getEmail());
				organizationResource.setAdminUrl(orgResourcePayLoad.getAdminUrl());
			}

			if (organizationResource == null) {
				throw new ResourceException(
						"Org resource record not found for Id: " + orgResourcePayLoad.getId() + " to update in DB ");
			} else {
				setOrganizationResourceCategory(orgResourcePayLoad, organizationResource);
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organizationResource.setOrganizationId(orgResourcePayLoad.getOrganizationId());
				organizationResource.setCount(orgResourcePayLoad.getCount());
				organizationResource.setDescription(orgResourcePayLoad.getDescription());
				organizationResource.setUpdatedAt(sdf.parse(formattedDte));
				organizationResource.setUpdatedBy(user.getEmail());

			}

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.exception.construct"), e);
		}

		return organizationResource;
	}

	@Override
	public List<OrganizationResource> getOrganizationResourceList(Long id) {
		return organizationResourceRepository.findAllOrgResourceById(id);
	}// end of method getOrganizationResourceList

	/**
	 * @param organizationResourcePayLoad
	 * @param organizationResource
	 */
	private void setOrganizationResourceCategory(OrganizationResourcePayload organizationResourcePayLoad,
			OrganizationResource organizationResource) {
		ResourceCategory organizationResourceCategory = null;
		try {
			if (null != organizationResourcePayLoad.getResourceCategory()) {
				Long categoryId = organizationResourcePayLoad.getResourceCategory().getId();
				if (null != categoryId) {
					if (categoryId.equals(CATEGORY_ID)) {
						organizationResourceCategory = saveOrganizationResourceCategory(
								organizationResourcePayLoad.getResourceCategory());
						LOGGER.info(customMessageSource.getMessage("org.resource.category.success.created"));
						organizationResource.setResourceCategory(organizationResourceCategory);

					} else {
						organizationResourceCategory = resourceCategoryRepository.getOne(categoryId);
						if (organizationResourceCategory == null) {
							throw new ResourceCategoryException(
									"Org resource category record not found for Id: " + categoryId + " in DB ");
						}
						organizationResource.setResourceCategory(organizationResourceCategory);

					}

				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.category.error.created"), e);
		}
	}

	@Transactional
	public ResourceCategory saveOrganizationResourceCategory(ResourceCategoryPayLoad categoryFromPayLoad) {
		UserPayload user = userService.getCurrentUserDetails();
		ResourceCategory category = new ResourceCategory();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		if (null != user) {
			try {
				if (null != categoryFromPayLoad && !StringUtils.isEmpty(categoryFromPayLoad.getCategoryName())) {
					category.setCategoryName(categoryFromPayLoad.getCategoryName());
				}
				category.setCreatedAt(sdf.parse(formattedDte));
				category.setUpdatedAt(sdf.parse(formattedDte));
				category.setCreatedBy(user.getEmail());
				category.setUpdatedBy(user.getEmail());
				category.setAdminUrl(categoryFromPayLoad.getAdminUrl());
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.resource.category.error.updated"), e);
			}
		}
		return resourceCategoryRepository.saveAndFlush(category);
	}// end of method saveOrganizationResourceCategory

	@Override
	public OrganizationResource getOrgResourceById(Long id) {
		return organizationResourceRepository.findOrgResourceById(id);
	}

	@Override
	public ResourceCategory getResourceCategoryById(Long categoryId) {
		return resourceCategoryRepository.getOne(categoryId);
	}

	@Override
	public List<ResourceCategory> getResourceCategoryList() {
		return resourceCategoryRepository.findAll();
	}

}
