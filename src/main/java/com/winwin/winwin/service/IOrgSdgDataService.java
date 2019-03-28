package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.exception.OrgSdgDataException;
import com.winwin.winwin.payload.OrgSdgDataMapPayload;
import com.winwin.winwin.payload.OrgSdgGoalPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface IOrgSdgDataService {
	List<OrgSdgGoalPayload> getSdgDataForResponse();

	void createSdgDataMapping(List<OrgSdgDataMapPayload> payloadList, Long orgId) throws OrgSdgDataException;

	List<OrgSdgDataMapPayload> getSelectedSdgData(Long orgId);

}
