/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.payload.OrganizationDataSetPayLoad;

/**
 * @author ArvindKhatik
 *
 */
public interface IOrganizationDataSetService {
	OrganizationDataSet createOrUpdateOrganizationDataSet(OrganizationDataSetPayLoad organizationDataSetPayLoad);

	void removeOrganizationDataSet(Long dataSetId);

	List<OrganizationDataSet> getOrganizationDataSetList(Long id);

}
