package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.exception.SpiDataException;
import com.winwin.winwin.payload.OrganizationSpiDataMapPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface OrgSpiDataService {

	void createSpiDataMapping(List<OrganizationSpiDataMapPayload> payloadList, Long orgId) throws SpiDataException;

	List<OrganizationSpiDataMapPayload> getSelectedSpiData(Long orgId);

}
