/**
 * 
 */
package com.winwin.winwin.service;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrgRegionMaster;
import com.winwin.winwin.entity.OrgRegionServed;
import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.exception.OrgRegionServedException;
import com.winwin.winwin.payload.OrgRegionMasterPayload;
import com.winwin.winwin.payload.OrgRegionServedPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.OrgHistoryRepository;
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
	OrgHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgRegionServedService.class);

	private final Long REGION_ID = -1L;

	@Override
	public List<OrgRegionServed> createOrgRegionServed(List<OrgRegionServedPayload> orgRegionPayloadlist) {
		UserPayload user = getUserDetails();
		List<OrgRegionServed> orgRegionList = null;
		try {
			if (null != orgRegionPayloadlist && null != user) {
				orgRegionList = new ArrayList<OrgRegionServed>();
				for (OrgRegionServedPayload payload : orgRegionPayloadlist) {
					if (payload.getId() == null) {
						OrgRegionServed orgRegionServed = null;
						orgRegionServed = new OrgRegionServed();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

						setOrgRegionMasterData(payload, orgRegionServed, user);

						orgRegionServed.setOrgId(payload.getOrganizationId());
						orgRegionServed.setCreatedAt(sdf.parse(formattedDte));
						orgRegionServed.setUpdatedAt(sdf.parse(formattedDte));
						orgRegionServed.setCreatedBy(user.getEmail());
						orgRegionServed.setUpdatedBy(user.getEmail());
						orgRegionServed = orgRegionServedRepository.saveAndFlush(orgRegionServed);

						if (null != orgRegionServed && null != orgRegionServed.getOrgId()) {
							createOrgHistory(user, orgRegionServed.getOrgId(), sdf, formattedDte,
									OrganizationConstants.CREATE, OrganizationConstants.REGION,
									orgRegionServed.getId());
						}

						orgRegionList.add(orgRegionServed);

					}
					// for delete organization region served
					else if (null != payload.getId() && !(payload.getIsActive())) {
						OrgRegionServed region = null;
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

						region = orgRegionServedRepository.findOrgRegionById(payload.getId());
						if (region == null) {
							LOGGER.info(customMessageSource.getMessage("org.region.error.not_found"));
							throw new OrgRegionServedException(
									customMessageSource.getMessage("org.region.error.not_found"));
						} else {
							region.setIsActive(payload.getIsActive());
							region.setUpdatedAt(sdf.parse(formattedDte));
							region.setUpdatedBy(user.getEmail());
							region = orgRegionServedRepository.saveAndFlush(region);

							if (null != region && null != region.getOrgId()) {
								createOrgHistory(user, region.getOrgId(), sdf, formattedDte,
										OrganizationConstants.UPDATE, OrganizationConstants.REGION, region.getId());
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
	private void setOrgRegionMasterData(OrgRegionServedPayload payload, OrgRegionServed region, UserPayload user) {
		OrgRegionMaster regionMaster = null;
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

	public OrgRegionMaster saveOrganizationRegionMaster(OrgRegionMasterPayload payload, UserPayload user) {
		OrgRegionMaster region = new OrgRegionMaster();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
			if (!StringUtils.isEmpty(payload.getRegionName())) {
				region.setRegionName(payload.getRegionName());
			}
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
	public List<OrgRegionServed> getOrgRegionServedList(Long orgId) {
		return orgRegionServedRepository.findAllOrgRegionsList(orgId);
	}

	@Override
	public List<OrgRegionMaster> getOrgRegionMasterList() {
		return orgRegionMasterRepository.findAll();
	}

	/**
	 * @param user
	 * @return
	 */
	private UserPayload getUserDetails() {
		UserPayload user = null;
		if (null != SecurityContextHolder.getContext() && null != SecurityContextHolder.getContext().getAuthentication()
				&& null != SecurityContextHolder.getContext().getAuthentication().getDetails()) {
			user = (UserPayload) SecurityContextHolder.getContext().getAuthentication().getDetails();

		}
		return user;
	}

	/**
	 * @param user
	 * @param orgId
	 * @param sdf
	 * @param formattedDte
	 * @param actionPerformed
	 * @param entity
	 * @param entityId
	 * @throws ParseException
	 */
	private void createOrgHistory(UserPayload user, Long orgId, SimpleDateFormat sdf, String formattedDte,
			String actionPerformed, String entity, Long entityId) throws ParseException {
		OrganizationHistory orgHistory = new OrganizationHistory();
		orgHistory.setOrganizationId(orgId);
		orgHistory.setEntityId(entityId);
		orgHistory.setEntity(entity);
		orgHistory.setUpdatedAt(sdf.parse(formattedDte));
		orgHistory.setUpdatedBy(user.getUserDisplayName());
		orgHistory.setActionPerformed(actionPerformed);
		orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
	}

}
