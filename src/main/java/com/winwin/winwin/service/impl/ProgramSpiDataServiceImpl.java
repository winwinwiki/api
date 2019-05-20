package com.winwin.winwin.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
	public void createSpiDataMapping(List<ProgramSpiDataMapPayload> payloadList, Long orgId) throws SpiDataException {
		// TODO Auto-generated method stub
		UserPayload user = userService.getCurrentUserDetails();
		if (null != payloadList && null != user) {
			for (ProgramSpiDataMapPayload payload : payloadList) {
				try {
					ProgramSpiData spiDataMapObj = null;
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

					if (payload.getId() == null) {
						spiDataMapObj = new ProgramSpiData();
						spiDataMapObj.setProgramId(payload.getProgramId());
						Long dId = payload.getDimensionId();
						String cId = payload.getComponentId();
						String indId = payload.getIndicatorId();

						if (null != dId && !(StringUtils.isEmpty(cId)) && !(StringUtils.isEmpty(indId))) {
							SpiData orgSpiDataObj = spiDataRepository.findSpiObjByIds(dId, cId, indId);
							spiDataMapObj.setSpiData(orgSpiDataObj);
						}

						spiDataMapObj.setIsChecked(payload.getIsChecked());
						spiDataMapObj.setCreatedAt(sdf.parse(formattedDte));
						spiDataMapObj.setUpdatedAt(sdf.parse(formattedDte));
						spiDataMapObj.setCreatedBy(user.getEmail());
						spiDataMapObj.setUpdatedBy(user.getEmail());
						spiDataMapObj.setAdminUrl(payload.getAdminUrl());
						spiDataMapObj = programSpiDataMapRepository.saveAndFlush(spiDataMapObj);

						if (null != spiDataMapObj && null != payload.getOrganizationId()) {

							orgHistoryService.createOrganizationHistory(user, payload.getOrganizationId(), sdf,
									formattedDte, OrganizationConstants.CREATE, OrganizationConstants.SPI,
									spiDataMapObj.getId(), spiDataMapObj.getSpiData().getIndicatorName());
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
							throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.not_found"));
						}

						if (payload.getOrganizationId() == null || !(payload.getOrganizationId().equals(orgId))) {
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
							formattedDte = sdf.format(new Date(System.currentTimeMillis()));
							spiDataMapObj.setIsChecked(payload.getIsChecked());
							spiDataMapObj.setUpdatedAt(sdf.parse(formattedDte));
							spiDataMapObj.setUpdatedBy(user.getEmail());
							spiDataMapObj.setAdminUrl(payload.getAdminUrl());
							spiDataMapObj = programSpiDataMapRepository.saveAndFlush(spiDataMapObj);

							if (null != spiDataMapObj && null != payload.getOrganizationId()) {
								orgHistoryService.createOrganizationHistory(user, payload.getOrganizationId(), sdf,
										formattedDte, OrganizationConstants.UPDATE, OrganizationConstants.SPI,
										spiDataMapObj.getId(), spiDataMapObj.getSpiData().getIndicatorName());
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
	}

	@Override
	public List<ProgramSpiDataMapPayload> getSelectedSpiData(Long orgId) {
		// TODO Auto-generated method stub
		List<ProgramSpiDataMapPayload> payloadList = null;
		List<ProgramSpiData> spiDataMapList = programSpiDataMapRepository.getProgramSpiMapDataByOrgId(orgId);
		if (null != spiDataMapList) {
			payloadList = new ArrayList<>();

			for (ProgramSpiData spiMapData : spiDataMapList) {
				ProgramSpiDataMapPayload payload = new ProgramSpiDataMapPayload();
				payload.setId(spiMapData.getId());
				payload.setProgramId(spiMapData.getProgramId());
				payload.setIsChecked(spiMapData.getIsChecked());
				if (null != spiMapData.getSpiData()) {
					payload.setDimensionId(spiMapData.getSpiData().getDimensionId());
					payload.setDimensionName(spiMapData.getSpiData().getDimensionName());
					payload.setComponentId(spiMapData.getSpiData().getComponentId());
					payload.setComponentName(spiMapData.getSpiData().getComponentName());
					payload.setIndicatorId(spiMapData.getSpiData().getIndicatorId());
					payload.setIndicatorName(spiMapData.getSpiData().getIndicatorName());
				}
				payload.setAdminUrl(spiMapData.getAdminUrl());
				payloadList.add(payload);

			}
		}

		return payloadList;
	}

}
