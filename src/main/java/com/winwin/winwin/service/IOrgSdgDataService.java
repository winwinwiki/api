package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrgSdgDataMapping;
import com.winwin.winwin.payload.OrgSdgDataMapPayload;
import com.winwin.winwin.payload.OrgSdgGoalPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface IOrgSdgDataService {
	List<OrgSdgGoalPayload> getSdgDataForResponse();

	void createSdgDataMapping(List<OrgSdgDataMapPayload> payloadList, Long orgId);

	List<OrgSdgDataMapping> getSelectedSdgData();

}
