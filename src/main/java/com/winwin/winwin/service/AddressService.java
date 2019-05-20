package com.winwin.winwin.service;

import com.winwin.winwin.entity.Address;
import com.winwin.winwin.payload.AddressPayload;

public interface AddressService {
	Address saveAddress(AddressPayload payload);

	public Boolean updateAddress(Address address, AddressPayload addressPayload);

	AddressPayload getAddressPayloadFromAddress(Address address);
}
