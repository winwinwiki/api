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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrganizationRegionServed;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.RegionServedException;
import com.winwin.winwin.payload.OrganizationRegionServedPayload;
import com.winwin.winwin.payload.RegionMasterFilterPayload;
import com.winwin.winwin.payload.RegionMasterPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.OrganizationRegionServedRepository;
import com.winwin.winwin.repository.RegionMasterRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationRegionServedService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class OrganizationRegionServedServiceImpl implements OrganizationRegionServedService {

	@Autowired
	AddressRepository addressRepository;

	@Autowired
	private OrganizationRegionServedRepository orgRegionServedRepository;

	@Autowired
	private RegionMasterRepository orgRegionMasterRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService orgHistoryService;
	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationRegionServedServiceImpl.class);

	private final Long REGION_ID = -1L;

	@Override
	@Transactional
	public List<OrganizationRegionServed> createOrgRegionServed(
			List<OrganizationRegionServedPayload> orgRegionPayloadlist) {
		UserPayload user = userService.getCurrentUserDetails();
		List<OrganizationRegionServed> orgRegionList = null;
		try {
			if (null != orgRegionPayloadlist && null != user) {
				Date date = CommonUtils.getFormattedDate();
				orgRegionList = new ArrayList<OrganizationRegionServed>();
				for (OrganizationRegionServedPayload payload : orgRegionPayloadlist) {
					if (payload.getId() == null) {
						OrganizationRegionServed orgRegionServed = null;
						orgRegionServed = new OrganizationRegionServed();
						setOrgRegionMasterData(payload, orgRegionServed, user);
						orgRegionServed.setOrgId(payload.getOrganizationId());
						orgRegionServed.setCreatedAt(date);
						orgRegionServed.setUpdatedAt(date);
						orgRegionServed.setCreatedBy(user.getEmail());
						orgRegionServed.setUpdatedBy(user.getEmail());
						orgRegionServed.setAdminUrl(payload.getAdminUrl());

						orgRegionServed = orgRegionServedRepository.saveAndFlush(orgRegionServed);

						if (null != orgRegionServed && null != orgRegionServed.getOrgId()) {
							orgHistoryService.createOrganizationHistory(user, orgRegionServed.getOrgId(),
									OrganizationConstants.CREATE, OrganizationConstants.REGION, orgRegionServed.getId(),
									orgRegionServed.getRegionMaster().getRegionName());
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
							region.setUpdatedBy(user.getEmail());
							region = orgRegionServedRepository.saveAndFlush(region);

							if (null != region && null != region.getOrgId()) {
								orgHistoryService.createOrganizationHistory(user, region.getOrgId(),
										OrganizationConstants.UPDATE, OrganizationConstants.REGION, region.getId(),
										region.getRegionMaster().getRegionName());
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
			BeanUtils.copyProperties(payload, region);
			region.setCreatedAt(date);
			region.setUpdatedAt(date);
			region.setCreatedBy(user.getEmail());
			region.setUpdatedBy(user.getEmail());
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.region.master.error.created"), e);
		}
		return orgRegionMasterRepository.saveAndFlush(region);
	}// end of method saveOrganizationRegionMaster

	@Override
	public List<OrganizationRegionServed> getOrgRegionServedList(Long orgId) {
		return orgRegionServedRepository.findAllOrgRegionsList(orgId);
	}

	@Override
	public List<RegionMaster> getOrgRegionMasterList(RegionMasterFilterPayload payload, ExceptionResponse response) {
		List<RegionMaster> regionsList = new ArrayList<RegionMaster>();
		try {
			if (null != payload.getPageNo() && null != payload.getPageSize()) {
				if (null != payload.getNameSearch()) {
					Pageable pageable = PageRequest.of(payload.getPageNo(), payload.getPageSize(), Sort.by("name"));
					return orgRegionMasterRepository.findRegionsByNameIgnoreCaseContaining(payload.getNameSearch(),
							pageable);
				}else{
					throw new Exception("nameSearch found as null");
				}
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
