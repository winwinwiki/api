/**
 * 
 */
package com.winwin.winwin.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.OrgRegionServed;
import com.winwin.winwin.exception.OrgRegionServedException;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrgRegionServedPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.OrgRegionServedRepository;

/**
 * @author ArvindK
 *
 */
@Component
public class OrgRegionServedService implements IOrgRegionServedService {

	@Autowired
	AddressRepository addressRepository;

	@Autowired
	private OrgRegionServedRepository orgRegionServedRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgRegionServedService.class);

	@Override
	public List<OrgRegionServed> createOrgRegionServed(List<OrgRegionServedPayload> orgRegionPayloadlist) {
		OrgRegionServed orgRegionServed = null;
		List<OrgRegionServed> orgRegionList = null;
		Address address = null;
		if (null != orgRegionPayloadlist) {
			orgRegionList = new ArrayList<OrgRegionServed>();
			for (OrgRegionServedPayload payload : orgRegionPayloadlist) {
				orgRegionServed = new OrgRegionServed();
				orgRegionServed.setOrgId(payload.getOrganizationId());
				if (null != payload.getAddress()) {
					address = saveAddress(payload.getAddress());
					orgRegionServed.setAddress(address);
				}
				orgRegionServed.setCreatedAt(new Date(System.currentTimeMillis()));
				orgRegionServed.setUpdatedAt(new Date(System.currentTimeMillis()));
				orgRegionServed.setCreatedBy(OrganizationConstants.CREATED_BY);
				orgRegionServed.setUpdatedBy(OrganizationConstants.UPDATED_BY);
				orgRegionServedRepository.saveAndFlush(orgRegionServed);

				orgRegionServed = orgRegionServedRepository.findLastOrgRegion();
				orgRegionList.add(orgRegionServed);
			}

		}

		return orgRegionList;
	}

	@Override
	public OrgRegionServed updateOrgRegionServed(OrgRegionServedPayload payload, OrgRegionServed orgRegionServed) {
		if (!payload.getIsActive()) {
			orgRegionServed.setIsActive(payload.getIsActive());
		}

		Boolean isUpdated = updateAddress(orgRegionServed, payload.getAddress());
		if (!isUpdated) {
			try {
				throw new OrgRegionServedException(customMessageSource.getMessage("org.exception.address.null"));
			} catch (OrgRegionServedException e) {
				LOGGER.error(customMessageSource.getMessage("org.exception.address.updated"), e);
			}
		}

		orgRegionServed.setUpdatedAt(new Date(System.currentTimeMillis()));
		orgRegionServed.setUpdatedBy(OrganizationConstants.UPDATED_BY);

		orgRegionServedRepository.saveAndFlush(orgRegionServed);

		if (null != payload && null != payload.getId()) {
			orgRegionServed = orgRegionServedRepository.findOrgRegionById(payload.getId());
		}
		return orgRegionServed;
	}

	@Override
	public List<OrgRegionServed> getOrgRegionServedList() {
		return orgRegionServedRepository.findAllOrgRegionsList();
	}

	public Address saveAddress(AddressPayload addressPayload) {
		Address address = new Address();
		address.setCountry(addressPayload.getCountry());
		address.setCity(addressPayload.getCity());
		address.setState(addressPayload.getState());
		address.setCounty(addressPayload.getCounty());
		address.setZip(addressPayload.getZip());
		address.setStreet(addressPayload.getStreet());
		address.setCreatedAt(new Date(System.currentTimeMillis()));
		address.setUpdatedAt(new Date(System.currentTimeMillis()));
		address.setCreatedBy(OrganizationConstants.CREATED_BY);
		address.setUpdatedBy(OrganizationConstants.UPDATED_BY);
		return addressRepository.saveAndFlush(address);
	}

	public Boolean updateAddress(OrgRegionServed orgRegionServed, AddressPayload addressPayload) {
		if (null != addressPayload && null != addressPayload.getId()) {
			if (addressPayload.getId().equals(orgRegionServed.getAddress().getId())) {
				if (!StringUtils.isEmpty(addressPayload.getCountry())) {
					orgRegionServed.getAddress().setCountry(addressPayload.getCountry());
				}
				if (!StringUtils.isEmpty(addressPayload.getState())) {
					orgRegionServed.getAddress().setState(addressPayload.getState());
				}
				if (!StringUtils.isEmpty(addressPayload.getCity())) {
					orgRegionServed.getAddress().setCity(addressPayload.getCity());
				}
				if (!StringUtils.isEmpty(addressPayload.getCounty())) {
					orgRegionServed.getAddress().setCounty(addressPayload.getCounty());
				}
				if (!StringUtils.isEmpty(addressPayload.getZip())) {
					orgRegionServed.getAddress().setZip(addressPayload.getZip());
				}
				if (!StringUtils.isEmpty(addressPayload.getStreet())) {
					orgRegionServed.getAddress().setStreet(addressPayload.getStreet());
				}

				orgRegionServed.getAddress().setUpdatedAt(new Date(System.currentTimeMillis()));
				orgRegionServed.getAddress().setUpdatedBy(OrganizationConstants.UPDATED_BY);

				return true;
			}

		}
		return false;
	}

}
