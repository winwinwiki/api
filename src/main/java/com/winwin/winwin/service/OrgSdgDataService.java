package com.winwin.winwin.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrgSdgData;
import com.winwin.winwin.entity.OrgSdgDataMapping;
import com.winwin.winwin.exception.OrgSdgDataException;
import com.winwin.winwin.exception.OrgSpiDataException;
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
		List<OrgSdgGoalPayload> payloadList = null;
		List<OrgSdgData> sdgList = orgSdgDataRepository.findAll();
		if (null != sdgList) {
			HashMap<Long, List<OrgSdgData>> sdgDataMap = new HashMap<Long, List<OrgSdgData>>();
			payloadList = new ArrayList<OrgSdgGoalPayload>();
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

		}

		return payloadList;
	}// end of method getSdgDataForResponse

	/**
	 * @param sdgList
	 * @param sdgDataMap
	 */
	private void setSdgDataMap(List<OrgSdgData> sdgList, HashMap<Long, List<OrgSdgData>> sdgDataMap) {
		for (OrgSdgData sdgDataObj : sdgList) {
			if (!sdgDataMap.containsKey(sdgDataObj.getGoalCode())) {
				List<OrgSdgData> subGoals = new ArrayList<OrgSdgData>();
				subGoals.add(sdgDataObj);

				sdgDataMap.put(sdgDataObj.getGoalCode(), subGoals);
			} else {
				sdgDataMap.get(sdgDataObj.getGoalCode()).add(sdgDataObj);
			}
		}
	}// end of method setSdgDataMap

	@Override
	public void createSdgDataMapping(List<OrgSdgDataMapPayload> payloadList, Long orgId) throws OrgSdgDataException {
		HashMap<String, OrgSdgData> subGoalCodesMap = new HashMap<String, OrgSdgData>();
		List<OrgSdgData> sdgList = orgSdgDataRepository.findAll();
		if (null != sdgList) {
			for (OrgSdgData sdgDataObj : sdgList) {
				subGoalCodesMap.put(sdgDataObj.getShortNameCode(), sdgDataObj);
			}
		}

		for (OrgSdgDataMapPayload payload : payloadList) {
			try {
				OrgSdgDataMapping sdgDataMapObj = null;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				if (payload.getId() == null) {
					sdgDataMapObj = new OrgSdgDataMapping();
					sdgDataMapObj.setOrganizationId(orgId);

					if (!StringUtils.isEmpty(payload.getSubGoalCode())) {
						OrgSdgData orgSdgData = subGoalCodesMap.get(payload.getSubGoalCode());
						sdgDataMapObj.setSdgData(orgSdgData);
					} else {
						LOGGER.error(customMessageSource.getMessage("org.sdgdata.exception.subgoalcode_null"));
						throw new OrgSdgDataException(
								customMessageSource.getMessage("org.sdgdata.exception.subgoalcode_null"));
					}
					sdgDataMapObj.setIsChecked(payload.getIsChecked());
					sdgDataMapObj.setCreatedAt(sdf.parse(formattedDte));
					sdgDataMapObj.setUpdatedAt(sdf.parse(formattedDte));
					sdgDataMapObj.setCreatedBy(OrganizationConstants.CREATED_BY);
					sdgDataMapObj.setUpdatedBy(OrganizationConstants.UPDATED_BY);
					orgSdgDataMapRepository.saveAndFlush(sdgDataMapObj);
				} else {
					Boolean isValidSdgData = true;
					Long goalCode = payload.getGoalCode();
					String subGoalCode = payload.getSubGoalCode();
					String goalName = payload.getGoalName();
					String subGoalName = payload.getSubGoalName();

					sdgDataMapObj = orgSdgDataMapRepository.findSdgSelectedTagsById(payload.getId());

					if (sdgDataMapObj == null) {
						LOGGER.error(customMessageSource.getMessage("org.sdgdata.error.not_found"));
						throw new OrgSdgDataException(customMessageSource.getMessage("org.sdgdata.error.not_found"));
					}

					if (payload.getOrganizationId() == null || !(payload.getOrganizationId() == orgId)) {
						isValidSdgData = false;
					}

					if (null != goalCode && !(StringUtils.isEmpty(subGoalCode)) && !(StringUtils.isEmpty(goalName))
							&& !(StringUtils.isEmpty(subGoalName))) {

						if (null != sdgDataMapObj.getSdgData()) {

							if (goalCode != sdgDataMapObj.getSdgData().getGoalCode()) {
								isValidSdgData = false;
							}
							if (!subGoalCode.equals(sdgDataMapObj.getSdgData().getShortNameCode())) {
								isValidSdgData = false;
							}
							if (!goalName.equals(sdgDataMapObj.getSdgData().getGoalName())) {
								isValidSdgData = false;
							}
							if (!subGoalName.equals(sdgDataMapObj.getSdgData().getShortName())) {
								isValidSdgData = false;
							}

						}
					}

					if (!isValidSdgData) {
						LOGGER.error(customMessageSource.getMessage("org.sdgdata.error.updated"));
						throw new OrgSpiDataException(customMessageSource.getMessage("org.sdgdata.error.updated"));
					} else {
						formattedDte = sdf.format(new Date(System.currentTimeMillis()));
						sdgDataMapObj.setIsChecked(payload.getIsChecked());
						sdgDataMapObj.setUpdatedAt(sdf.parse(formattedDte));
						sdgDataMapObj.setUpdatedBy(OrganizationConstants.UPDATED_BY);
					}

				}

			} catch (Exception e) {
				if (payload.getId() == null) {
					LOGGER.error(customMessageSource.getMessage("org.sdgdata.exception.created"), e);
					throw new OrgSdgDataException(customMessageSource.getMessage("org.sdgdata.error.created"));
				} else {
					LOGGER.error(customMessageSource.getMessage("org.sdgdata.exception.updated"), e);
					throw new OrgSdgDataException(customMessageSource.getMessage("org.sdgdata.error.updated"));
				}

			}
		}

	}// end of method createSdgDataMapping

	@Override
	public List<OrgSdgDataMapPayload> getSelectedSdgData(Long orgId) {

		List<OrgSdgDataMapPayload> payloadList = null;
		List<OrgSdgDataMapping> sdgDataMapList = orgSdgDataMapRepository.getOrgSdgMapDataByOrgId(orgId);
		if (null != sdgDataMapList) {
			payloadList = new ArrayList<OrgSdgDataMapPayload>();

			for (OrgSdgDataMapping sdgMapData : sdgDataMapList) {
				OrgSdgDataMapPayload payload = new OrgSdgDataMapPayload();
				payload.setId(sdgMapData.getId());
				payload.setOrganizationId(sdgMapData.getOrganizationId());
				payload.setIsChecked(sdgMapData.getIsChecked());
				if (null != sdgMapData.getSdgData()) {
					payload.setGoalCode(sdgMapData.getSdgData().getGoalCode());
					payload.setGoalName(sdgMapData.getSdgData().getGoalName());
					payload.setSubGoalCode(sdgMapData.getSdgData().getShortNameCode());
					payload.setSubGoalName(sdgMapData.getSdgData().getShortName());
				}

				payloadList.add(payload);

			}
		}

		return payloadList;
	}// end of method

}
