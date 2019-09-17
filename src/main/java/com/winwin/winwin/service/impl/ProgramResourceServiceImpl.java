package com.winwin.winwin.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.ProgramResource;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.exception.ResourceException;
import com.winwin.winwin.payload.ProgramResourcePayLoad;
import com.winwin.winwin.payload.ResourceCategoryPayLoad;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.repository.ProgramResourceRepository;
import com.winwin.winwin.repository.ResourceCategoryRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.ProgramResourceService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class ProgramResourceServiceImpl implements ProgramResourceService {
	@Autowired
	private ProgramRepository programRepository;
	@Autowired
	protected CustomMessageSource customMessageSource;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationHistoryService organizationHistoryService;
	@Autowired
	private ProgramResourceRepository programResourceRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationResourceServiceImpl.class);

	@Autowired
	private ResourceCategoryRepository resourceCategoryRepository;

	/**
	 * create or update OrganizationResource and ResourceCategory, create new
	 * entry in ResourceCategory if the value of CATEGORY_ID is -1L;
	 * 
	 * @param orgResourcePayLoad
	 */
	@Override
	@Transactional
	@CacheEvict(value = "program_resource__list,program_resource_category_list")
	public ProgramResource createOrUpdateProgramResource(ProgramResourcePayLoad programResourcePayLoad) {
		ProgramResource programResource = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (null != programResourcePayLoad && null != user) {
				programResource = constructProgramResource(programResourcePayLoad);
				programResource = programResourceRepository.saveAndFlush(programResource);

				/*
				 * if (null != programResource && null !=
				 * programResource.getProgram() && null !=
				 * programResource.getProgram().getId()) {
				 * organizationHistoryService.createOrganizationHistory(user,
				 * programResourcePayLoad.getOrganizationId(),
				 * programResourcePayLoad.getProgramId(),
				 * OrganizationConstants.UPDATE, OrganizationConstants.RESOURCE,
				 * programResource.getId(),
				 * programResource.getResourceCategory().getCategoryName(), "");
				 * }
				 */

				if (null != programResource && null != programResource.getProgram()
						&& null != programResource.getProgram().getId()) {
					if (null != programResourcePayLoad.getId()) {
						organizationHistoryService.createOrganizationHistory(user,
								programResourcePayLoad.getOrganizationId(), programResourcePayLoad.getProgramId(),
								OrganizationConstants.UPDATE, OrganizationConstants.RESOURCE, programResource.getId(),
								programResource.getResourceCategory().getCategoryName(), "");
					} else {
						organizationHistoryService.createOrganizationHistory(user,
								programResourcePayLoad.getOrganizationId(), programResourcePayLoad.getProgramId(),
								OrganizationConstants.CREATE, OrganizationConstants.RESOURCE, programResource.getId(),
								programResource.getResourceCategory().getCategoryName(), "");
					}
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

	/**
	 * delete ProgramResource by Id
	 * 
	 * @param resourceId
	 * @param organizationId
	 * @param programId
	 */
	@Override
	@Transactional
	@CacheEvict(value = "program_resource__list,program_resource_category_list")
	public void removeProgramResource(Long resourceId, Long organizationId, Long programId) {
		// TODO Auto-generated method stub
		ProgramResource resource = programResourceRepository.findProgramResourceById(resourceId);
		try {
			UserPayload user = userService.getCurrentUserDetails();
			Date date = CommonUtils.getFormattedDate();
			if (null != resource && null != user) {
				resource.setIsActive(false);
				resource.setUpdatedAt(date);
				resource.setUpdatedBy(user.getUserDisplayName());
				resource.setUpdatedByEmail(user.getEmail());
				programResourceRepository.saveAndFlush(resource);

				if (null != resource) {
					organizationHistoryService.createOrganizationHistory(user, organizationId, programId,
							OrganizationConstants.DELETE, "", resource.getId(),
							resource.getResourceCategory().getCategoryName(), "");
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.error.deleted"), e);
		}
	}

	@Override
	public ProgramResource getProgramResourceById(Long id) {
		return programResourceRepository.findProgramResourceById(id);
	}// end of method getOrganizationResourceById

	/**
	 * returns ResourceCategory by Id
	 * 
	 * @param categoryId
	 */
	@Override
	public ResourceCategory getResourceCategoryById(Long categoryId) {
		return resourceCategoryRepository.getOne(categoryId);
	}// end of method getOrganizationResourceCategoryById

	/**
	 * returns ResourceCategory List
	 * 
	 * @param id
	 */
	@Override
	@Cacheable("program_resource_category_list")
	public List<ResourceCategory> getResourceCategoryList() {
		return resourceCategoryRepository.findAll();
	}// end of method getOrganizationResourceCategoryList

	/**
	 * returns ProgramResource List by programId
	 * 
	 * @param programId
	 */
	@Override
	@Cacheable("program_resource__list")
	public List<ProgramResource> getProgramResourceList(Long programId) {
		return programResourceRepository.findAllActiveProgramResources(programId);
	}

	private ProgramResource constructProgramResource(ProgramResourcePayLoad programResourcePayLoad) {
		ProgramResource programResource = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			Date date = CommonUtils.getFormattedDate();
			if (null != programResourcePayLoad.getId() && null != user) {
				programResource = programResourceRepository.getOne(programResourcePayLoad.getId());
			} else {
				programResource = new ProgramResource();
				BeanUtils.copyProperties(programResourcePayLoad, programResource);
				programResource.setIsActive(true);
				programResource.setCreatedAt(date);
				programResource.setCreatedBy(user.getUserDisplayName());
				programResource.setCreatedByEmail(user.getEmail());
			}

			if (programResource == null) {
				throw new ResourceException("Org resource record not found for Id: " + programResourcePayLoad.getId()
						+ " to update in DB ");
			} else {
				setResourceCategory(programResourcePayLoad, programResource);
				BeanUtils.copyProperties(programResourcePayLoad, programResource);
				programResource.setIsActive(true);
				programResource.setUpdatedAt(date);
				programResource.setUpdatedBy(user.getUserDisplayName());
				programResource.setUpdatedByEmail(user.getEmail());

				if (null != programResourcePayLoad.getProgramId()) {
					Program program = programRepository.findProgramById(programResourcePayLoad.getProgramId());
					programResource.setProgram(program);
				}
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

	private ResourceCategory saveResourceCategory(ResourceCategoryPayLoad categoryFromPayLoad) {
		ResourceCategory category = new ResourceCategory();
		try {
			UserPayload user = userService.getCurrentUserDetails();
			Date date = CommonUtils.getFormattedDate();
			if (null != user) {
				if (null != categoryFromPayLoad && !StringUtils.isEmpty(categoryFromPayLoad.getCategoryName())) {
					category.setCategoryName(categoryFromPayLoad.getCategoryName());
				}
				category.setCreatedAt(date);
				category.setUpdatedAt(date);
				category.setCreatedBy(user.getUserDisplayName());
				category.setUpdatedBy(user.getUserDisplayName());
				category.setCreatedByEmail(user.getEmail());
				category.setUpdatedByEmail(user.getEmail());
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.category.error.updated"), e);
		}
		return resourceCategoryRepository.saveAndFlush(category);
	}

}
