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
import com.winwin.winwin.entity.ProgramRegionServed;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.exception.RegionServedException;
import com.winwin.winwin.payload.ProgramRegionServedPayload;
import com.winwin.winwin.payload.RegionMasterPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.ProgramRegionServedRepository;
import com.winwin.winwin.repository.RegionMasterRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.ProgramRegionServedService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class ProgramRegionServedServiceImpl implements ProgramRegionServedService {

	@Autowired
	AddressRepository addressRepository;

	@Autowired
	private RegionMasterRepository regionMasterRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService orgHistoryService;

	@Autowired
	ProgramRegionServedRepository programRegionServedRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationRegionServedServiceImpl.class);

	private final Long REGION_ID = -1L;

	@Override
	@Transactional
	public List<ProgramRegionServed> createProgramRegionServed(
			List<ProgramRegionServedPayload> programRegionPayloadList) {
		List<ProgramRegionServed> programRegionList = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			Date date = CommonUtils.getFormattedDate();
			if (null != programRegionPayloadList && null != user) {
				programRegionList = new ArrayList<>();
				for (ProgramRegionServedPayload payload : programRegionPayloadList) {
					if (payload.getId() == null) {
						ProgramRegionServed programRegionServed = null;
						programRegionServed = new ProgramRegionServed();
						setProgramRegionMasterData(payload, programRegionServed, user);
						BeanUtils.copyProperties(payload, programRegionServed);
						programRegionServed.setIsActive(true);;
						programRegionServed.setCreatedAt(date);
						programRegionServed.setUpdatedAt(date);
						programRegionServed.setCreatedBy(user.getEmail());
						programRegionServed.setUpdatedBy(user.getEmail());
						programRegionServed = programRegionServedRepository.saveAndFlush(programRegionServed);

						if (null != programRegionServed && null != payload.getOrganizationId()) {
							orgHistoryService.createOrganizationHistory(user, payload.getOrganizationId(),
									OrganizationConstants.CREATE, OrganizationConstants.REGION,
									programRegionServed.getId(), programRegionServed.getRegionMaster().getRegionName());
						}
						programRegionList.add(programRegionServed);
					} else if (null != payload.getId() && !(payload.getIsActive())) {
						ProgramRegionServed region = null;
						region = programRegionServedRepository.findProgramRegionById(payload.getId());
						if (region == null) {
							LOGGER.info(customMessageSource.getMessage("org.region.error.not_found"));
							throw new RegionServedException(
									customMessageSource.getMessage("org.region.error.not_found"));
						} else {
							region.setIsActive(payload.getIsActive());
							region.setUpdatedAt(date);
							region.setUpdatedBy(user.getEmail());
							region = programRegionServedRepository.saveAndFlush(region);

							if (null != region && null != payload.getOrganizationId()) {
								orgHistoryService.createOrganizationHistory(user, payload.getOrganizationId(),
										OrganizationConstants.UPDATE, OrganizationConstants.REGION, region.getId(),
										region.getRegionMaster().getRegionName());
							}
							programRegionList.add(region);
						}
					} // end of else if
				} // end of loop
			} // end of if
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.region.exception.created"), e);
		}
		return programRegionList;
	}

	@Override
	public List<ProgramRegionServed> getProgramRegionServedList(Long programId) {
		// TODO Auto-generated method stub
		return programRegionServedRepository.findAllProgramRegionsList(programId);
	}

	@Override
	public List<RegionMaster> getRegionMasterList() {
		// TODO Auto-generated method stub
		return regionMasterRepository.findAll();
	}

	private void setProgramRegionMasterData(ProgramRegionServedPayload payload, ProgramRegionServed region,
			UserPayload user) {
		RegionMaster regionMaster = null;
		try {
			if (null != payload.getRegion()) {
				Long regionMasterId = payload.getRegion().getRegionId();
				if (null != regionMasterId) {
					if (regionMasterId.equals(REGION_ID)) {
						regionMaster = saveOrganizationRegionMaster(payload.getRegion(), user);
						LOGGER.info(customMessageSource.getMessage("org.region.master.success.created"));
					} else {
						regionMaster = regionMasterRepository.getOne(regionMasterId);
						if (regionMaster == null) {
							throw new RegionServedException(
									"Org region master record not found for Id: " + regionMasterId + " in DB ");
						}
					}
					region.setRegionMaster(regionMaster);
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.region.master.error.created"), e);
		}
	}

	@Transactional
	public RegionMaster saveOrganizationRegionMaster(RegionMasterPayload payload, UserPayload user) {
		RegionMaster region = new RegionMaster();
		try {
			Date date = CommonUtils.getFormattedDate();
			if (!StringUtils.isEmpty(payload.getRegionName())) {
				region.setRegionName(payload.getRegionName());
			}
			region.setCreatedAt(date);
			region.setUpdatedAt(date);
			region.setCreatedBy(user.getEmail());
			region.setUpdatedBy(user.getEmail());
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.region.master.error.created"), e);
		}
		return regionMasterRepository.saveAndFlush(region);
	}// end of method saveOrganizationRegionMaster

}
