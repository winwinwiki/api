/**
 * 
 */
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
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationSpiData;
import com.winwin.winwin.entity.SpiData;
import com.winwin.winwin.exception.SpiDataException;
import com.winwin.winwin.payload.OrganizationSpiDataMapPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrgSpiDataMapRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.SpiDataRepository;
import com.winwin.winwin.service.OrgSpiDataService;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class OrgSpiDataServiceImpl implements OrgSpiDataService {

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

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgSpiDataServiceImpl.class);

	@Override
	@Transactional
	public void createSpiDataMapping(List<OrganizationSpiDataMapPayload> payloadList, Organization organization)
			throws SpiDataException {
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (null != payloadList && null != user) {
				Date date = CommonUtils.getFormattedDate();
				for (OrganizationSpiDataMapPayload payload : payloadList) {
					try {
						OrganizationSpiData spiDataMapObj = null;
						if (payload.getId() == null) {
							Long dId = payload.getDimensionId();
							String cId = payload.getComponentId();
							String indId = payload.getIndicatorId();

							if (null != dId && !(StringUtils.isEmpty(cId)) && !(StringUtils.isEmpty(indId))) {
								SpiData orgSpiDataObj = spiDataRepository.findSpiObjByIds(dId, cId, indId);

								if (null != orgSpiDataObj) {
									spiDataMapObj = orgSpiDataMapRepository.findSpiSelectedTagsByOrgIdAndBySpiId(
											organization.getId(), orgSpiDataObj.getId());

									if (spiDataMapObj == null) {
										spiDataMapObj = new OrganizationSpiData();
										spiDataMapObj.setOrganization(organization);
										spiDataMapObj.setSpiData(orgSpiDataObj);
										spiDataMapObj.setIsChecked(payload.getIsChecked());
										spiDataMapObj.setCreatedAt(date);
										spiDataMapObj.setUpdatedAt(date);
										spiDataMapObj.setCreatedBy(user.getEmail());
										spiDataMapObj.setUpdatedBy(user.getEmail());
									}
								}
								spiDataMapObj = orgSpiDataMapRepository.saveAndFlush(spiDataMapObj);
							}

							if (null != spiDataMapObj && null != spiDataMapObj.getOrganization()) {
								orgHistoryService.createOrganizationHistory(user,
										spiDataMapObj.getOrganization().getId(), OrganizationConstants.CREATE,
										OrganizationConstants.SPI, spiDataMapObj.getId(),
										spiDataMapObj.getSpiData().getIndicatorName(), "");
							}
						} else {
							Boolean isValidSpiData = true;
							Long dId = payload.getDimensionId();
							String cId = payload.getComponentId();
							String indId = payload.getIndicatorId();
							String dName = payload.getDimensionName();
							String cName = payload.getComponentName();
							String indName = payload.getIndicatorName();

							spiDataMapObj = orgSpiDataMapRepository.findSpiSelectedTagsById(payload.getId());

							if (spiDataMapObj == null) {
								LOGGER.error(customMessageSource.getMessage("org.spidata.error.not_found"));
								throw new SpiDataException(
										customMessageSource.getMessage("org.spidata.error.not_found"));
							}

							if (payload.getOrganizationId() == null
									|| !(payload.getOrganizationId().equals(organization.getId()))) {
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
								spiDataMapObj.setIsChecked(payload.getIsChecked());
								spiDataMapObj.setUpdatedAt(date);
								spiDataMapObj.setUpdatedBy(user.getEmail());
								spiDataMapObj = orgSpiDataMapRepository.saveAndFlush(spiDataMapObj);

								if (null != spiDataMapObj && null != spiDataMapObj.getOrganization()) {
									orgHistoryService.createOrganizationHistory(user,
											spiDataMapObj.getOrganization().getId(), OrganizationConstants.UPDATE,
											OrganizationConstants.SPI, spiDataMapObj.getId(),
											spiDataMapObj.getSpiData().getIndicatorName(), "");
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

	}// end of method public void createSpiDataMapping(

	@Override
	public List<OrganizationSpiDataMapPayload> getSelectedSpiData(Long orgId) {
		List<OrganizationSpiDataMapPayload> payloadList = null;
		List<OrganizationSpiData> spiDataMapList = orgSpiDataMapRepository.getOrgSpiMapDataByOrgId(orgId);
		if (null != spiDataMapList) {
			payloadList = new ArrayList<OrganizationSpiDataMapPayload>();
			for (OrganizationSpiData spiMapData : spiDataMapList) {
				OrganizationSpiDataMapPayload payload = new OrganizationSpiDataMapPayload();
				BeanUtils.copyProperties(spiMapData, payload);
				if (null != spiMapData.getSpiData()) {
					BeanUtils.copyProperties(spiMapData.getSpiData(), payload);
				}
				payload.setId(spiMapData.getId());
				payloadList.add(payload);
			}
		}
		return payloadList;
	}// end of method
}
