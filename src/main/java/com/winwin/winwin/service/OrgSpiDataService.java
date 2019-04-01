/**
 * 
 */
package com.winwin.winwin.service;

import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrgSpiData;
import com.winwin.winwin.entity.OrgSpiDataMapping;
import com.winwin.winwin.exception.OrgSpiDataException;
import com.winwin.winwin.payload.OrgSpiDataComponentsPayload;
import com.winwin.winwin.payload.OrgSpiDataDimensionsPayload;
import com.winwin.winwin.payload.OrgSpiDataIndicatorsPayload;
import com.winwin.winwin.payload.OrgSpiDataMapPayload;
import com.winwin.winwin.repository.OrgSpiDataMapRepository;
import com.winwin.winwin.repository.OrgSpiDataRepository;

/**
 * @author ArvindKhatik
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
		List<OrgSpiData> spiList = orgSpiDataRepository.findAll();
		if (null != spiList) {
			HashMap<Long, List<OrgSpiData>> spiDimensionsMap = new HashMap<Long, List<OrgSpiData>>();
			setSpiDimensionsMap(spiList, spiDimensionsMap);

			if ((!spiDimensionsMap.isEmpty())) {
				dimensionPayloadList = new ArrayList<OrgSpiDataDimensionsPayload>();
				for (List<OrgSpiData> spiComponentsList : spiDimensionsMap.values()) {
					OrgSpiDataDimensionsPayload dimensionPayloadObj = new OrgSpiDataDimensionsPayload();
					dimensionPayloadObj.setDimensionId(spiComponentsList.get(0).getDimensionId());
					dimensionPayloadObj.setDimensionName(spiComponentsList.get(0).getDimensionName());

					HashMap<String, List<OrgSpiData>> spiComponentsMap = new HashMap<String, List<OrgSpiData>>();
					setSpiComponentsMap(spiComponentsList, spiComponentsMap);

					HashMap<String, List<OrgSpiDataIndicatorsPayload>> spiIndicatorsMap = new HashMap<String, List<OrgSpiDataIndicatorsPayload>>();
					setSpiIndicatorsMap(spiComponentsList, spiIndicatorsMap);

					List<OrgSpiDataComponentsPayload> componentPayloadList = new ArrayList<OrgSpiDataComponentsPayload>();
					for (List<OrgSpiData> splittedComponentsList : spiComponentsMap.values()) {
						OrgSpiDataComponentsPayload componentPayloadObj = new OrgSpiDataComponentsPayload();
						componentPayloadObj.setComponentId(splittedComponentsList.get(0).getComponentId());
						componentPayloadObj.setComponentName(splittedComponentsList.get(0).getComponentName());
						componentPayloadObj
								.setIndicators(spiIndicatorsMap.get(splittedComponentsList.get(0).getComponentId()));
						componentPayloadList.add(componentPayloadObj);
					}

					dimensionPayloadObj.setComponents(componentPayloadList);
					dimensionPayloadList.add(dimensionPayloadObj);
				} // end of loop

			} // end of if ((!spiDimensionsMap.isEmpty()))

		} // end of if (null != spiList) {

		return dimensionPayloadList;
	}// end of method public List<OrgSpiDataDimensionsPayload>
		// getSpiDataForResponse(

	/**
	 * @param spiList
	 * @param spiDimensionsMap
	 */
	private void setSpiDimensionsMap(List<OrgSpiData> spiList, HashMap<Long, List<OrgSpiData>> spiDimensionsMap) {
		for (OrgSpiData spiDataObj : spiList) {
			if (!spiDimensionsMap.containsKey(spiDataObj.getDimensionId())) {
				List<OrgSpiData> components = new ArrayList<OrgSpiData>();
				components.add(spiDataObj);

				spiDimensionsMap.put(spiDataObj.getDimensionId(), components);
			} else {
				spiDimensionsMap.get(spiDataObj.getDimensionId()).add(spiDataObj);
			}
		}
	}// end of method setSpiDimensionsMap

	/**
	 * @param spiList
	 * @param spiComponentsMap
	 */
	private void setSpiComponentsMap(List<OrgSpiData> spiList, HashMap<String, List<OrgSpiData>> spiComponentsMap) {
		for (OrgSpiData spiDataObj : spiList) {
			if (!spiComponentsMap.containsKey(spiDataObj.getComponentId())) {
				List<OrgSpiData> components = new ArrayList<OrgSpiData>();
				components.add(spiDataObj);

				spiComponentsMap.put(spiDataObj.getComponentId(), components);
			} else {
				spiComponentsMap.get(spiDataObj.getComponentId()).add(spiDataObj);
			}
		}
	}// end of method setSpiIndicatorsMap

	/**
	 * @param spiList
	 * @param spiIndicatorsMap
	 */
	private void setSpiIndicatorsMap(List<OrgSpiData> spiList,
			HashMap<String, List<OrgSpiDataIndicatorsPayload>> spiIndicatorsMap) {
		for (OrgSpiData spiDataObj : spiList) {
			OrgSpiDataIndicatorsPayload payload = new OrgSpiDataIndicatorsPayload();
			if (!StringUtils.isEmpty(spiDataObj.getIndicatorId())) {
				payload.setIndicatorId(spiDataObj.getIndicatorId());
				payload.setIndicatorName(spiDataObj.getIndicatorName());
			}

			if (!spiIndicatorsMap.containsKey(spiDataObj.getComponentId())) {
				List<OrgSpiDataIndicatorsPayload> indicators = new ArrayList<OrgSpiDataIndicatorsPayload>();
				indicators.add(payload);

				spiIndicatorsMap.put(spiDataObj.getComponentId(), indicators);
			} else {
				spiIndicatorsMap.get(spiDataObj.getComponentId()).add(payload);
			}
		}
	}// end of method setSpiIndicatorsMap

	@Override
	public void createSpiDataMapping(List<OrgSpiDataMapPayload> payloadList, Long orgId) throws OrgSpiDataException {
		if (null != payloadList) {
			for (OrgSpiDataMapPayload payload : payloadList) {
				try {
					OrgSpiDataMapping spiDataMapObj = new OrgSpiDataMapping();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
					spiDataMapObj.setOrganizationId(orgId);
					Long dId = payload.getDimensionId();
					String cId = payload.getComponentId();
					String indId = payload.getIndicatorId();

					if (null != dId && !(StringUtils.isEmpty(cId)) && !(StringUtils.isEmpty(indId))) {
						OrgSpiData orgSpiDataObj = orgSpiDataRepository.findSpiObjByIds(dId, cId, indId);
						spiDataMapObj.setSpiData(orgSpiDataObj);
					}

					if (!payload.getIsChecked()) {
						throw new OrgSpiDataException(customMessageSource.getMessage("org.spidata.error.created"));
					}

					spiDataMapObj.setIsChecked(payload.getIsChecked());
					spiDataMapObj.setCreatedAt(sdf.parse(formattedDte));
					spiDataMapObj.setUpdatedAt(sdf.parse(formattedDte));
					spiDataMapObj.setCreatedBy(OrganizationConstants.CREATED_BY);
					spiDataMapObj.setUpdatedBy(OrganizationConstants.UPDATED_BY);
					orgSpiDataMapRepository.saveAndFlush(spiDataMapObj);
				} catch (Exception e) {
					LOGGER.error(customMessageSource.getMessage("org.spidata.exception.created"), e);
					throw new OrgSpiDataException(customMessageSource.getMessage("org.spidata.error.created"));
				}
			}

		}

	}// end of method public void createSpiDataMapping(

	@Override
	public List<OrgSpiDataMapPayload> getSelectedSpiData(Long orgId) {
		List<OrgSpiDataMapPayload> payloadList = null;
		List<OrgSpiDataMapping> spiDataMapList = orgSpiDataMapRepository.getOrgSpiMapDataByOrgId(orgId);
		if (null != spiDataMapList) {
			payloadList = new ArrayList<OrgSpiDataMapPayload>();

			for (OrgSpiDataMapping spiMapData : spiDataMapList) {
				OrgSpiDataMapPayload payload = new OrgSpiDataMapPayload();
				payload.setId(spiMapData.getId());
				payload.setOrganizationId(spiMapData.getOrganizationId());
				if (null != spiMapData.getSpiData()) {
					payload.setDimensionId(spiMapData.getSpiData().getDimensionId());
					payload.setDimensionName(spiMapData.getSpiData().getDimensionName());
					payload.setComponentId(spiMapData.getSpiData().getComponentId());
					payload.setComponentName(spiMapData.getSpiData().getComponentName());
					payload.setIndicatorId(spiMapData.getSpiData().getIndicatorId());
					payload.setIndicatorName(spiMapData.getSpiData().getIndicatorName());
				}

				payloadList.add(payload);

			}
		}

		return payloadList;
	}// end of method

}
