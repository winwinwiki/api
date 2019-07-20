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
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.ProgramSdgData;
import com.winwin.winwin.entity.SdgData;
import com.winwin.winwin.exception.SdgDataException;
import com.winwin.winwin.exception.SpiDataException;
import com.winwin.winwin.payload.ProgramSdgDataMapPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.ProgramSdgDataMapRepository;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.ProgramSdgDataService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class ProgramSdgDataServiceImpl implements ProgramSdgDataService {

	@Autowired
	SdgDataRepository sdgDataRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;
	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService orgHistoryService;

	@Autowired
	ProgramSdgDataMapRepository programSdgDataMapRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramSdgDataServiceImpl.class);

	@Override
	@Transactional
	public void createSdgDataMapping(List<ProgramSdgDataMapPayload> payloadList, Program program)
			throws SdgDataException {
		UserPayload user = userService.getCurrentUserDetails();
		Date date = CommonUtils.getFormattedDate();
		HashMap<String, SdgData> subGoalCodesMap = new HashMap<String, SdgData>();
		if (null != payloadList && null != user) {
			List<SdgData> sdgList = sdgDataRepository.findAllActiveSdgData();
			if (null != sdgList) {
				for (SdgData sdgDataObj : sdgList) {
					subGoalCodesMap.put(sdgDataObj.getShortNameCode(), sdgDataObj);
				}
			}
			for (ProgramSdgDataMapPayload payload : payloadList) {
				try {
					ProgramSdgData sdgDataMapObj = null;
					if (payload.getId() == null) {
						if (!StringUtils.isEmpty(payload.getSubGoalCode())) {
							SdgData sdgData = subGoalCodesMap.get(payload.getSubGoalCode());

							if (null != sdgData) {
								sdgDataMapObj = programSdgDataMapRepository
										.findSdgSelectedTagsByProgramIdAndBySdgId(program.getId(), sdgData.getId());

								if (sdgDataMapObj == null) {
									sdgDataMapObj = new ProgramSdgData();
									sdgDataMapObj.setProgram(program);
									sdgDataMapObj.setSdgData(sdgData);
									sdgDataMapObj.setIsChecked(payload.getIsChecked());
									sdgDataMapObj.setCreatedAt(date);
									sdgDataMapObj.setUpdatedAt(date);
									sdgDataMapObj.setCreatedBy(user.getEmail());
									sdgDataMapObj.setUpdatedBy(user.getEmail());
								}
							}
							sdgDataMapObj = programSdgDataMapRepository.saveAndFlush(sdgDataMapObj);
						} else {
							LOGGER.error(customMessageSource.getMessage("org.sdgdata.exception.subgoalcode_null"));
							throw new SdgDataException(
									customMessageSource.getMessage("org.sdgdata.exception.subgoalcode_null"));
						}

						if (null != sdgDataMapObj && null != payload.getOrganizationId()) {
							orgHistoryService.createOrganizationHistory(user, payload.getOrganizationId(),
									payload.getProgramId(), OrganizationConstants.CREATE, OrganizationConstants.SDG,
									sdgDataMapObj.getId(), sdgDataMapObj.getSdgData().getShortName(),
									sdgDataMapObj.getSdgData().getShortNameCode());
						}
					} else {
						Boolean isValidSdgData = true;
						Long goalCode = payload.getGoalCode();
						String subGoalCode = payload.getSubGoalCode();
						String goalName = payload.getGoalName();
						String subGoalName = payload.getSubGoalName();

						sdgDataMapObj = programSdgDataMapRepository.findSdgSelectedTagsById(payload.getId());

						if (sdgDataMapObj == null) {
							LOGGER.error(customMessageSource.getMessage("org.sdgdata.error.not_found"));
							throw new SdgDataException(customMessageSource.getMessage("org.sdgdata.error.not_found"));
						}
						if (payload.getProgramId() == null) {
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
							sdgDataMapObj.setProgram(program);

							sdgDataMapObj = programSdgDataMapRepository.saveAndFlush(sdgDataMapObj);

							if (null != sdgDataMapObj && null != payload.getOrganizationId()) {
								orgHistoryService.createOrganizationHistory(user, payload.getOrganizationId(),
										payload.getProgramId(), OrganizationConstants.UPDATE, OrganizationConstants.SDG,
										sdgDataMapObj.getId(), sdgDataMapObj.getSdgData().getShortName(),
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
	}

	@Override
	public List<ProgramSdgDataMapPayload> getSelectedSdgData(Long programId) {
		// TODO Auto-generated method stub
		List<ProgramSdgDataMapPayload> payloadList = null;
		List<ProgramSdgData> sdgDataMapList = programSdgDataMapRepository.getProgramSdgMapDataByOrgId(programId);
		if (null != sdgDataMapList) {
			payloadList = new ArrayList<>();
			for (ProgramSdgData sdgMapData : sdgDataMapList) {
				ProgramSdgDataMapPayload payload = new ProgramSdgDataMapPayload();
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
	}
}
