package com.winwin.winwin.service;

import java.sql.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.OrganizationResourceCategory;
import com.winwin.winwin.exception.OrganizationResourceCategoryException;
import com.winwin.winwin.exception.OrganizationResourceException;
import com.winwin.winwin.payload.OrganizationResourcePayLoad;
import com.winwin.winwin.repository.OrganizationResourceCategoryRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindK
 *
 */
@Component
public class OrganizationResourceService implements IOrganizationResourceService {
	@Autowired
	OrganizationResourceRepository organizationResourceRepository;

	@Autowired
	OrganizationResourceCategoryRepository organizationResourceCategoryRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationResourceService.class);

	private final Long CATEGORY_ID = -1L;

	@Override
	public void createOrUpdateOrganizationResource(OrganizationResourcePayLoad organizationResourcePayLoad) {

		try {
			if (null != organizationResourcePayLoad) {
				OrganizationResource organizationResource = constructOrganizationResource(organizationResourcePayLoad);

				organizationResourceRepository.saveAndFlush(organizationResource);
			}
		} catch (Exception e) {
			if (null != organizationResourcePayLoad.getId()) {
				LOGGER.error("Exception occured while updating organization resource", e);
			} else {
				LOGGER.error("Exception occured while creating organization resource", e);
			}

		}

	}// end of method createOrUpdateOrganizationResource

	@Override
	public void removeOrganizationResource(Long resourceId) {
		organizationResourceRepository.deleteById(resourceId);

	}// end of method removeOrganizationResource

	/**
	 * @param organizationResourcePayLoad
	 * @return
	 */
	private OrganizationResource constructOrganizationResource(
			OrganizationResourcePayLoad organizationResourcePayLoad) {
		OrganizationResource organizationResource = null;
		try {
			if (null != organizationResourcePayLoad.getId()) {
				organizationResource = organizationResourceRepository.getOne(organizationResourcePayLoad.getId());
			} else {
				organizationResource = new OrganizationResource();
				organizationResource.setCreatedAt(new Date(System.currentTimeMillis()));
			}

			if (organizationResource == null) {
				throw new OrganizationResourceException("Org resource record not found for Id: "
						+ organizationResourcePayLoad.getId() + " to update in DB ");
			} else {
				setOrganizationResourceCategory(organizationResourcePayLoad, organizationResource);

				organizationResource.setOrganizationId(organizationResourcePayLoad.getOrganization_id());
				organizationResource.setCount(organizationResourcePayLoad.getCount());
				organizationResource.setDescription(organizationResourcePayLoad.getDescription());
				organizationResource.setType(organizationResourcePayLoad.getType());
				organizationResource.setUrl(organizationResourcePayLoad.getUrl());
				organizationResource.setUpdatedAt(new Date(System.currentTimeMillis()));

			}

		} catch (Exception e) {
			LOGGER.error("Exception occured during construction of org resource", e);
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
	private void setOrganizationResourceCategory(OrganizationResourcePayLoad organizationResourcePayLoad,
			OrganizationResource organizationResource) {
		OrganizationResourceCategory organizationResourceCategory = null;
		;
		try {
			if (null != organizationResourcePayLoad.getOrganizationResourceCategory()) {
				Long categoryId = organizationResourcePayLoad.getOrganizationResourceCategory().getId();
				if (null != categoryId) {
					if (categoryId == CATEGORY_ID) {
						organizationResourceCategory = saveOrganizationResourceCategory(
								organizationResourcePayLoad.getOrganizationResourceCategory());
						LOGGER.info("org resource category has been created successfully in DB");
						organizationResource.setOrganizationResourceCategory(organizationResourceCategory);

					} else {
						organizationResourceCategory = organizationResourceCategoryRepository.getOne(categoryId);
						if (organizationResourceCategory == null) {
							throw new OrganizationResourceCategoryException(
									"Org resource category record not found for Id: " + categoryId + " in DB ");
						} else {
							organizationResource.setOrganizationResourceCategory(organizationResourceCategory);

						}

					}

				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured during creation of organization resource category", e);
		}
	}

	public OrganizationResourceCategory saveOrganizationResourceCategory(
			OrganizationResourceCategory categoryFromPayLoad) {
		OrganizationResourceCategory category = new OrganizationResourceCategory();
		try {
			if (!StringUtils.isEmpty(categoryFromPayLoad.getCategoryName())) {
				category.setCategoryName(categoryFromPayLoad.getCategoryName());
			}

			category.setCreatedAt(new Date(System.currentTimeMillis()));
			category.setUpdatedAt(new Date(System.currentTimeMillis()));
		} catch (Exception e) {
			LOGGER.error("Exception occured during updation of organization Resource category in DB", e);
		}

		return organizationResourceCategoryRepository.saveAndFlush(category);
	}// end of method saveOrganizationResourceCategory

	public OrganizationResource getOrgResourceById(Long id) {
		return organizationResourceRepository.findOrgResourceById(id);
	}// end of method getOrganizationResourceById

	public OrganizationResourceCategory getOrganizationResourceCategoryById(Long categoryId) {
		return organizationResourceCategoryRepository.getOne(categoryId);
	}// end of method getOrganizationResourceCategoryById

	public List<OrganizationResourceCategory> getOrganizationResourceCategoryList() {
		return organizationResourceCategoryRepository.findAll();
	}// end of method getOrganizationResourceCategoryList

}