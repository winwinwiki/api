package com.winwin.winwin.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.winwin.winwin.entity.Address;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.service.AddressService;
import com.winwin.winwin.service.UserService;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	UserService userService;
	@Autowired
	AddressRepository addressRepository;

	public Address saveAddress(AddressPayload addressPayload) {
		UserPayload user = userService.getCurrentUserDetails();
		Address address = new Address();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
			address.setCountry(addressPayload.getCountry());
			address.setState(addressPayload.getState());
			address.setCity(addressPayload.getCity());
			address.setCounty(addressPayload.getCounty());
			address.setZip(addressPayload.getZip());
			address.setPlaceId(addressPayload.getPlaceId());
			address.setCreatedAt(sdf.parse(formattedDte));
			address.setUpdatedAt(sdf.parse(formattedDte));
			address.setCreatedBy(user.getEmail());
			address.setUpdatedBy(user.getEmail());
			address.setAdminUrl(addressPayload.getAdminUrl());
		} catch (Exception e) {

		}
		return addressRepository.saveAndFlush(address);
	}

	public Boolean updateAddress(Address address, AddressPayload addressPayload) {
		UserPayload user = userService.getCurrentUserDetails();

		if (null != addressPayload && null != addressPayload.getId()) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				if (!StringUtils.isEmpty(addressPayload.getCountry())) {
					address.setCountry(addressPayload.getCountry());
				}
				if (!StringUtils.isEmpty(addressPayload.getState())) {
					address.setState(addressPayload.getState());
				}
				if (!StringUtils.isEmpty(addressPayload.getCity())) {
					address.setCity(addressPayload.getCity());
				}
				if (!StringUtils.isEmpty(addressPayload.getCounty())) {
					address.setCounty(addressPayload.getCounty());
				}
				if (!StringUtils.isEmpty(addressPayload.getZip())) {
					address.setZip(addressPayload.getZip());
				}
				address.setStreet(addressPayload.getStreet());
				address.setPlaceId(addressPayload.getPlaceId());
				address.setUpdatedAt(sdf.parse(formattedDte));
				address.setUpdatedBy(user.getEmail());
				address.setAdminUrl(addressPayload.getAdminUrl());

				addressRepository.saveAndFlush(address);
				return true;
			} catch (Exception e) {

			}

		}
		return false;
	}

	@Override
	public AddressPayload getAddressPayloadFromAddress(Address address) {
		// TODO Auto-generated method stub
		AddressPayload addressPayload = new AddressPayload();
		addressPayload.setId(address.getId());
		addressPayload.setCountry(address.getCountry());
		addressPayload.setState(address.getState());
		addressPayload.setCity(address.getCity());
		addressPayload.setCounty(address.getCounty());
		addressPayload.setZip(address.getZip());
		addressPayload.setStreet(address.getStreet());
		addressPayload.setPlaceId(address.getPlaceId());
		addressPayload.setAdminUrl(address.getAdminUrl());

		return addressPayload;
	}
}
