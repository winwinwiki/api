/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.entity.OrganizationRegionServed;
import com.winwin.winwin.payload.OrganizationRegionServedPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationRegionServedService {
	List<OrganizationRegionServed> createOrgRegionServed(List<OrganizationRegionServedPayload> orgRegionPayloadList);

	List<OrganizationRegionServed> getOrgRegionServedList( Long orgId);

	List<RegionMaster> getOrgRegionMasterList();

}
