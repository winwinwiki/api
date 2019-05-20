/**
 * 
 */
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
import com.winwin.winwin.entity.OrganizationRegionServed;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.exception.RegionServedException;
import com.winwin.winwin.payload.OrganizationRegionServedPayload;
import com.winwin.winwin.payload.RegionMasterPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.OrganizationRegionServedRepository;
import com.winwin.winwin.repository.RegionMasterRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationRegionServedService;
import com.winwin.winwin.service.UserService;

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
	public List<OrganizationRegionServed> createOrgRegionServed(
			List<OrganizationRegionServedPayload> orgRegionPayloadlist) {
		UserPayload user = userService.getCurrentUserDetails();
		List<OrganizationRegionServed> orgRegionList = null;
		try {
			if (null != orgRegionPayloadlist && null != user) {
				orgRegionList = new ArrayList<OrganizationRegionServed>();
				for (OrganizationRegionServedPayload payload : orgRegionPayloadlist) {
					if (payload.getId() == null) {
						OrganizationRegionServed orgRegionServed = null;
						orgRegionServed = new OrganizationRegionServed();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

						setOrgRegionMasterData(payload, orgRegionServed, user);

						orgRegionServed.setOrgId(payload.getOrganizationId());
						orgRegionServed.setCreatedAt(sdf.parse(formattedDte));
						orgRegionServed.setUpdatedAt(sdf.parse(formattedDte));
						orgRegionServed.setCreatedBy(user.getEmail());
						orgRegionServed.setUpdatedBy(user.getEmail());
						orgRegionServed.setAdminUrl(payload.getAdminUrl());

						orgRegionServed = orgRegionServedRepository.saveAndFlush(orgRegionServed);

						if (null != orgRegionServed && null != orgRegionServed.getOrgId()) {
							orgHistoryService.createOrganizationHistory(user, orgRegionServed.getOrgId(), sdf,
									formattedDte, OrganizationConstants.CREATE, OrganizationConstants.REGION,
									orgRegionServed.getId(), orgRegionServed.getRegionMaster().getRegionName());
						}

						orgRegionList.add(orgRegionServed);

					}
					// for delete organization region served
					else if (null != payload.getId() && !(payload.getIsActive())) {
						OrganizationRegionServed region = null;
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

						region = orgRegionServedRepository.findOrgRegionById(payload.getId());
						if (region == null) {
							LOGGER.info(customMessageSource.getMessage("org.region.error.not_found"));
							throw new RegionServedException(
									customMessageSource.getMessage("org.region.error.not_found"));
						} else {
							region.setIsActive(payload.getIsActive());
							region.setUpdatedAt(sdf.parse(formattedDte));
							region.setUpdatedBy(user.getEmail());
							region = orgRegionServedRepository.saveAndFlush(region);

							if (null != region && null != region.getOrgId()) {
								orgHistoryService.createOrganizationHistory(user, region.getOrgId(), sdf, formattedDte,
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

	public RegionMaster saveOrganizationRegionMaster(RegionMasterPayload payload, UserPayload user) {
		RegionMaster region = new RegionMaster();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
			if (!StringUtils.isEmpty(payload.getRegionName())) {
				region.setRegionName(payload.getRegionName());
			}
			region.setAdminUrl(payload.getAdminUrl());
			region.setCreatedAt(sdf.parse(formattedDte));
			region.setUpdatedAt(sdf.parse(formattedDte));
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
	public List<RegionMaster> getOrgRegionMasterList() {
		return orgRegionMasterRepository.findAll();
	}

}
