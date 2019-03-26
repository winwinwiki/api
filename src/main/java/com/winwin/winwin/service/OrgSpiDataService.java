/**
 * 
 */
package com.winwin.winwin.service;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrgSpiData;
import com.winwin.winwin.entity.OrgSpiDataMapping;
import com.winwin.winwin.payload.OrgSpiDataComponentsPayload;
import com.winwin.winwin.payload.OrgSpiDataDimensionsPayload;
import com.winwin.winwin.payload.OrgSpiDataIndicatorsPayload;
import com.winwin.winwin.payload.OrgSpiDataMapPayload;
import com.winwin.winwin.repository.OrgSpiDataMapRepository;
import com.winwin.winwin.repository.OrgSpiDataRepository;

/**
 * @author ArvindK
 *
 */
@Component
public class OrgSpiDataService implements IOrgSpiDataService {

	@Autowired
	OrgSpiDataRepository orgSpiDataRepository;

	@Autowired
	OrgSpiDataMapRepository orgSpiDataMapRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgSpiDataService.class);

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
	}// end of method public List<OrgSpiDataDimensionsPayload>
		// getSpiDataForResponse(

	@Override
	public List<OrgSpiData> getSpiDimensionMasterList() {
		return orgSpiDataRepository.findAllSpiDimensionData();
	}

	@Override
	public void createSpiDataMapping(List<OrgSpiDataMapPayload> payloadList, Long orgId) {
		for (OrgSpiDataMapPayload spiDataMapPayload : payloadList) {
			try {
				OrgSpiDataMapping spiDataMap = new OrgSpiDataMapping();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				spiDataMap.setOrganizationId(orgId);
				spiDataMap.setDimensionId(spiDataMapPayload.getDimensionId());
				spiDataMap.setDimensionName(spiDataMapPayload.getDimensionName());
				spiDataMap.setComponentId(spiDataMapPayload.getComponentId());
				spiDataMap.setComponentName(spiDataMapPayload.getComponentName());
				spiDataMap.setIndicatorId(spiDataMapPayload.getIndicatorId());
				spiDataMap.setIndicatorName(spiDataMapPayload.getIndicatorName());
				spiDataMap.setCreatedAt(sdf.parse(formattedDte));
				spiDataMap.setUpdatedAt(sdf.parse(formattedDte));
				spiDataMap.setCreatedBy(OrganizationConstants.CREATED_BY);
				spiDataMap.setUpdatedBy(OrganizationConstants.UPDATED_BY);
				orgSpiDataMapRepository.saveAndFlush(spiDataMap);
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.spidata.exception.created"), e);
			}
		}

	}// end of method public void createSpiDataMapping(

	@Override
	public List<OrgSpiDataMapping> getSelectedSpiData() {

		return orgSpiDataMapRepository.findAllSpiMappedData();
	}

}
