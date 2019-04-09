/**
 * 
 */
package com.winwin.winwin.service;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrgRegionMaster;
import com.winwin.winwin.entity.OrgRegionServed;
import com.winwin.winwin.exception.OrgRegionServedException;
import com.winwin.winwin.payload.OrgRegionMasterPayload;
import com.winwin.winwin.payload.OrgRegionServedPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.OrgRegionMasterRepository;
import com.winwin.winwin.repository.OrgRegionServedRepository;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class OrgRegionServedService implements IOrgRegionServedService {

	@Autowired
	AddressRepository addressRepository;

	@Autowired
	private OrgRegionServedRepository orgRegionServedRepository;

	@Autowired
	private OrgRegionMasterRepository orgRegionMasterRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgRegionServedService.class);

	private final Long REGION_ID = -1L;

	@Override
	public List<OrgRegionServed> createOrgRegionServed(List<OrgRegionServedPayload> orgRegionPayloadlist) {
		List<OrgRegionServed> orgRegionList = null;
		try {
			if (null != orgRegionPayloadlist) {
				orgRegionList = new ArrayList<OrgRegionServed>();
				for (OrgRegionServedPayload payload : orgRegionPayloadlist) {
					if (payload.getId() == null) {
						OrgRegionServed orgRegionServed = null;
						orgRegionServed = new OrgRegionServed();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

						setOrgRegionMasterData(payload, orgRegionServed);

						orgRegionServed.setOrgId(payload.getOrganizationId());
						orgRegionServed.setCreatedAt(sdf.parse(formattedDte));
						orgRegionServed.setUpdatedAt(sdf.parse(formattedDte));
						orgRegionServed.setCreatedBy(OrganizationConstants.CREATED_BY);
						orgRegionServed.setUpdatedBy(OrganizationConstants.UPDATED_BY);
						orgRegionServed = orgRegionServedRepository.saveAndFlush(orgRegionServed);

						orgRegionList.add(orgRegionServed);

					}
					// for delete organization region served
					else if (null != payload.getId() && !(payload.getIsActive())) {
						OrgRegionServed region = null;
						region = orgRegionServedRepository.findOrgRegionById(payload.getId());
						if (region == null) {
							LOGGER.info(customMessageSource.getMessage("org.region.error.not_found"));
							throw new OrgRegionServedException(
									customMessageSource.getMessage("org.region.error.not_found"));
						} else {
							region.setIsActive(payload.getIsActive());
							region.setUpdatedAt(new Date(System.currentTimeMillis()));
							region.setUpdatedBy(OrganizationConstants.UPDATED_BY);
							region = orgRegionServedRepository.saveAndFlush(region);

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
	private void setOrgRegionMasterData(OrgRegionServedPayload payload, OrgRegionServed region) {
		OrgRegionMaster regionMaster = null;
		try {
			if (null != payload.getOrgRegionMasterPayload()) {
				Long regionMasterId = payload.getOrgRegionMasterPayload().getId();
				if (null != regionMasterId) {
					if (regionMasterId.equals(REGION_ID)) {
						regionMaster = saveOrganizationRegionMaster(payload.getOrgRegionMasterPayload());
						LOGGER.info(customMessageSource.getMessage("org.region.master.success.created"));
					} else {
						regionMaster = orgRegionMasterRepository.getOne(regionMasterId);
						if (regionMaster == null) {
							throw new OrgRegionServedException(
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

	public OrgRegionMaster saveOrganizationRegionMaster(OrgRegionMasterPayload payload) {
		OrgRegionMaster region = new OrgRegionMaster();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
			if (!StringUtils.isEmpty(payload.getRegionName())) {
				region.setRegionName(payload.getRegionName());
			}
			region.setCreatedAt(sdf.parse(formattedDte));
			region.setUpdatedAt(sdf.parse(formattedDte));
			region.setCreatedBy(OrganizationConstants.CREATED_BY);
			region.setUpdatedBy(OrganizationConstants.UPDATED_BY);
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.region.master.error.created"), e);
		}

		return orgRegionMasterRepository.saveAndFlush(region);
	}// end of method saveOrganizationRegionMaster
	
	@Override
	public List<OrgRegionServed> getOrgRegionServedList() {
		return orgRegionServedRepository.findAllOrgRegionsList();
	}

	@Override
	public List<OrgRegionMaster> getOrgRegionMasterList() {
		return orgRegionMasterRepository.findAll();
	}

}
