package com.winwin.winwin.service;

import java.sql.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationDataSetCategory;
import com.winwin.winwin.exception.OrganizationDataSetCategoryException;
import com.winwin.winwin.exception.OrganizationDataSetException;
import com.winwin.winwin.payload.OrganizationDataSetPayLoad;
import com.winwin.winwin.repository.OrganizationDataSetCategoryRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindK
 *
 */
@Component
public class OrganizationDataSetService implements IOrganizationDataSetService {
	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;

	@Autowired
	OrganizationDataSetCategoryRepository organizationDataSetCategoryRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataSetService.class);

	private final Long CATEGORY_ID = -1L;

	@Override
	public OrganizationDataSet createOrUpdateOrganizationDataSet(
			OrganizationDataSetPayLoad organizationDataSetPayLoad) {
		OrganizationDataSet organizationDataSet = null;

		try {
			if (null != organizationDataSetPayLoad) {
				organizationDataSet = constructOrganizationDataSet(organizationDataSetPayLoad);

				organizationDataSetRepository.saveAndFlush(organizationDataSet);
				if (null != organizationDataSetPayLoad.getId()) {
					return organizationDataSet;
				}

			}
		} catch (Exception e) {
			if (null != organizationDataSetPayLoad.getId()) {
				LOGGER.error("Exception occured while updating of organization data set", e);
			} else {
				LOGGER.error("Exception occured while creating organization data set", e);
			}

		}
		organizationDataSet = organizationDataSetRepository.findLastOrgDataSet();
		return organizationDataSet;

	}// end of method createOrUpdateOrganizationDataSet

	@Override
	public void removeOrganizationDataSet(Long dataSetId) {
		organizationDataSetRepository.deleteById(dataSetId);

	}// end of method removeOrganizationDataSet

	/**
	 * @param organizationDataSetPayLoad
	 * @return
	 */
	private OrganizationDataSet constructOrganizationDataSet(OrganizationDataSetPayLoad organizationDataSetPayLoad) {
		OrganizationDataSet organizationDataSet = null;
		try {
			if (null != organizationDataSetPayLoad.getId()) {
				organizationDataSet = organizationDataSetRepository.getOne(organizationDataSetPayLoad.getId());
			} else {
				organizationDataSet = new OrganizationDataSet();
				organizationDataSet.setCreatedAt(new Date(System.currentTimeMillis()));
			}

			if (organizationDataSet == null) {
				throw new OrganizationDataSetException("Org dataset record not found for Id: "
						+ organizationDataSetPayLoad.getId() + " to update in DB ");
			} else {
				setOrganizationDataSetCategory(organizationDataSetPayLoad, organizationDataSet);

				organizationDataSet.setOrganizationId(organizationDataSetPayLoad.getOrganization_id());
				organizationDataSet.setDescription(organizationDataSetPayLoad.getDescription());
				organizationDataSet.setType(organizationDataSetPayLoad.getType());
				organizationDataSet.setUrl(organizationDataSetPayLoad.getUrl());
				organizationDataSet.setUpdatedAt(new Date(System.currentTimeMillis()));

			}

		} catch (Exception e) {
			LOGGER.error("Exception occured during construction of organization data set", e);
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
	private void setOrganizationDataSetCategory(OrganizationDataSetPayLoad organizationDataSetPayLoad,
			OrganizationDataSet organizationDataSet) {
		OrganizationDataSetCategory organizationDataSetCategory = null;
		;
		try {
			if (null != organizationDataSetPayLoad.getOrganizationDataSetCategory()) {
				Long categoryId = organizationDataSetPayLoad.getOrganizationDataSetCategory().getId();
				if (null != categoryId) {
					if (categoryId == CATEGORY_ID) {
						organizationDataSetCategory = saveOrganizationDataSetCategory(
								organizationDataSetPayLoad.getOrganizationDataSetCategory());
						LOGGER.info("oraganization dataset category has been created successfully in DB");
						organizationDataSet.setOrganizationDataSetCategory(organizationDataSetCategory);

					} else {
						organizationDataSetCategory = organizationDataSetCategoryRepository.getOne(categoryId);
						if (organizationDataSetCategory == null) {
							throw new OrganizationDataSetCategoryException(
									"Org dataset category record not found for Id: " + categoryId + " in DB ");
						} else {
							organizationDataSet.setOrganizationDataSetCategory(organizationDataSetCategory);

						}
					}

				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured during creation of organization dataset category", e);
		}
	}

	public OrganizationDataSetCategory saveOrganizationDataSetCategory(
			OrganizationDataSetCategory categoryFromPayLoad) {
		OrganizationDataSetCategory category = new OrganizationDataSetCategory();
		try {
			if (!StringUtils.isEmpty(categoryFromPayLoad.getCategoryName())) {
				category.setCategoryName(categoryFromPayLoad.getCategoryName());
			}
			category.setCreatedAt(new Date(System.currentTimeMillis()));
			category.setUpdatedAt(new Date(System.currentTimeMillis()));
		} catch (Exception e) {
			LOGGER.error("Exception occured during updation of organization dataset category in DB", e);
		}

		return organizationDataSetCategoryRepository.saveAndFlush(category);
	}// end of method saveOrganizationDataSetCategory

	public OrganizationDataSetCategory getOrganizationDataSetCategoryById(Long categoryId) {
		return organizationDataSetCategoryRepository.getOne(categoryId);
	}// end of method getOrganizationDataSetCategoryById

	public List<OrganizationDataSetCategory> getOrganizationDataSetCategoryList() {
		return organizationDataSetCategoryRepository.findAll();
	}// end of method getOrganizationDataSetCategoryList

}
