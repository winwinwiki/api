package com.winwin.winwin.service;

import java.util.Date;
import java.text.ParseException;
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
		OrganizationResource orgResource = null;
		try {
			if (null != orgResourcePayLoad && null != user) {
				orgResource = constructOrganizationResource(orgResourcePayLoad);
				orgResource = organizationResourceRepository.saveAndFlush(orgResource);

				if (null != orgResource && null != orgResource.getOrganizationId()) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

					createOrgHistory(user, orgResource.getOrganizationId(), sdf, formattedDte,
							OrganizationConstants.UPDATE, OrganizationConstants.RESOURCE, orgResource.getId());
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
	public void removeOrganizationResource(Long resourceId) {
		OrganizationResource resource = organizationResourceRepository.findOrgResourceById(resourceId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
		UserPayload user = getUserDetails();
		try {
			if (null != resource && null != user) {
				resource.setUpdatedAt(sdf.parse(formattedDte));
				resource.setUpdatedBy(user.getEmail());
				resource.setIsActive(false);

				organizationResourceRepository.saveAndFlush(resource);

				if (null != resource) {
					createOrgHistory(user, resource.getOrganizationId(), sdf, formattedDte,
							OrganizationConstants.DELETE, "", null);
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

	/**
	 * @param user
	 * @param orgId
	 * @param sdf
	 * @param formattedDte
	 * @param actionPerformed
	 * @param entity
	 * @param entityId
	 * @throws ParseException
	 */
	private void createOrgHistory(UserPayload user, Long orgId, SimpleDateFormat sdf, String formattedDte,
			String actionPerformed, String entity, Long entityId) throws ParseException {
		OrganizationHistory orgHistory = new OrganizationHistory();
		orgHistory.setOrganizationId(orgId);
		orgHistory.setEntityId(entityId);
		orgHistory.setEntity(entity);
		orgHistory.setUpdatedAt(sdf.parse(formattedDte));
		orgHistory.setUpdatedBy(user.getUserDisplayName());
		orgHistory.setActionPerformed(actionPerformed);
		orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
	}

}
