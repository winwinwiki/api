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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationRegionServed;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.RegionServedException;
import com.winwin.winwin.payload.OrganizationRegionServedPayload;
import com.winwin.winwin.payload.RegionMasterFilterPayload;
import com.winwin.winwin.payload.RegionMasterPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationRegionServedRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.RegionMasterRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationRegionServedService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class OrganizationRegionServedServiceImpl implements OrganizationRegionServedService {

	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private OrganizationRegionServedRepository orgRegionServedRepository;
	@Autowired
	private RegionMasterRepository orgRegionMasterRepository;
	@Autowired
	private CustomMessageSource customMessageSource;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationHistoryService orgHistoryService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationRegionServedServiceImpl.class);

	private static final Long REGION_ID = -1L;

	/**
	 * create or update multiple OrganizationRegionServed for Organization
	 * create new entry in RegionMaster if value of REGION_ID is -1L
	 * 
	 * @param orgRegionPayloadlist
	 * @return
	 */
	@Override
	@Transactional
	@CacheEvict(value = "organization_region_master")
	public List<OrganizationRegionServed> createOrgRegionServed(
			List<OrganizationRegionServedPayload> orgRegionPayloadlist) {
		UserPayload user = userService.getCurrentUserDetails();
		List<OrganizationRegionServed> orgRegionList = null;
		try {
			if (null != orgRegionPayloadlist && null != user) {
				Date date = CommonUtils.getFormattedDate();
				orgRegionList = new ArrayList<>();
				for (OrganizationRegionServedPayload payload : orgRegionPayloadlist) {
					if (payload.getId() == null) {
						OrganizationRegionServed orgRegionServed = null;
						orgRegionServed = new OrganizationRegionServed();
						// set organization region master
						setOrgRegionMasterData(payload, orgRegionServed, user);
						if (null != payload.getOrganizationId()) {
							Organization organization = organizationRepository.findOrgById(payload.getOrganizationId());
							orgRegionServed.setOrganization(organization);
						}

						orgRegionServed.setCreatedAt(date);
						orgRegionServed.setUpdatedAt(date);
						orgRegionServed.setCreatedBy(user.getUserDisplayName());
						orgRegionServed.setUpdatedBy(user.getUserDisplayName());
						orgRegionServed.setCreatedByEmail(user.getEmail());
						orgRegionServed.setUpdatedByEmail(user.getEmail());
						orgRegionServed = orgRegionServedRepository.saveAndFlush(orgRegionServed);

						if (null != orgRegionServed && null != orgRegionServed.getOrganization()) {
							orgHistoryService.createOrganizationHistory(user, orgRegionServed.getOrganization().getId(),
									OrganizationConstants.CREATE, OrganizationConstants.REGION, orgRegionServed.getId(),
									orgRegionServed.getRegionMaster().getRegionName(), "");
						}
						orgRegionList.add(orgRegionServed);
					}
					// for delete organization region served
					else if (null != payload.getId() && !(payload.getIsActive())) {
						OrganizationRegionServed region = null;
						region = orgRegionServedRepository.findOrgRegionById(payload.getId());

						if (region == null) {
							LOGGER.info(customMessageSource.getMessage("org.region.error.not_found"));
							throw new RegionServedException(
									customMessageSource.getMessage("org.region.error.not_found"));
						} else {
							region.setIsActive(payload.getIsActive());
							region.setUpdatedAt(date);
							region.setUpdatedBy(user.getUserDisplayName());
							region.setUpdatedByEmail(user.getEmail());
							region = orgRegionServedRepository.saveAndFlush(region);

							if (null != region && null != region.getOrganization()) {
								orgHistoryService.createOrganizationHistory(user, region.getOrganization().getId(),
										OrganizationConstants.DELETE, OrganizationConstants.REGION, region.getId(),
										region.getRegionMaster().getRegionName(), "");
							}
							orgRegionList.add(region);
						}
					} // end of else if
				} // end of loop
			} // end of if
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.region.exception.created"), e);
		}
		return orgRegionList;
	}// end of method createOrgRegionServed

	/**
	 * @param payload
	 * @param region
	 */
	private void setOrgRegionMasterData(OrganizationRegionServedPayload payload, OrganizationRegionServed region,
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
						regionMaster = orgRegionMasterRepository.getOne(regionMasterId);
					}
					region.setRegionMaster(regionMaster);
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.region.master.error.created"), e);
		}
	}

	private RegionMaster saveOrganizationRegionMaster(RegionMasterPayload payload, UserPayload user) {
		RegionMaster region = new RegionMaster();
		try {
			Date date = CommonUtils.getFormattedDate();
			BeanUtils.copyProperties(payload, region);
			region.setCreatedAt(date);
			region.setUpdatedAt(date);
			region.setCreatedBy(user.getUserDisplayName());
			region.setUpdatedBy(user.getUserDisplayName());
			region.setCreatedByEmail(user.getEmail());
			region.setUpdatedByEmail(user.getEmail());
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.region.master.error.created"), e);
		}
		return orgRegionMasterRepository.saveAndFlush(region);
	}// end of method saveOrganizationRegionMaster

	/**
	 * returns OrganizationRegionServed List by orgId
	 * 
	 * @param orgId
	 */
	@Override
	public List<OrganizationRegionServed> getOrgRegionServedList(Long orgId) {
		return orgRegionServedRepository.findAllActiveOrgRegions(orgId);
	}

	/**
	 * returns RegionMaster List
	 * 
	 * @param payload
	 * @param response
	 */
	@Override
	@CachePut(value = "organization_region_master")
	public List<RegionMaster> getOrgRegionMasterList(RegionMasterFilterPayload payload, ExceptionResponse response) {
		List<RegionMaster> regionsList = new ArrayList<>();
		try {
			if (null != payload.getPageNo() && null != payload.getPageSize()) {
				String regionName = "";
				if (!StringUtils.isEmpty(payload.getNameSearch()))
					regionName = payload.getNameSearch();

				Pageable pageable = PageRequest.of(payload.getPageNo(), payload.getPageSize());
				return orgRegionMasterRepository.findRegionsByNameIgnoreCaseContaining(regionName, regionName,
						pageable);
			} else if (payload.getPageNo() == null) {
				throw new Exception("Page No found as null");
			} else if (payload.getPageSize() == null) {
				throw new Exception("Page Size found as null");
			}

		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("org.region.error.list"), e);
		}
		return regionsList;
	}

}
