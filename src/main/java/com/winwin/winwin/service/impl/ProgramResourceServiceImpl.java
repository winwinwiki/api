package com.winwin.winwin.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.ProgramResource;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.exception.ResourceException;
import com.winwin.winwin.payload.ProgramResourcePayLoad;
import com.winwin.winwin.payload.ResourceCategoryPayLoad;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.ProgramResourceRepository;
import com.winwin.winwin.repository.ResourceCategoryRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.ProgramResourceService;
import com.winwin.winwin.service.UserService;

import io.micrometer.core.instrument.util.StringUtils;

@Service
public class ProgramResourceServiceImpl implements ProgramResourceService {

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService organizationHistoryService;

	@Autowired
	ProgramResourceRepository programResourceRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationResourceServiceImpl.class);

	private final Long CATEGORY_ID = -1L;

	@Autowired
	private ResourceCategoryRepository resourceCategoryRepository;

	private ProgramResource constructProgramResource(ProgramResourcePayLoad programResourcePayLoad) {
		UserPayload user = userService.getCurrentUserDetails();
		ProgramResource programResource = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = null;
		try {
			if (null != programResourcePayLoad.getId() && null != user) {
				programResource = programResourceRepository.getOne(programResourcePayLoad.getId());
			} else {
				programResource = new ProgramResource();
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				programResource.setCreatedAt(sdf.parse(formattedDte));
				programResource.setCreatedBy(user.getEmail());
				programResource.setAdminUrl(programResourcePayLoad.getAdminUrl());
			}

			if (programResource == null) {
				throw new ResourceException("Org resource record not found for Id: " + programResourcePayLoad.getId()
						+ " to update in DB ");
			} else {
				setResourceCategory(programResourcePayLoad, programResource);
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				programResource.setProgramId(programResourcePayLoad.getProgramId());
				programResource.setCount(programResourcePayLoad.getCount());
				programResource.setDescription(programResourcePayLoad.getDescription());
				programResource.setUpdatedAt(sdf.parse(formattedDte));
				programResource.setUpdatedBy(user.getEmail());

			}

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.exception.construct"), e);
		}

		return programResource;
	}

	private void setResourceCategory(ProgramResourcePayLoad programResourcePayLoad, ProgramResource programResource) {
		ResourceCategory resourceCategory = null;
		try {
			if (null != programResourcePayLoad.getResourceCategory()) {
				Long categoryId = programResourcePayLoad.getResourceCategory().getId();

				if (categoryId != null)
					resourceCategory = resourceCategoryRepository.findById(categoryId).orElse(null);

				if (resourceCategory == null) {
					resourceCategory = saveResourceCategory(programResourcePayLoad.getResourceCategory());
					LOGGER.info(customMessageSource.getMessage("org.resource.category.success.created"));
				}
				programResource.setResourceCategory(resourceCategory);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.category.error.created"), e);
		}
	}

	public ResourceCategory saveResourceCategory(ResourceCategoryPayLoad categoryFromPayLoad) {
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
	}

	@Override
	public ProgramResource getProgramResourceById(Long id) {
		return programResourceRepository.findProgramResourceById(id);
	}// end of method getOrganizationResourceById

	@Override
	public ResourceCategory getResourceCategoryById(Long categoryId) {
		return resourceCategoryRepository.getOne(categoryId);
	}// end of method getOrganizationResourceCategoryById

	@Override
	public List<ResourceCategory> getResourceCategoryList() {
		return resourceCategoryRepository.findAll();
	}// end of method getOrganizationResourceCategoryList

	@Override
	public ProgramResource createOrUpdateProgramResource(ProgramResourcePayLoad programResourcePayLoad) {
		// TODO Auto-generated method stub
		UserPayload user = userService.getCurrentUserDetails();
		ProgramResource programResource = null;
		try {
			if (null != programResourcePayLoad && null != user) {
				programResource = constructProgramResource(programResourcePayLoad);
				programResource = programResourceRepository.saveAndFlush(programResource);

				if (null != programResource && null != programResource.getProgramId()) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

					organizationHistoryService.createOrganizationHistory(user,
							programResourcePayLoad.getOrganizationId(), sdf, formattedDte, OrganizationConstants.UPDATE,
							OrganizationConstants.RESOURCE, programResource.getId(), programResource.getDescription());
				}
			}
		} catch (Exception e) {
			if (null != programResourcePayLoad.getId()) {
				LOGGER.error(customMessageSource.getMessage("org.resource.exception.updated"), e);
			} else {
				LOGGER.error(customMessageSource.getMessage("org.resource.exception.created"), e);
			}

		}
		return programResource;
	}

	@Override
	public void removeProgramResource(Long resourceId, Long organizationId) {
		// TODO Auto-generated method stub
		ProgramResource resource = programResourceRepository.findProgramResourceById(resourceId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
		UserPayload user = userService.getCurrentUserDetails();
		try {
			if (null != resource && null != user) {
				resource.setUpdatedAt(sdf.parse(formattedDte));
				resource.setUpdatedBy(user.getEmail());
				resource.setIsActive(false);

				programResourceRepository.saveAndFlush(resource);

				if (null != resource) {
					organizationHistoryService.createOrganizationHistory(user, organizationId, sdf, formattedDte,
							OrganizationConstants.DELETE, "", resource.getId(), resource.getDescription());
				}
			}
		} catch (ParseException e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.error.deleted"), e);
		}
	}

	@Override
	public List<ProgramResource> getProgramResourceList(Long programId) {
		// TODO Auto-generated method stub
		return programResourceRepository.findAllProgramResourceById(programId);
	}

}
