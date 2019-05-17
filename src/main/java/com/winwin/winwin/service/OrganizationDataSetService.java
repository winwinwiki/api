/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.payload.DataSetPayLoad;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationDataSetService {
	OrganizationDataSet createOrUpdateOrganizationDataSet(DataSetPayLoad orgDataSetPayLoad);

	void removeOrganizationDataSet(Long dataSetId);

	List<OrganizationDataSet> getOrganizationDataSetList(Long id);

	DataSetCategory getDataSetCategoryById(Long categoryId);

	List<DataSetCategory> getDataSetCategoryList();

}
