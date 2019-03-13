package com.winwin.winwin.service;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationDataSetCategory;
import com.winwin.winwin.payload.OrganizationDataSetPayLoad;
import com.winwin.winwin.repository.OrganizationDataSetCategoryRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;

/**
 * @author ArvindK
 *
 */
public class OrganizationDataSetService implements IOrganizationDataSetService {
	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;

	@Autowired
	OrganizationDataSetCategoryRepository organizationDataSetCategoryRepository;

	private final Long CATEGORY_ID = -1L;

	@Override
	public void createOrUpdateOrganizationDataSet(OrganizationDataSetPayLoad organizationDataSetPayLoad) {

		try {
			if (null != organizationDataSetPayLoad) {
				OrganizationDataSet organizationDataSet = constructOrganizationDataSet(organizationDataSetPayLoad);

				organizationDataSetRepository.saveAndFlush(organizationDataSet);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		OrganizationDataSet organizationDataSet;
		if (null != organizationDataSetPayLoad.getId()) {
			organizationDataSet = organizationDataSetRepository.getOne(organizationDataSetPayLoad.getOrganization_id());
		} else {
			organizationDataSet = new OrganizationDataSet();
			organizationDataSet.setCreatedAt(new Date(System.currentTimeMillis()));
		}

		setOrganizationDataSetCategory(organizationDataSetPayLoad, organizationDataSet);

		organizationDataSet.setOrganizationId(organizationDataSetPayLoad.getOrganization_id());
		organizationDataSet.setDescription(organizationDataSetPayLoad.getDescription());
		organizationDataSet.setType(organizationDataSetPayLoad.getType());
		organizationDataSet.setUrl(organizationDataSetPayLoad.getUrl());
		organizationDataSet.setUpdatedAt(new Date(System.currentTimeMillis()));

		return organizationDataSet;
	}

	@Override
	public List<OrganizationDataSet> getOrganizationDataSetList() {
		return organizationDataSetRepository.findAll();
	}// end of method getOrganizationDataSetList

	/**
	 * @param organizationDataSetPayLoad
	 * @param organizationDataSet
	 */
	private void setOrganizationDataSetCategory(OrganizationDataSetPayLoad organizationDataSetPayLoad,
			OrganizationDataSet organizationDataSet) {
		OrganizationDataSetCategory organizationDataSetCategory;
		if (null != organizationDataSetPayLoad.getOrganizationDataSetCategory()) {
			if (null != organizationDataSetPayLoad.getOrganizationDataSetCategory().getId()) {
				if (organizationDataSetPayLoad.getOrganizationDataSetCategory().getId() == CATEGORY_ID) {
					organizationDataSetCategory = saveOrganizationDataSetCategory(
							organizationDataSetPayLoad.getOrganizationDataSetCategory());

					organizationDataSet.setOrganizationDataSetCategory(organizationDataSetCategory);

				} else {
					organizationDataSetCategory = getOrganizationDataSetCategoryById(
							organizationDataSetPayLoad.getOrganizationDataSetCategory().getId());
					organizationDataSet.setOrganizationDataSetCategory(organizationDataSetCategory);
				}

			}
		}
	}

	public OrganizationDataSetCategory saveOrganizationDataSetCategory(
			OrganizationDataSetCategory categoryFromPayLoad) {
		OrganizationDataSetCategory category = new OrganizationDataSetCategory();
		category.setCategoryName(categoryFromPayLoad.getCategoryName());
		category.setCreatedAt(new Date(System.currentTimeMillis()));
		category.setUpdatedAt(new Date(System.currentTimeMillis()));

		return organizationDataSetCategoryRepository.saveAndFlush(category);
	}// end of method saveOrganizationDataSetCategory

	public OrganizationDataSetCategory getOrganizationDataSetCategoryById(Long categoryId) {
		return organizationDataSetCategoryRepository.getOne(categoryId);
	}// end of method getOrganizationDataSetCategoryById

	public List<OrganizationDataSetCategory> getOrganizationDataSetCategoryList() {
		return organizationDataSetCategoryRepository.findAll();
	}// end of method getOrganizationDataSetCategoryList

}
