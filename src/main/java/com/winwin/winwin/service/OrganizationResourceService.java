package com.winwin.winwin.service;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.OrganizationResourceCategory;
import com.winwin.winwin.exception.OrganizationResourceCategoryException;
import com.winwin.winwin.exception.OrganizationResourceException;
import com.winwin.winwin.payload.OrganizationResourceCategoryPayLoad;
import com.winwin.winwin.payload.OrganizationResourcePayLoad;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrgHistoryRepository;
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
	OrgHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationResourceService.class);

	private final Long CATEGORY_ID = -1L;

	@Override
	public OrganizationResource createOrUpdateOrganizationResource(OrganizationResourcePayLoad orgResourcePayLoad) {
		UserPayload user = getUserDetails();
		OrganizationResource organizationResource = null;
		try {
			if (null != orgResourcePayLoad && null != user) {
				organizationResource = constructOrganizationResource(orgResourcePayLoad);
				organizationResource = organizationResourceRepository.saveAndFlush(organizationResource);

				if (null != organizationResource.getId()) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

					OrganizationHistory orgHistory = new OrganizationHistory();
					orgHistory.setOrganizationId(organizationResource.getOrganizationId());
					orgHistory.setUpdatedAt(sdf.parse(formattedDte));
					orgHistory.setUpdatedBy(user.getUserDisplayName());
					orgHistory.setActionPerformed(OrganizationConstants.UPDATE_RESOURCE);
					orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
				}
			}
		} catch (Exception e) {
			if (null != orgResourcePayLoad.getId()) {
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
	 * @param orgResourcePayLoad
	 * @return
	 */
	private OrganizationResource constructOrganizationResource(OrganizationResourcePayLoad orgResourcePayLoad) {
		UserPayload user = getUserDetails();
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
			}

			if (organizationResource == null) {
				throw new OrganizationResourceException(
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
		UserPayload user = getUserDetails();
		OrganizationResourceCategory category = new OrganizationResourceCategory();
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
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.resource.category.error.updated"), e);
			}
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

	/**
	 * @param user
	 * @return
	 */
	private UserPayload getUserDetails() {
		UserPayload user = null;
		if (null != SecurityContextHolder.getContext() && null != SecurityContextHolder.getContext().getAuthentication()
				&& null != SecurityContextHolder.getContext().getAuthentication().getDetails()) {
			user = (UserPayload) SecurityContextHolder.getContext().getAuthentication().getDetails();

		}
		return user;
	}

}
