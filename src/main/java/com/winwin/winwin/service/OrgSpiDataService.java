/**
 * 
 */
package com.winwin.winwin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.winwin.winwin.entity.OrgSpiData;
import com.winwin.winwin.payload.OrgSpiDataComponentsPayload;
import com.winwin.winwin.payload.OrgSpiDataDimensionsPayload;
import com.winwin.winwin.payload.OrgSpiDataIndicatorsPayload;
import com.winwin.winwin.repository.OrgSpiDataRepository;

/**
 * @author ArvindK
 *
 */
@Component
public class OrgSpiDataService implements IOrgSpiDataService {

	@Autowired
	OrgSpiDataRepository orgSpiDataRepository;

	@Override
	public List<OrgSpiDataDimensionsPayload> getSpiDataForResponse() {
		List<OrgSpiDataDimensionsPayload> dimensionPayloadList = null;
		List<OrgSpiData> spiDimensionList = getSpiDimensionMasterList();
		if (null != spiDimensionList) {
			dimensionPayloadList = new ArrayList<OrgSpiDataDimensionsPayload>();
			for (OrgSpiData dimensionDataObj : spiDimensionList) {
				List<OrgSpiDataComponentsPayload> componentPayloadList = new ArrayList<OrgSpiDataComponentsPayload>();
				List<OrgSpiData> components = new ArrayList<OrgSpiData>();
				OrgSpiDataDimensionsPayload dimensionPayloadObj = new OrgSpiDataDimensionsPayload();
				dimensionPayloadObj.setDimensionId(dimensionDataObj.getId());
				dimensionPayloadObj.setDimensionName(dimensionDataObj.getName());
				components = orgSpiDataRepository.findAllComponentsById(dimensionDataObj.getId());
				if (null != components) {
					for (OrgSpiData componentDataObj : components) {
						List<OrgSpiData> indicators = new ArrayList<OrgSpiData>();
						OrgSpiDataComponentsPayload componentPayloadObj = new OrgSpiDataComponentsPayload();
						List<OrgSpiDataIndicatorsPayload> indPayloadList = new ArrayList<OrgSpiDataIndicatorsPayload>();
						componentPayloadObj.setComponentId(componentDataObj.getId());
						componentPayloadObj.setComponentName(componentDataObj.getName());
						indicators = orgSpiDataRepository.findAllIndicatorsById(componentDataObj.getId());
						if (null != indicators) {
							for (OrgSpiData obj : indicators) {
								OrgSpiDataIndicatorsPayload indPayloadObj = new OrgSpiDataIndicatorsPayload();
								indPayloadObj.setIndicatorId(obj.getId());
								indPayloadObj.setIndicatorName(obj.getName());
								indPayloadList.add(indPayloadObj);
							}
							componentPayloadObj.setIndicators(indPayloadList);

						}

						componentPayloadList.add(componentPayloadObj);
					}
				}
				dimensionPayloadObj.setComponents(componentPayloadList);
				dimensionPayloadList.add(dimensionPayloadObj);

			}

		}
		return dimensionPayloadList;
	}

	@Override
	public List<OrgSpiData> getSpiDimensionMasterList() {
		return orgSpiDataRepository.findAllSpiDimensionData();
	}

}
