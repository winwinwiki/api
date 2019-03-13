/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.payload.OrganizationDataSetPayLoad;

/**
 * @author ArvindK
 *
 */
public interface IOrganizationDataSetService {
	void createOrUpdateOrganizationDataSet(OrganizationDataSetPayLoad organizationDataSetPayLoad);

	void removeOrganizationDataSet(Long dataSetId);

	List<OrganizationDataSet> getOrganizationDataSetList();

}