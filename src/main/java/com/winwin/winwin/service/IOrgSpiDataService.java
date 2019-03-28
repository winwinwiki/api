package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrgSpiData;
import com.winwin.winwin.entity.OrgSpiDataMapping;
import com.winwin.winwin.payload.OrgSpiDataDimensionsPayload;
import com.winwin.winwin.payload.OrgSpiDataMapPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface IOrgSpiDataService {
	List<OrgSpiDataDimensionsPayload> getSpiDataForResponse();

	void createSpiDataMapping(List<OrgSpiDataMapPayload> payloadList, Long orgId);

	List<OrgSpiDataMapping> getSelectedSpiData();

	List<OrgSpiData> getSpiDimensionMasterList();

}
