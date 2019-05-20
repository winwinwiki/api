/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.payload.DataSetPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationDataSetService {
	OrganizationDataSet createOrUpdateOrganizationDataSet(DataSetPayload orgDataSetPayLoad);

	void removeOrganizationDataSet(Long dataSetId);

	List<OrganizationDataSet> getOrganizationDataSetList(Long id);

	DataSetCategory getDataSetCategoryById(Long categoryId);

	List<DataSetCategory> getDataSetCategoryList();

}
