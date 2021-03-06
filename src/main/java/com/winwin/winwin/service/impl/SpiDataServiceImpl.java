package com.winwin.winwin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.winwin.winwin.entity.SpiData;
import com.winwin.winwin.payload.SpiDataComponentsPayload;
import com.winwin.winwin.payload.SpiDataDimensionsPayload;
import com.winwin.winwin.payload.SpiDataIndicatorsPayload;
import com.winwin.winwin.repository.SpiDataRepository;
import com.winwin.winwin.service.SpiDataService;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class SpiDataServiceImpl implements SpiDataService {

	@Autowired
	private SpiDataRepository spiDataRepository;

	/**
	 * returns SpiData List
	 * 
	 * @return
	 */
	@Override
	@CachePut(value = "spi_data_list")
	public List<SpiDataDimensionsPayload> getSpiDataForResponse() {
		List<SpiDataDimensionsPayload> dimensionPayloadList = new ArrayList<>();
		List<SpiData> spiList = spiDataRepository.findAllActiveSpiData();
		if (null != spiList) {
			HashMap<Long, List<SpiData>> spiDimensionsMap = new HashMap<>();
			setSpiDimensionsMap(spiList, spiDimensionsMap);

			if ((!spiDimensionsMap.isEmpty())) {
				for (List<SpiData> spiComponentsList : spiDimensionsMap.values()) {
					SpiDataDimensionsPayload dimensionPayloadObj = new SpiDataDimensionsPayload();
					dimensionPayloadObj.setDimensionId(spiComponentsList.get(0).getDimensionId());
					dimensionPayloadObj.setDimensionName(spiComponentsList.get(0).getDimensionName());

					HashMap<String, List<SpiData>> spiComponentsMap = new HashMap<>();
					setSpiComponentsMap(spiComponentsList, spiComponentsMap);

					HashMap<String, List<SpiDataIndicatorsPayload>> spiIndicatorsMap = new HashMap<>();
					setSpiIndicatorsMap(spiComponentsList, spiIndicatorsMap);

					List<SpiDataComponentsPayload> componentPayloadList = new ArrayList<>();
					for (List<SpiData> splittedComponentsList : spiComponentsMap.values()) {
						SpiDataComponentsPayload componentPayloadObj = new SpiDataComponentsPayload();
						componentPayloadObj.setComponentId(splittedComponentsList.get(0).getComponentId());
						componentPayloadObj.setComponentName(splittedComponentsList.get(0).getComponentName());
						componentPayloadObj
								.setIndicators(spiIndicatorsMap.get(splittedComponentsList.get(0).getComponentId()));
						componentPayloadList.add(componentPayloadObj);
					}
					dimensionPayloadObj.setComponents(componentPayloadList);
					dimensionPayloadList.add(dimensionPayloadObj);
				} // end of loop
			} // end of if
		} // end of if
		return dimensionPayloadList;
	}

	private void setSpiComponentsMap(List<SpiData> spiList, HashMap<String, List<SpiData>> spiComponentsMap) {
		for (SpiData spiDataObj : spiList) {
			if (!spiComponentsMap.containsKey(spiDataObj.getComponentId())) {
				List<SpiData> components = new ArrayList<>();
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
	private void setSpiIndicatorsMap(List<SpiData> spiList,
			HashMap<String, List<SpiDataIndicatorsPayload>> spiIndicatorsMap) {
		for (SpiData spiDataObj : spiList) {
			SpiDataIndicatorsPayload payload = new SpiDataIndicatorsPayload();
			if (!StringUtils.isEmpty(spiDataObj.getIndicatorId())) {
				payload.setIndicatorId(spiDataObj.getIndicatorId());
				payload.setIndicatorName(spiDataObj.getIndicatorName());
			}

			if (!spiIndicatorsMap.containsKey(spiDataObj.getComponentId())) {
				List<SpiDataIndicatorsPayload> indicators = new ArrayList<>();
				indicators.add(payload);

				spiIndicatorsMap.put(spiDataObj.getComponentId(), indicators);
			} else {
				spiIndicatorsMap.get(spiDataObj.getComponentId()).add(payload);
			}
		}
	}

	private void setSpiDimensionsMap(List<SpiData> spiList, HashMap<Long, List<SpiData>> spiDimensionsMap) {
		for (SpiData spiDataObj : spiList) {
			if (!spiDimensionsMap.containsKey(spiDataObj.getDimensionId())) {
				List<SpiData> components = new ArrayList<>();
				components.add(spiDataObj);
				spiDimensionsMap.put(spiDataObj.getDimensionId(), components);
			} else {
				spiDimensionsMap.get(spiDataObj.getDimensionId()).add(spiDataObj);
			}
		}
	}
}
