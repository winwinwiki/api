/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrgRegionServed;
import com.winwin.winwin.payload.OrgRegionServedPayload;

/**
 * @author ArvindK
 *
 */
public interface IOrgRegionServedService {
	List<OrgRegionServed> createOrgRegionServed(List<OrgRegionServedPayload> orgRegionPayloadList);

	OrgRegionServed updateOrgRegionServed(OrgRegionServedPayload orgRegionServedPayload,
			OrgRegionServed orgRegionServed);

	List<OrgRegionServed> getOrgRegionServedList();

}
