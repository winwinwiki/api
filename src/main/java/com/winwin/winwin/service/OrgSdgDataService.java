package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.exception.SdgDataException;
import com.winwin.winwin.payload.OrganizationSdgDataMapPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface OrgSdgDataService {

	void createSdgDataMapping(List<OrganizationSdgDataMapPayload> payloadList, Long orgId) throws SdgDataException;

	List<OrganizationSdgDataMapPayload> getSelectedSdgData(Long orgId);

}
