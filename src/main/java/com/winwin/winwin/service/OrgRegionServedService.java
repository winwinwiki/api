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
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgRegionServedService.class);

	@Override
	public List<OrgRegionServed> createOrgRegionServed(List<OrgRegionServedPayload> orgRegionPayloadlist) {
		List<OrgRegionServed> orgRegionList = null;
		Address address = null;
		try {
			if (null != orgRegionPayloadlist) {
				orgRegionList = new ArrayList<OrgRegionServed>();
				for (OrgRegionServedPayload payload : orgRegionPayloadlist) {
					if (payload.getId() == null) {
						OrgRegionServed orgRegionServed = null;
						orgRegionServed = new OrgRegionServed();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
						orgRegionServed.setOrgId(payload.getOrganizationId());
						if (null != payload.getAddress()) {
							address = saveAddress(payload.getAddress());
							orgRegionServed.setAddress(address);
						}
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

	@Override
	public List<OrgRegionServed> getOrgRegionServedList() {
		return orgRegionServedRepository.findAllOrgRegionsList();
	}

	public Address saveAddress(AddressPayload addressPayload) {
		Address address = new Address();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
			address.setCountry(addressPayload.getCountry());
			address.setCity(addressPayload.getCity());
			address.setState(addressPayload.getState());
			address.setCounty(addressPayload.getCounty());
			address.setZip(addressPayload.getZip());
			address.setStreet(addressPayload.getStreet());
			address.setPlaceId(addressPayload.getPlaceId());
			address.setCreatedAt(sdf.parse(formattedDte));
			address.setUpdatedAt(sdf.parse(formattedDte));
			address.setCreatedBy(OrganizationConstants.CREATED_BY);
			address.setUpdatedBy(OrganizationConstants.UPDATED_BY);
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.address.created"), e);
		}
		return addressRepository.saveAndFlush(address);
	}

}
