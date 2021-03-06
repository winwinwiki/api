package com.winwin.winwin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.SdgData;
import com.winwin.winwin.payload.SdgGoalPayload;
import com.winwin.winwin.payload.SdgSubGoalPayload;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.service.SdgDataService;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class SdgDataServiceImpl implements SdgDataService {

	@Autowired
	private SdgDataRepository orgSdgDataRepository;

	/**
	 * returns SdgData List
	 * 
	 * @return
	 */
	@Override
	@CachePut(value = "sdg_data_list")
	public List<SdgGoalPayload> getSdgDataForResponse() {
		List<SdgGoalPayload> payloadList = new ArrayList<>();
		List<SdgData> sdgList = orgSdgDataRepository.findAllActiveSdgData();
		if (null != sdgList) {
			HashMap<Long, List<SdgData>> sdgDataMap = new HashMap<>();
			setSdgDataMap(sdgList, sdgDataMap);

			if ((!sdgDataMap.isEmpty())) {
				for (List<SdgData> sdgDataList : sdgDataMap.values()) {
					SdgGoalPayload sdgGoalPayload = new SdgGoalPayload();
					List<SdgSubGoalPayload> subGoalsList = new ArrayList<>();
					sdgGoalPayload.setGoalCode(sdgDataList.get(0).getGoalCode());
					sdgGoalPayload.setGoalName(sdgDataList.get(0).getGoalName());
					for (SdgData sdgdata : sdgDataList) {
						SdgSubGoalPayload subGoalPayload = new SdgSubGoalPayload();
						subGoalPayload.setSubGoalCode(sdgdata.getShortNameCode());
						subGoalPayload.setSubGoalName(sdgdata.getShortName());
						subGoalsList.add(subGoalPayload);
					}
					sdgGoalPayload.setSubGoals(subGoalsList);
					payloadList.add(sdgGoalPayload);
				}
			}
		}
		return payloadList;
	}// end of method getSdgDataForResponse

	/**
	 * @param sdgList
	 * @param sdgDataMap
	 */
	private void setSdgDataMap(List<SdgData> sdgList, HashMap<Long, List<SdgData>> sdgDataMap) {
		for (SdgData sdgDataObj : sdgList) {
			if (!sdgDataMap.containsKey(sdgDataObj.getGoalCode())) {
				List<SdgData> subGoals = new ArrayList<>();
				subGoals.add(sdgDataObj);
				sdgDataMap.put(sdgDataObj.getGoalCode(), subGoals);
			} else {
				sdgDataMap.get(sdgDataObj.getGoalCode()).add(sdgDataObj);
			}
		}
	}// end of method setSdgDataMap

}
