package com.winwin.winwin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.entity.OrgSdgData;
import com.winwin.winwin.entity.OrgSdgDataMapping;
import com.winwin.winwin.payload.OrgSdgDataMapPayload;
import com.winwin.winwin.payload.OrgSdgGoalPayload;
import com.winwin.winwin.payload.OrgSdgSubGoalPayload;
import com.winwin.winwin.repository.OrgSdgDataMapRepository;
import com.winwin.winwin.repository.OrgSdgDataRepository;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class OrgSdgDataService implements IOrgSdgDataService {
	@Autowired
	OrgSdgDataRepository orgSdgDataRepository;

	@Autowired
	OrgSdgDataMapRepository orgSdgDataMapRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgSdgDataService.class);

	@Override
	public List<OrgSdgGoalPayload> getSdgDataForResponse() {
		List<OrgSdgData> sdgList = orgSdgDataRepository.findAll();
		HashMap<Long, List<OrgSdgData>> sdgDataMap = new HashMap<Long, List<OrgSdgData>>();
		List<OrgSdgGoalPayload> payloadList = new ArrayList<OrgSdgGoalPayload>();
		setSdgDataMap(sdgList, sdgDataMap);

		if ((!sdgDataMap.isEmpty())) {
			for (List<OrgSdgData> sdgDataList : sdgDataMap.values()) {
				OrgSdgGoalPayload sdgGoalPayload = new OrgSdgGoalPayload();
				List<OrgSdgSubGoalPayload> subGoalsList = new ArrayList<OrgSdgSubGoalPayload>();
				sdgGoalPayload.setGoalCode(sdgDataList.get(0).getGoalCode());
				sdgGoalPayload.setGoalName(sdgDataList.get(0).getGoalName());
				for (OrgSdgData sdgdata : sdgDataList) {
					OrgSdgSubGoalPayload subGoalPayload = new OrgSdgSubGoalPayload();
					subGoalPayload.setSubGoalCode(sdgdata.getShortNameCode());
					subGoalPayload.setSubGoalName(sdgdata.getShortName());
					subGoalsList.add(subGoalPayload);
				}

				sdgGoalPayload.setSubGoals(subGoalsList);
				payloadList.add(sdgGoalPayload);
			}
		}
		return payloadList;
	}

	/**
	 * @param sdgList
	 * @param sdgDataMap
	 */
	private void setSdgDataMap(List<OrgSdgData> sdgList, HashMap<Long, List<OrgSdgData>> sdgDataMap) {
		for (OrgSdgData sdgDataObj : sdgList) {
			if (!sdgDataMap.containsKey(sdgDataObj.getGoalCode())) {
				List<OrgSdgData> SubGoals = new ArrayList<OrgSdgData>();
				SubGoals.add(sdgDataObj);

				sdgDataMap.put(sdgDataObj.getGoalCode(), SubGoals);
			} else {
				sdgDataMap.get(sdgDataObj.getGoalCode()).add(sdgDataObj);
			}
		}
	}

	@Override
	public void createSdgDataMapping(List<OrgSdgDataMapPayload> payloadList, Long orgId) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<OrgSdgDataMapping> getSelectedSdgData() {
		// TODO Auto-generated method stub
		return null;
	}

}
