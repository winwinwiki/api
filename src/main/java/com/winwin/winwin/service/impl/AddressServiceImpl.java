package com.winwin.winwin.service.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.Address;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.service.AddressService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	UserService userService;
	@Autowired
	AddressRepository addressRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataSetServiceImpl.class);

	/**
	 * save new address
	 * 
	 * @param addressPayload
	 * @param user
	 */
	public Address saveAddress(AddressPayload addressPayload, UserPayload user) {
		Address address = new Address();
		try {
			Date date = CommonUtils.getFormattedDate();
			if (addressPayload.getId() != null)
				address = addressRepository.findAddressById(addressPayload.getId());
			BeanUtils.copyProperties(addressPayload, address);
			address.setCreatedAt(date);
			address.setUpdatedAt(date);
			address.setCreatedBy(user.getUserDisplayName());
			address.setUpdatedBy(user.getUserDisplayName());
			address.setCreatedByEmail(user.getEmail());
			address.setUpdatedByEmail(user.getEmail());
		} catch (Exception e) {
			LOGGER.error("exception occured while creating address", e);
		}
		return addressRepository.saveAndFlush(address);
	}

	/**
	 * update existing address
	 * 
	 * @param address
	 * @param addressPayload
	 */
	public Boolean updateAddress(Address address, AddressPayload addressPayload) {
		try {
			if (null != addressPayload && null != addressPayload.getId()) {
				UserPayload user = userService.getCurrentUserDetails();
				Date date = CommonUtils.getFormattedDate();
				BeanUtils.copyProperties(addressPayload, address);
				address.setUpdatedAt(date);
				address.setUpdatedBy(user.getUserDisplayName());
				address.setUpdatedByEmail(user.getEmail());
				addressRepository.saveAndFlush(address);
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("exception occured while updating address", e);
		}
		return false;
	}

	/**
	 * returns AddressPayload from Address
	 * 
	 * @param address
	 */
	@Override
	public AddressPayload getAddressPayloadFromAddress(Address address) {
		AddressPayload addressPayload = new AddressPayload();
		BeanUtils.copyProperties(address, addressPayload);
		return addressPayload;
	}
}
