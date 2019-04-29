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
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationDataSetCategory;
import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.exception.OrganizationDataSetCategoryException;
import com.winwin.winwin.exception.OrganizationDataSetException;
import com.winwin.winwin.payload.OrganizationDataSetCategoryPayLoad;
import com.winwin.winwin.payload.OrganizationDataSetPayLoad;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrgHistoryRepository;
import com.winwin.winwin.repository.OrganizationDataSetCategoryRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class OrganizationDataSetService implements IOrganizationDataSetService {
	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;

	@Autowired
	OrganizationDataSetCategoryRepository organizationDataSetCategoryRepository;

	@Autowired
	OrgHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataSetService.class);

	private final Long CATEGORY_ID = -1L;

	@Override
	public OrganizationDataSet createOrUpdateOrganizationDataSet(OrganizationDataSetPayLoad orgDataSetPayLoad) {
		UserPayload user = getUserDetails();
		OrganizationDataSet organizationDataSet = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		try {
			if (null != orgDataSetPayLoad && null != user) {
				organizationDataSet = constructOrganizationDataSet(orgDataSetPayLoad, user);
				organizationDataSet = organizationDataSetRepository.saveAndFlush(organizationDataSet);

				if (null != organizationDataSet.getId()) {

					if (null != orgDataSetPayLoad.getId()) {
						createOrgHistory(user, organizationDataSet.getOrganizationId(), sdf, formattedDte,
								OrganizationConstants.UPDATE, OrganizationConstants.DATASET,
								organizationDataSet.getId(), organizationDataSet.getDescription());
					} else {
						createOrgHistory(user, organizationDataSet.getOrganizationId(), sdf, formattedDte,
								OrganizationConstants.CREATE, OrganizationConstants.DATASET,
								organizationDataSet.getId(), organizationDataSet.getDescription());
					}

				}

			}
		} catch (Exception e) {
			if (null != orgDataSetPayLoad.getId()) {
				LOGGER.error(customMessageSource.getMessage("org.dataset.exception.updated"), e);
			} else {
				LOGGER.error(customMessageSource.getMessage("org.dataset.exception.created"), e);
			}

		}
		return organizationDataSet;

	}// end of method createOrUpdateOrganizationDataSet

	@Override
	public void removeOrganizationDataSet(Long dataSetId) {
		OrganizationDataSet dataSet = organizationDataSetRepository.findOrgDataSetById(dataSetId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
		UserPayload user = getUserDetails();

		if (null != dataSet && null != user) {
			try {
				dataSet.setUpdatedAt(sdf.parse(formattedDte));
				dataSet.setUpdatedBy(user.getEmail());
				dataSet.setIsActive(false);

				dataSet = organizationDataSetRepository.saveAndFlush(dataSet);

				if (null != dataSet) {
					createOrgHistory(user, dataSet.getOrganizationId(), sdf, formattedDte, OrganizationConstants.DELETE,
							OrganizationConstants.DATASET, dataSet.getId(), dataSet.getDescription());
				}
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.dataset.error.deleted"), e);
			}

		}

	}// end of method removeOrganizationDataSet

	/**
	 * @param organizationDataSetPayLoad
	 * @return
	 */
	private OrganizationDataSet constructOrganizationDataSet(OrganizationDataSetPayLoad orgDataSetPayLoad,
			UserPayload user) {
		OrganizationDataSet organizationDataSet = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = null;
		try {
			if (null != orgDataSetPayLoad.getId()) {
				organizationDataSet = organizationDataSetRepository.getOne(orgDataSetPayLoad.getId());
			} else {
				organizationDataSet = new OrganizationDataSet();
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organizationDataSet.setCreatedAt(sdf.parse(formattedDte));
				organizationDataSet.setCreatedBy(user.getEmail());
			}

			if (organizationDataSet == null) {
				throw new OrganizationDataSetException(
						"Org dataset record not found for Id: " + orgDataSetPayLoad.getId() + " to update in DB ");
			} else {
				setOrganizationDataSetCategory(orgDataSetPayLoad, organizationDataSet, user);
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organizationDataSet.setOrganizationId(orgDataSetPayLoad.getOrganizationId());
				organizationDataSet.setDescription(orgDataSetPayLoad.getDescription());
				organizationDataSet.setType(orgDataSetPayLoad.getType());
				organizationDataSet.setUrl(orgDataSetPayLoad.getUrl());
				organizationDataSet.setUpdatedAt(sdf.parse(formattedDte));
				organizationDataSet.setUpdatedBy(user.getEmail());

			}

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.exception.construct"), e);
		}

		return organizationDataSet;
	}

	@Override
	public List<OrganizationDataSet> getOrganizationDataSetList(Long id) {
		return organizationDataSetRepository.findAllOrgDataSetList(id);
	}// end of method getOrganizationDataSetList

	/**
	 * @param organizationDataSetPayLoad
	 * @param organizationDataSet
	 */
	private void setOrganizationDataSetCategory(OrganizationDataSetPayLoad orgDataSetPayLoad,
			OrganizationDataSet orgDataSet, UserPayload user) {
		OrganizationDataSetCategory organizationDataSetCategory = null;
		try {
			if (null != orgDataSetPayLoad.getOrganizationDataSetCategory()) {
				Long categoryId = orgDataSetPayLoad.getOrganizationDataSetCategory().getId();
				if (null != categoryId) {
					if (categoryId.equals(CATEGORY_ID)) {
						organizationDataSetCategory = saveOrganizationDataSetCategory(
								orgDataSetPayLoad.getOrganizationDataSetCategory(), user);
						LOGGER.info(customMessageSource.getMessage("org.dataset.category.success.created"));
						orgDataSet.setOrganizationDataSetCategory(organizationDataSetCategory);

					} else {
						organizationDataSetCategory = organizationDataSetCategoryRepository.getOne(categoryId);
						if (organizationDataSetCategory == null) {
							throw new OrganizationDataSetCategoryException(
									"Org dataset category record not found for Id: " + categoryId + " in DB ");
						} else {
							orgDataSet.setOrganizationDataSetCategory(organizationDataSetCategory);

						}
					}

				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.category.error.created"), e);
		}
	}

	public OrganizationDataSetCategory saveOrganizationDataSetCategory(
			OrganizationDataSetCategoryPayLoad categoryFromPayLoad, UserPayload user) {
		OrganizationDataSetCategory category = new OrganizationDataSetCategory();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
			if (!StringUtils.isEmpty(categoryFromPayLoad.getCategoryName())) {
				category.setCategoryName(categoryFromPayLoad.getCategoryName());
			}
			category.setCreatedAt(sdf.parse(formattedDte));
			category.setUpdatedAt(sdf.parse(formattedDte));
			category.setCreatedBy(user.getEmail());
			category.setUpdatedBy(user.getEmail());
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.category.error.updated"), e);
		}

		return organizationDataSetCategoryRepository.saveAndFlush(category);
	}// end of method saveOrganizationDataSetCategory

	public OrganizationDataSetCategory getOrganizationDataSetCategoryById(Long categoryId) {
		return organizationDataSetCategoryRepository.getOne(categoryId);
	}// end of method getOrganizationDataSetCategoryById

	public List<OrganizationDataSetCategory> getOrganizationDataSetCategoryList() {
		return organizationDataSetCategoryRepository.findAll();
	}// end of method getOrganizationDataSetCategoryList

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
	 * @param entityType
	 * @param entityId
	 * @param entityName
	 * @throws ParseException
	 */
	private void createOrgHistory(UserPayload user, Long orgId, SimpleDateFormat sdf, String formattedDte,
			String actionPerformed, String entityType, Long entityId, String entityName) throws ParseException {
		OrganizationHistory orgHistory = new OrganizationHistory();
		orgHistory.setOrganizationId(orgId);
		orgHistory.setEntityId(entityId);
		orgHistory.setEntityName(entityName);
		orgHistory.setEntityType(entityType);
		orgHistory.setUpdatedAt(sdf.parse(formattedDte));
		orgHistory.setUpdatedBy(user.getUserDisplayName());
		orgHistory.setActionPerformed(actionPerformed);
		orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
	}


}
