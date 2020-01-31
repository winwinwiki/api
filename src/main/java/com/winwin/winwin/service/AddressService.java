package com.winwin.winwin.service;

import com.winwin.winwin.entity.Address;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface AddressService {
	Address saveAddress(AddressPayload payload, UserPayload user);

	public Boolean updateAddress(Address address, AddressPayload addressPayload);

	AddressPayload getAddressPayloadFromAddress(Address address);
}
