package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrgSpiData;
import com.winwin.winwin.payload.OrgSpiDataDimensionsPayload;

/**
 * @author ArvindK
 *
 */
public interface IOrgSpiDataService {
	List<OrgSpiDataDimensionsPayload> getSpiDataForResponse();
	List<OrgSpiData> getSpiDimensionMasterList();

}
