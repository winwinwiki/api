package com.winwin.winwin.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrganizationSdgData;
import com.winwin.winwin.entity.SdgData;
import com.winwin.winwin.exception.SdgDataException;
import com.winwin.winwin.exception.SpiDataException;
import com.winwin.winwin.payload.OrganizationSdgDataMapPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrgSdgDataMapRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.service.OrgSdgDataService;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class OrgSdgDataServiceImpl implements OrgSdgDataService {
	@Autowired
	SdgDataRepository orgSdgDataRepository;

	@Autowired
	OrgSdgDataMapRepository orgSdgDataMapRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;
	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService orgHistoryService;
	private static final Logger LOGGER = LoggerFactory.getLogger(OrgSdgDataServiceImpl.class);

	@Override
	@Transactional
	public void createSdgDataMapping(List<OrganizationSdgDataMapPayload> payloadList, Long orgId)
			throws SdgDataException {
		UserPayload user = userService.getCurrentUserDetails();
		HashMap<String, SdgData> subGoalCodesMap = new HashMap<String, SdgData>();
		if (null != payloadList && null != user) {
			List<SdgData> sdgList = orgSdgDataRepository.findAllSdgData();
			if (null != sdgList) {
				for (SdgData sdgDataObj : sdgList) {
					subGoalCodesMap.put(sdgDataObj.getShortNameCode(), sdgDataObj);
				}
			}
			for (OrganizationSdgDataMapPayload payload : payloadList) {
				try {
					OrganizationSdgData sdgDataMapObj = null;
					Date date = CommonUtils.getFormattedDate();
					if (payload.getId() == null) {
						if (!StringUtils.isEmpty(payload.getSubGoalCode())) {
							SdgData sdgData = subGoalCodesMap.get(payload.getSubGoalCode());

							if (null != sdgData) {
								sdgDataMapObj = orgSdgDataMapRepository.findSdgSelectedTagsByOrgIdAndBySdgId(orgId,
										sdgData.getId());

								if (sdgDataMapObj == null) {
									sdgDataMapObj = new OrganizationSdgData();
									sdgDataMapObj.setOrganizationId(orgId);
									sdgDataMapObj.setSdgData(sdgData);
									sdgDataMapObj.setIsChecked(payload.getIsChecked());
									sdgDataMapObj.setCreatedAt(date);
									sdgDataMapObj.setUpdatedAt(date);
									sdgDataMapObj.setCreatedBy(user.getEmail());
									sdgDataMapObj.setUpdatedBy(user.getEmail());
									sdgDataMapObj.setAdminUrl(payload.getAdminUrl());
								}
							}
							sdgDataMapObj = orgSdgDataMapRepository.saveAndFlush(sdgDataMapObj);
						} else {
							LOGGER.error(customMessageSource.getMessage("org.sdgdata.exception.subgoalcode_null"));
							throw new SdgDataException(
									customMessageSource.getMessage("org.sdgdata.exception.subgoalcode_null"));
						}

						if (null != sdgDataMapObj && null != sdgDataMapObj.getOrganizationId()) {
							orgHistoryService.createOrganizationHistory(user, sdgDataMapObj.getOrganizationId(),
									OrganizationConstants.CREATE, OrganizationConstants.SDG, sdgDataMapObj.getId(),
									sdgDataMapObj.getSdgData().getShortName());
						}

					} else {
						Boolean isValidSdgData = true;
						Long goalCode = payload.getGoalCode();
						String subGoalCode = payload.getSubGoalCode();
						String goalName = payload.getGoalName();
						String subGoalName = payload.getSubGoalName();

						sdgDataMapObj = orgSdgDataMapRepository.findSdgSelectedTagsById(payload.getId());

						if (sdgDataMapObj == null) {
							LOGGER.error(customMessageSource.getMessage("org.sdgdata.error.not_found"));
							throw new SdgDataException(customMessageSource.getMessage("org.sdgdata.error.not_found"));
						}

						if (payload.getOrganizationId() == null || !(payload.getOrganizationId().equals(orgId))) {
							isValidSdgData = false;
						}

						if (null != goalCode && !(StringUtils.isEmpty(subGoalCode)) && !(StringUtils.isEmpty(goalName))
								&& !(StringUtils.isEmpty(subGoalName))) {

							if (null != sdgDataMapObj.getSdgData()) {

								if (goalCode != sdgDataMapObj.getSdgData().getGoalCode()) {
									isValidSdgData = false;
								} else if (!subGoalCode.equals(sdgDataMapObj.getSdgData().getShortNameCode())) {
									isValidSdgData = false;
								} else if (!goalName.equals(sdgDataMapObj.getSdgData().getGoalName())) {
									isValidSdgData = false;
								} else if (!subGoalName.equals(sdgDataMapObj.getSdgData().getShortName())) {
									isValidSdgData = false;
								}
							}
						}

						if (!isValidSdgData) {
							LOGGER.error(customMessageSource.getMessage("org.sdgdata.error.updated"));
							throw new SpiDataException(customMessageSource.getMessage("org.sdgdata.error.updated"));
						} else {
							sdgDataMapObj.setIsChecked(payload.getIsChecked());
							sdgDataMapObj.setUpdatedAt(date);
							sdgDataMapObj.setUpdatedBy(user.getEmail());
							sdgDataMapObj.setAdminUrl(payload.getAdminUrl());

							sdgDataMapObj = orgSdgDataMapRepository.saveAndFlush(sdgDataMapObj);

							if (null != sdgDataMapObj && null != sdgDataMapObj.getOrganizationId()) {
								orgHistoryService.createOrganizationHistory(user, sdgDataMapObj.getOrganizationId(),
										OrganizationConstants.UPDATE, OrganizationConstants.SDG, sdgDataMapObj.getId(),
										sdgDataMapObj.getSdgData().getShortName());
							}
						}
					}
				} catch (Exception e) {
					if (payload.getId() == null) {
						LOGGER.error(customMessageSource.getMessage("org.sdgdata.exception.created"), e);
						throw new SdgDataException(customMessageSource.getMessage("org.sdgdata.error.created"));
					} else {
						LOGGER.error(customMessageSource.getMessage("org.sdgdata.exception.updated"), e);
						throw new SdgDataException(customMessageSource.getMessage("org.sdgdata.error.updated"));
					}
				}
			} // end of loop for (OrgSdgDataMapPayload payload :
		}
	}// end of method createSdgDataMapping

	@Override
	public List<OrganizationSdgDataMapPayload> getSelectedSdgData(Long orgId) {
		List<OrganizationSdgDataMapPayload> payloadList = null;
		List<OrganizationSdgData> sdgDataMapList = orgSdgDataMapRepository.getOrgSdgMapDataByOrgId(orgId);

		if (null != sdgDataMapList) {
			payloadList = new ArrayList<OrganizationSdgDataMapPayload>();

			for (OrganizationSdgData sdgMapData : sdgDataMapList) {
				OrganizationSdgDataMapPayload payload = new OrganizationSdgDataMapPayload();
				BeanUtils.copyProperties(sdgMapData, payload);
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
