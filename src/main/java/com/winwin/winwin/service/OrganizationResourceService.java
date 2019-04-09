package com.winwin.winwin.service;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.OrganizationResourceCategory;
import com.winwin.winwin.exception.OrganizationResourceCategoryException;
import com.winwin.winwin.exception.OrganizationResourceException;
import com.winwin.winwin.payload.OrganizationResourceCategoryPayLoad;
import com.winwin.winwin.payload.OrganizationResourcePayLoad;
import com.winwin.winwin.repository.OrganizationResourceCategoryRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class OrganizationResourceService implements IOrganizationResourceService {
	@Autowired
	OrganizationResourceRepository organizationResourceRepository;

	@Autowired
	OrganizationResourceCategoryRepository organizationResourceCategoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationResourceService.class);

	private final Long CATEGORY_ID = -1L;

	@Override
	public OrganizationResource createOrUpdateOrganizationResource(
			OrganizationResourcePayLoad organizationResourcePayLoad) {
		OrganizationResource organizationResource = null;

		try {
			if (null != organizationResourcePayLoad) {
				organizationResource = constructOrganizationResource(organizationResourcePayLoad);
				organizationResource = organizationResourceRepository.saveAndFlush(organizationResource);
			}
		} catch (Exception e) {
			if (null != organizationResourcePayLoad.getId()) {
				LOGGER.error(customMessageSource.getMessage("org.resource.exception.updated"), e);
			} else {
				LOGGER.error(customMessageSource.getMessage("org.resource.exception.created"), e);
			}

		}
		return organizationResource;

	}// end of method createOrUpdateOrganizationResource

	@Override
	public void removeOrganizationResource(Long resourceId) {
		OrganizationResource resource = organizationResourceRepository.findOrgResourceById(resourceId);
		if (null != resource) {
			resource.setIsActive(false);
		}

		organizationResourceRepository.saveAndFlush(resource);

	}// end of method removeOrganizationResource

	/**
	 * @param organizationResourcePayLoad
	 * @return
	 */
	private OrganizationResource constructOrganizationResource(
			OrganizationResourcePayLoad organizationResourcePayLoad) {
		OrganizationResource organizationResource = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = null;
		try {
			if (null != organizationResourcePayLoad.getId()) {
				organizationResource = organizationResourceRepository.getOne(organizationResourcePayLoad.getId());
			} else {
				organizationResource = new OrganizationResource();
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organizationResource.setCreatedAt(sdf.parse(formattedDte));
				organizationResource.setCreatedBy(OrganizationConstants.CREATED_BY);
			}

			if (organizationResource == null) {
				throw new OrganizationResourceException("Org resource record not found for Id: "
						+ organizationResourcePayLoad.getId() + " to update in DB ");
			} else {
				setOrganizationResourceCategory(organizationResourcePayLoad, organizationResource);
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organizationResource.setOrganizationId(organizationResourcePayLoad.getOrganizationId());
				organizationResource.setCount(organizationResourcePayLoad.getCount());
				organizationResource.setDescription(organizationResourcePayLoad.getDescription());
				organizationResource.setUpdatedAt(sdf.parse(formattedDte));
				organizationResource.setUpdatedBy(OrganizationConstants.UPDATED_BY);

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
	private void setOrganizationResourceCategory(OrganizationResourcePayLoad organizationResourcePayLoad,
			OrganizationResource organizationResource) {
		OrganizationResourceCategory organizationResourceCategory = null;
		try {
			if (null != organizationResourcePayLoad.getOrganizationResourceCategory()) {
				Long categoryId = organizationResourcePayLoad.getOrganizationResourceCategory().getId();
				if (null != categoryId) {
					if (categoryId.equals(CATEGORY_ID)) {
						organizationResourceCategory = saveOrganizationResourceCategory(
								organizationResourcePayLoad.getOrganizationResourceCategory());
						LOGGER.info(customMessageSource.getMessage("org.resource.category.success.created"));
						organizationResource.setOrganizationResourceCategory(organizationResourceCategory);

					} else {
						organizationResourceCategory = organizationResourceCategoryRepository.getOne(categoryId);
						if (organizationResourceCategory == null) {
							throw new OrganizationResourceCategoryException(
									"Org resource category record not found for Id: " + categoryId + " in DB ");
						}
						organizationResource.setOrganizationResourceCategory(organizationResourceCategory);

					}

				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.category.error.created"), e);
		}
	}

	public OrganizationResourceCategory saveOrganizationResourceCategory(
			OrganizationResourceCategoryPayLoad categoryFromPayLoad) {
		OrganizationResourceCategory category = new OrganizationResourceCategory();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
			if (!StringUtils.isEmpty(categoryFromPayLoad.getCategoryName())) {
				category.setCategoryName(categoryFromPayLoad.getCategoryName());
			}

			category.setCreatedAt(sdf.parse(formattedDte));
			category.setUpdatedAt(sdf.parse(formattedDte));
			category.setCreatedBy(OrganizationConstants.CREATED_BY);
			category.setUpdatedBy(OrganizationConstants.UPDATED_BY);
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.category.error.updated"), e);
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
