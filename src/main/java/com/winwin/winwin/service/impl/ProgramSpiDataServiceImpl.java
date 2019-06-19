package com.winwin.winwin.service.impl;

import java.util.ArrayList;
import java.util.Date;
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
import com.winwin.winwin.entity.ProgramSpiData;
import com.winwin.winwin.entity.SpiData;
import com.winwin.winwin.exception.SpiDataException;
import com.winwin.winwin.payload.ProgramSpiDataMapPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrgSpiDataMapRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.ProgramSpiDataMapRepository;
import com.winwin.winwin.repository.SpiDataRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.ProgramSpiDataService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class ProgramSpiDataServiceImpl implements ProgramSpiDataService {

	@Autowired
	SpiDataRepository spiDataRepository;

	@Autowired
	OrgSpiDataMapRepository orgSpiDataMapRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService orgHistoryService;

	@Autowired
	ProgramSpiDataMapRepository programSpiDataMapRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramSpiDataServiceImpl.class);

	@Override
	@Transactional
	public void createSpiDataMapping(List<ProgramSpiDataMapPayload> payloadList, Long progId) throws SpiDataException {
		try {
			UserPayload user = userService.getCurrentUserDetails();
			Date date = CommonUtils.getFormattedDate();
			if (null != payloadList && null != user) {
				for (ProgramSpiDataMapPayload payload : payloadList) {
					try {
						ProgramSpiData spiDataMapObj = null;
						if (payload.getId() == null) {
							spiDataMapObj = new ProgramSpiData();
							BeanUtils.copyProperties(payload, spiDataMapObj);

							Long dId = payload.getDimensionId();
							String cId = payload.getComponentId();
							String indId = payload.getIndicatorId();

							if (null != dId && !(StringUtils.isEmpty(cId)) && !(StringUtils.isEmpty(indId))) {
								SpiData spiDataObj = spiDataRepository.findSpiObjByIds(dId, cId, indId);

								if (null != spiDataObj) {
									spiDataMapObj = programSpiDataMapRepository
											.findSpiSelectedTagsByProgramIdAndBySpiId(progId, spiDataObj.getId());

									if (spiDataMapObj == null) {
										spiDataMapObj = new ProgramSpiData();
										spiDataMapObj.setProgramId(progId);
										spiDataMapObj.setSpiData(spiDataObj);
										spiDataMapObj.setIsChecked(payload.getIsChecked());
										spiDataMapObj.setCreatedAt(date);
										spiDataMapObj.setUpdatedAt(date);
										spiDataMapObj.setCreatedBy(user.getEmail());
										spiDataMapObj.setUpdatedBy(user.getEmail());
									}
								}
								spiDataMapObj = programSpiDataMapRepository.saveAndFlush(spiDataMapObj);
							}

							if (null != spiDataMapObj && null != payload.getOrganizationId()) {
								orgHistoryService.createOrganizationHistory(user, payload.getOrganizationId(),
										payload.getProgramId(), OrganizationConstants.CREATE, OrganizationConstants.SPI,
										spiDataMapObj.getId(), spiDataMapObj.getSpiData().getIndicatorName(),
										spiDataMapObj.getSpiData().getIndicatorId());
							}
						} else {
							Boolean isValidSpiData = true;
							Long dId = payload.getDimensionId();
							String cId = payload.getComponentId();
							String indId = payload.getIndicatorId();
							String dName = payload.getDimensionName();
							String cName = payload.getComponentName();
							String indName = payload.getIndicatorName();

							spiDataMapObj = programSpiDataMapRepository.findSpiSelectedTagsById(payload.getId());

							if (spiDataMapObj == null) {
								LOGGER.error(customMessageSource.getMessage("org.spidata.error.not_found"));
								throw new SpiDataException(
										customMessageSource.getMessage("org.spidata.error.not_found"));
							}

							if (payload.getProgramId() == null) {
								isValidSpiData = false;
							}

							if (null != dId && !(StringUtils.isEmpty(cId)) && !(StringUtils.isEmpty(indId))
									&& !(StringUtils.isEmpty(dName)) && !(StringUtils.isEmpty(dName))
									&& !(StringUtils.isEmpty(dName))) {
								if (null != spiDataMapObj.getSpiData()) {
									if (dId != spiDataMapObj.getSpiData().getDimensionId()) {
										isValidSpiData = false;
									} else if (!cId.equals(spiDataMapObj.getSpiData().getComponentId())) {
										isValidSpiData = false;
									} else if (!indId.equals(spiDataMapObj.getSpiData().getIndicatorId())) {
										isValidSpiData = false;
									} else if (!dName.equals(spiDataMapObj.getSpiData().getDimensionName())) {
										isValidSpiData = false;
									} else if (!cName.equals(spiDataMapObj.getSpiData().getComponentName())) {
										isValidSpiData = false;
									} else if (!indName.equals(spiDataMapObj.getSpiData().getIndicatorName())) {
										isValidSpiData = false;
									}
								}
							}

							if (!isValidSpiData) {
								LOGGER.error(customMessageSource.getMessage("org.spidata.error.updated"));
								throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.updated"));
							} else {
								BeanUtils.copyProperties(payload, spiDataMapObj);
								spiDataMapObj.setUpdatedAt(date);
								spiDataMapObj.setUpdatedBy(user.getEmail());
								spiDataMapObj = programSpiDataMapRepository.saveAndFlush(spiDataMapObj);

								if (null != spiDataMapObj && null != payload.getOrganizationId()) {
									orgHistoryService.createOrganizationHistory(user, payload.getOrganizationId(),
											payload.getProgramId(), OrganizationConstants.UPDATE,
											OrganizationConstants.SPI, spiDataMapObj.getId(),
											spiDataMapObj.getSpiData().getIndicatorName(),
											spiDataMapObj.getSpiData().getIndicatorId());
								}
							}
						}
					} catch (Exception e) {
						if (payload.getId() == null) {
							LOGGER.error(customMessageSource.getMessage("org.spidata.exception.created"), e);
							throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.created"));
						} else {
							LOGGER.error(customMessageSource.getMessage("org.spidata.exception.updated"), e);
							throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.updated"));
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.spidata.exception.created"), e);
			throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.created"));
		}
	}

	@Override
	public List<ProgramSpiDataMapPayload> getSelectedSpiData(Long orgId) {
		List<ProgramSpiDataMapPayload> payloadList = null;
		List<ProgramSpiData> spiDataMapList = programSpiDataMapRepository.getProgramSpiMapDataByOrgId(orgId);
		if (null != spiDataMapList) {
			payloadList = new ArrayList<>();

			for (ProgramSpiData spiMapData : spiDataMapList) {
				ProgramSpiDataMapPayload payload = new ProgramSpiDataMapPayload();
				BeanUtils.copyProperties(spiMapData, payload);
				if (null != spiMapData.getSpiData()) {
					BeanUtils.copyProperties(spiMapData.getSpiData(), payload);
				}
				payload.setId(spiMapData.getId());
				payloadList.add(payload);
			}
		}
		return payloadList;
	}
}
