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
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationSdgData;
import com.winwin.winwin.entity.SdgData;
import com.winwin.winwin.exception.SdgDataException;
import com.winwin.winwin.exception.SpiDataException;
import com.winwin.winwin.payload.OrganizationSdgDataMapPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrgSdgDataMapRepository;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.service.OrgSdgDataService;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class OrgSdgDataServiceImpl implements OrgSdgDataService {
	@Autowired
	private SdgDataRepository orgSdgDataRepository;
	@Autowired
	private OrgSdgDataMapRepository orgSdgDataMapRepository;
	@Autowired
	protected CustomMessageSource customMessageSource;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationHistoryService orgHistoryService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgSdgDataServiceImpl.class);

	/**
	 * create or update OrganizationSdgData
	 * 
	 * @param payloadList
	 * @param organization
	 */
	@Override
	@Transactional
	public void createSdgDataMapping(List<OrganizationSdgDataMapPayload> payloadList, Organization organization)
			throws SdgDataException {
		UserPayload user = userService.getCurrentUserDetails();
		HashMap<String, SdgData> subGoalCodesMap = new HashMap<String, SdgData>();
		if (null != payloadList && null != user) {
			List<SdgData> sdgList = orgSdgDataRepository.findAllActiveSdgData();
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
								sdgDataMapObj = orgSdgDataMapRepository
										.findSdgSelectedTagsByOrgIdAndBySdgId(organization.getId(), sdgData.getId());

								if (sdgDataMapObj == null) {
									sdgDataMapObj = new OrganizationSdgData();
									sdgDataMapObj.setOrganization(organization);
									sdgDataMapObj.setSdgData(sdgData);
									sdgDataMapObj.setIsChecked(payload.getIsChecked());
									sdgDataMapObj.setCreatedAt(date);
									sdgDataMapObj.setUpdatedAt(date);
									sdgDataMapObj.setCreatedBy(user.getUserDisplayName());
									sdgDataMapObj.setUpdatedBy(user.getUserDisplayName());
									sdgDataMapObj.setCreatedByEmail(user.getEmail());
									sdgDataMapObj.setUpdatedByEmail(user.getEmail());
								}
							}
							sdgDataMapObj = orgSdgDataMapRepository.saveAndFlush(sdgDataMapObj);
						} else {
							LOGGER.error(customMessageSource.getMessage("org.sdgdata.exception.subgoalcode_null"));
							throw new SdgDataException(
									customMessageSource.getMessage("org.sdgdata.exception.subgoalcode_null"));
						}

						if (null != sdgDataMapObj && null != sdgDataMapObj.getOrganization()) {
							orgHistoryService.createOrganizationHistory(user, sdgDataMapObj.getOrganization().getId(),
									OrganizationConstants.CREATE, OrganizationConstants.SDG, sdgDataMapObj.getId(),
									sdgDataMapObj.getSdgData().getShortName(),
									sdgDataMapObj.getSdgData().getShortNameCode());
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

						if (payload.getOrganizationId() == null
								|| !(payload.getOrganizationId().equals(organization.getId()))) {
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
							sdgDataMapObj.setUpdatedBy(user.getUserDisplayName());
							sdgDataMapObj.setUpdatedByEmail(user.getEmail());
							sdgDataMapObj = orgSdgDataMapRepository.saveAndFlush(sdgDataMapObj);

							if (null != sdgDataMapObj && null != sdgDataMapObj.getOrganization()) {
								orgHistoryService.createOrganizationHistory(user,
										sdgDataMapObj.getOrganization().getId(), OrganizationConstants.UPDATE,
										OrganizationConstants.SDG, sdgDataMapObj.getId(),
										sdgDataMapObj.getSdgData().getShortName(),
										sdgDataMapObj.getSdgData().getShortNameCode());
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

	/**
	 * returns OrganizationSdgData List by OrgId
	 * 
	 * @param orgId
	 */
	@Override
	public List<OrganizationSdgDataMapPayload> getSelectedSdgData(Long orgId) {
		List<OrganizationSdgDataMapPayload> payloadList = new ArrayList<OrganizationSdgDataMapPayload>();
		List<OrganizationSdgData> sdgDataMapList = orgSdgDataMapRepository.getOrgSdgMapDataByOrgId(orgId);

		if (null != sdgDataMapList) {
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
