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
import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.exception.DataSetCategoryException;
import com.winwin.winwin.exception.DataSetException;
import com.winwin.winwin.payload.DataSetCategoryPayload;
import com.winwin.winwin.payload.DataSetPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.DataSetCategoryRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.service.OrganizationDataSetService;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class OrganizationDataSetServiceImpl implements OrganizationDataSetService {
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private OrganizationDataSetRepository organizationDataSetRepository;
	@Autowired
	private DataSetCategoryRepository dataSetCategoryRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationHistoryService orgHistoryService;
	@Autowired
	private CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataSetServiceImpl.class);

	private final Long CATEGORY_ID = -1L;

	/**
	 * create or update OrganizationDataSet and DataSetCategory, create new
	 * entry in DataSetCategory if the value of CATEGORY_ID is -1L;
	 * 
	 * @param orgDataSetPayLoad
	 */
	@Override
	@Transactional
	@CacheEvict(value = "organization_dataset_list,organization_dataset_category_list")
	public OrganizationDataSet createOrUpdateOrganizationDataSet(DataSetPayload orgDataSetPayLoad) {
		OrganizationDataSet organizationDataSet = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (null != orgDataSetPayLoad && null != user) {
				organizationDataSet = constructOrganizationDataSet(orgDataSetPayLoad, user);
				organizationDataSet = organizationDataSetRepository.saveAndFlush(organizationDataSet);

				if (null != organizationDataSet.getId() && null != organizationDataSet.getOrganization()) {
					if (null != orgDataSetPayLoad.getId()) {
						orgHistoryService.createOrganizationHistory(user, organizationDataSet.getOrganization().getId(),
								OrganizationConstants.UPDATE, OrganizationConstants.DATASET,
								organizationDataSet.getId(), organizationDataSet.getDataSetCategory().getCategoryName(),
								"");
					} else {
						orgHistoryService.createOrganizationHistory(user, organizationDataSet.getOrganization().getId(),
								OrganizationConstants.CREATE, OrganizationConstants.DATASET,
								organizationDataSet.getId(), organizationDataSet.getDataSetCategory().getCategoryName(),
								"");
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

	/**
	 * delete OrganizationDataSet by Id
	 * 
	 * @param dataSetId
	 */
	@Override
	@Transactional
	@CacheEvict(value = "organization_dataset_list,organization_dataset_category_list")
	public void removeOrganizationDataSet(Long dataSetId) {
		OrganizationDataSet dataSet = organizationDataSetRepository.findOrgDataSetById(dataSetId);
		UserPayload user = userService.getCurrentUserDetails();

		if (null != dataSet && null != user) {
			try {
				Date date = CommonUtils.getFormattedDate();
				dataSet.setUpdatedAt(date);
				dataSet.setUpdatedBy(user.getUserDisplayName());
				dataSet.setUpdatedByEmail(user.getEmail());
				dataSet.setIsActive(false);
				dataSet = organizationDataSetRepository.saveAndFlush(dataSet);

				if (null != dataSet && null != dataSet.getOrganization()) {
					orgHistoryService.createOrganizationHistory(user, dataSet.getOrganization().getId(),
							OrganizationConstants.DELETE, OrganizationConstants.DATASET, dataSet.getId(),
							dataSet.getDataSetCategory().getCategoryName(), "");
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
	private OrganizationDataSet constructOrganizationDataSet(DataSetPayload orgDataSetPayLoad, UserPayload user) {
		OrganizationDataSet organizationDataSet = null;
		try {
			Date date = CommonUtils.getFormattedDate();
			if (null != orgDataSetPayLoad.getId()) {
				organizationDataSet = organizationDataSetRepository.getOne(orgDataSetPayLoad.getId());
				if (organizationDataSet == null)
					throw new DataSetException(
							"Org dataset record not found for Id: " + orgDataSetPayLoad.getId() + " to update in DB ");
			} else {
				organizationDataSet = new OrganizationDataSet();
				organizationDataSet.setCreatedAt(date);
				organizationDataSet.setCreatedBy(user.getUserDisplayName());
				organizationDataSet.setCreatedByEmail(user.getEmail());
			}
			// set Organization DataSetCategory
			setOrganizationDataSetCategory(orgDataSetPayLoad, organizationDataSet, user);
			BeanUtils.copyProperties(orgDataSetPayLoad, organizationDataSet);
			organizationDataSet.setIsActive(true);
			organizationDataSet.setUpdatedAt(date);
			organizationDataSet.setUpdatedBy(user.getUserDisplayName());
			organizationDataSet.setUpdatedByEmail(user.getEmail());

			if (null != orgDataSetPayLoad.getOrganizationId()) {
				Organization organization = organizationRepository.findOrgById(orgDataSetPayLoad.getOrganizationId());
				organizationDataSet.setOrganization(organization);
			}

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.exception.construct"), e);
		}
		return organizationDataSet;
	}

	/**
	 * returns DataSetCategory by Id
	 * 
	 * @param categoryId
	 */
	@Override
	public DataSetCategory getDataSetCategoryById(Long categoryId) {
		return dataSetCategoryRepository.getOne(categoryId);
	}// end of method getOrganizationDataSetCategoryById

	/**
	 * returns OrganizationDataSet List by OrgId
	 * 
	 * @param id
	 */
	@Override
	@Cacheable("organization_dataset_list")
	public List<OrganizationDataSet> getOrganizationDataSetList(Long id) {
		return organizationDataSetRepository.findAllOrgDataSetList(id);
	}// end of method getOrganizationDataSetList

	/**
	 * returns DataSetCategory List
	 * 
	 * @param categoryId
	 */
	@Override
	@Cacheable("organization_dataset_category_list")
	public List<DataSetCategory> getDataSetCategoryList() {
		return dataSetCategoryRepository.findAll();
	}// end of method getOrganizationDataSetCategoryList

	/**
	 * @param organizationDataSetPayLoad
	 * @param organizationDataSet
	 */
	private void setOrganizationDataSetCategory(DataSetPayload orgDataSetPayLoad, OrganizationDataSet orgDataSet,
			UserPayload user) {
		DataSetCategory organizationDataSetCategory = null;
		try {
			if (null != orgDataSetPayLoad.getDataSetCategory()) {
				Long categoryId = orgDataSetPayLoad.getDataSetCategory().getId();
				if (null != categoryId) {
					if (categoryId.equals(CATEGORY_ID)) {
						organizationDataSetCategory = saveOrganizationDataSetCategory(
								orgDataSetPayLoad.getDataSetCategory(), user);
						LOGGER.info(customMessageSource.getMessage("org.dataset.category.success.created"));
						orgDataSet.setDataSetCategory(organizationDataSetCategory);
					} else {
						organizationDataSetCategory = dataSetCategoryRepository.getOne(categoryId);
						if (organizationDataSetCategory == null) {
							throw new DataSetCategoryException(
									"Org dataset category record not found for Id: " + categoryId + " in DB ");
						} else {
							orgDataSet.setDataSetCategory(organizationDataSetCategory);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.category.error.created"), e);
		}
	}

	@Transactional
	private DataSetCategory saveOrganizationDataSetCategory(DataSetCategoryPayload categoryFromPayLoad,
			UserPayload user) {
		DataSetCategory category = new DataSetCategory();
		try {
			if (!StringUtils.isEmpty(categoryFromPayLoad.getCategoryName())) {
				category.setCategoryName(categoryFromPayLoad.getCategoryName());
			}
			Date date = CommonUtils.getFormattedDate();
			category.setCreatedAt(date);
			category.setUpdatedAt(date);
			category.setCreatedBy(user.getUserDisplayName());
			category.setUpdatedBy(user.getUserDisplayName());
			category.setCreatedByEmail(user.getEmail());
			category.setUpdatedByEmail(user.getEmail());
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.category.error.updated"), e);
		}

		return dataSetCategoryRepository.saveAndFlush(category);
	}// end of method saveOrganizationDataSetCategory

}
