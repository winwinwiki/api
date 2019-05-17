package com.winwin.winwin.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.exception.DataSetCategoryException;
import com.winwin.winwin.exception.DataSetException;
import com.winwin.winwin.payload.DataSetCategoryPayLoad;
import com.winwin.winwin.payload.DataSetPayLoad;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.DataSetCategoryRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.service.OrganizationDataSetService;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.UserService;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class OrganizationDataSetServiceImpl implements OrganizationDataSetService {
	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;

	@Autowired
	DataSetCategoryRepository dataSetCategoryRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService orgHistoryService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataSetServiceImpl.class);

	private final Long CATEGORY_ID = -1L;

	@Override
	public OrganizationDataSet createOrUpdateOrganizationDataSet(DataSetPayLoad orgDataSetPayLoad) {
		UserPayload user = userService.getCurrentUserDetails();
		OrganizationDataSet organizationDataSet = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		try {
			if (null != orgDataSetPayLoad && null != user) {
				organizationDataSet = constructOrganizationDataSet(orgDataSetPayLoad, user);
				organizationDataSet = organizationDataSetRepository.saveAndFlush(organizationDataSet);

				if (null != organizationDataSet.getId()) {

					if (null != orgDataSetPayLoad.getId()) {
						orgHistoryService.createOrganizationHistory(user, organizationDataSet.getOrganizationId(), sdf,
								formattedDte, OrganizationConstants.UPDATE, OrganizationConstants.DATASET,
								organizationDataSet.getId(), organizationDataSet.getDescription());
					} else {
						orgHistoryService.createOrganizationHistory(user, organizationDataSet.getOrganizationId(), sdf,
								formattedDte, OrganizationConstants.CREATE, OrganizationConstants.DATASET,
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
		UserPayload user = userService.getCurrentUserDetails();

		if (null != dataSet && null != user) {
			try {
				dataSet.setUpdatedAt(sdf.parse(formattedDte));
				dataSet.setUpdatedBy(user.getEmail());
				dataSet.setIsActive(false);

				dataSet = organizationDataSetRepository.saveAndFlush(dataSet);

				if (null != dataSet) {
					orgHistoryService.createOrganizationHistory(user, dataSet.getOrganizationId(), sdf, formattedDte,
							OrganizationConstants.DELETE, OrganizationConstants.DATASET, dataSet.getId(),
							dataSet.getDescription());
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
	private OrganizationDataSet constructOrganizationDataSet(DataSetPayLoad orgDataSetPayLoad, UserPayload user) {
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
				throw new DataSetException(
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
	private void setOrganizationDataSetCategory(DataSetPayLoad orgDataSetPayLoad, OrganizationDataSet orgDataSet,
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

	public DataSetCategory saveOrganizationDataSetCategory(DataSetCategoryPayLoad categoryFromPayLoad,
			UserPayload user) {
		DataSetCategory category = new DataSetCategory();
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

		return dataSetCategoryRepository.saveAndFlush(category);
	}// end of method saveOrganizationDataSetCategory

	@Override
	public DataSetCategory getDataSetCategoryById(Long categoryId) {
		return dataSetCategoryRepository.getOne(categoryId);
	}// end of method getOrganizationDataSetCategoryById

	@Override
	public List<DataSetCategory> getDataSetCategoryList() {
		return dataSetCategoryRepository.findAll();
	}// end of method getOrganizationDataSetCategoryList

}
