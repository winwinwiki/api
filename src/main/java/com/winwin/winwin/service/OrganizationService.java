package com.winwin.winwin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrganizationPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.OrganizationRepository;


@Component
public class OrganizationService implements IOrganizationService{
	@Autowired
	AddressRepository addressRepository;
	
	@Autowired
	OrganizationRepository organizationRepository;
	
	@Override
	public void createOrganization(OrganizationPayload organizationPayload) {
		if(organizationPayload != null) {
			Address address = new Address();
			Organization organization = new Organization();
			organization.setName(organizationPayload.getName());
			organization.setSector(organizationPayload.getSector());
			organization.setSectorLevel(organizationPayload.getSectorLevel());
			if(organizationPayload.getAddress() != null) {
				address = saveAddress(organizationPayload.getAddress());
			}
			organization.setAddress(address);
			organizationRepository.saveAndFlush(organization);
		}
	}
	
	public Address saveAddress(AddressPayload addressPayload) {
		Address address = new Address();
		address.setCountry(addressPayload.getCountry());
		address.setCity(addressPayload.getCity());
		address.setState(addressPayload.getState());
		address.setCounty(addressPayload.getCounty());
		address.setZip(addressPayload.getZip());
		address.setStreet(addressPayload.getStreet());
		return addressRepository.saveAndFlush(address);
				
			
		//return null;
	}
}
