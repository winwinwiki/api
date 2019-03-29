package com.winwin.winwin.service;

import java.util.List;
import com.winwin.winwin.exception.OrgSpiDataException;
import com.winwin.winwin.payload.OrgSpiDataDimensionsPayload;
import com.winwin.winwin.payload.OrgSpiDataMapPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface IOrgSpiDataService {
	List<OrgSpiDataDimensionsPayload> getSpiDataForResponse();

	void createSpiDataMapping(List<OrgSpiDataMapPayload> payloadList, Long orgId) throws OrgSpiDataException;

	List<OrgSpiDataMapPayload> getSelectedSpiData(Long orgId);

}
