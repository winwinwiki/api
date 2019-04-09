/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrgRegionMaster;
import com.winwin.winwin.entity.OrgRegionServed;
import com.winwin.winwin.payload.OrgRegionServedPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface IOrgRegionServedService {
	List<OrgRegionServed> createOrgRegionServed(List<OrgRegionServedPayload> orgRegionPayloadList);

	List<OrgRegionServed> getOrgRegionServedList();

	List<OrgRegionMaster> getOrgRegionMasterList();

}
