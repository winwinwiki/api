package com.winwin.winwin.service;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.Classification;
import com.winwin.winwin.entity.OrgClassificationMapping;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrganizationPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.ClassificationRepository;
import com.winwin.winwin.repository.OrgClassificationMapRepository;
import com.winwin.winwin.repository.OrganizationRepository;

@Component
public class OrganizationService implements IOrganizationService{
	@Autowired
	AddressRepository addressRepository;
	
	@Autowired
	OrganizationRepository organizationRepository;
	
	@Autowired
	OrgClassificationMapRepository orgClassificationMapRepository;
	
	@Autowired
	ClassificationRepository classificationRepository;
	
	@Override
	public void createOrganization(OrganizationPayload organizationPayload) {
		if(organizationPayload != null) {
			Address address = new Address();
			Organization organization = new Organization();
			organization.setName(organizationPayload.getName());
			organization.setSector(organizationPayload.getSector());
			organization.setSectorLevel(organizationPayload.getSectorLevel());
			organization.setDescription(organizationPayload.getDescription());
			if(organizationPayload.getAddress() != null) {
				address = saveAddress(organizationPayload.getAddress());
			}
			organization.setAddress(address);
			organization.setCreatedAt(new Date(System.currentTimeMillis()));
			organization.setUpdatedAt(new Date(System.currentTimeMillis()));
			organizationRepository.saveAndFlush(organization);
		}
	}
	
	@Override
	public void deleteOrganization(Long id) {
		Organization  organization = organizationRepository.findOrgById(id);
		if(organization != null) {
			organization.setIsActive(false);
			organization.getAddress().setIsActive(false);
			addressRepository.saveAndFlush(organization.getAddress());
			organizationRepository.saveAndFlush(organization);
		}
		
	}
	
	@Override
	public void updateOrgDetails(OrganizationPayload organizationPayload, Organization organization) {
		Address address = new Address();
		OrgClassificationMapping orgClassificationMapping = new OrgClassificationMapping();
		if(!StringUtils.isEmpty(organizationPayload.getDescription())) {
			organization.setDescription(organizationPayload.getDescription());
		}
		if(!StringUtils.isEmpty(organizationPayload.getPriority())) {
			organization.setPriority(organizationPayload.getPriority());
		}
		if(!StringUtils.isEmpty(organizationPayload.getRevenue())) {
			organization.setRevenue(organizationPayload.getRevenue());
		}
		if(!StringUtils.isEmpty(organizationPayload.getAssets())) {
			organization.setAssets(organizationPayload.getAssets());
		}
		if(!StringUtils.isEmpty(organizationPayload.getSector())) {
			organization.setSector(organizationPayload.getSector());
		}
		if(!StringUtils.isEmpty(organizationPayload.getSectorLevel())) {
			organization.setSectorLevel(organizationPayload.getSectorLevel());
		}
		Boolean isUpdated = updateAddress(organization,organizationPayload.getAddress());
		if(! isUpdated) {
			throw new OrganizationException("Address is null");
		}
		
		orgClassificationMapping = addClassification(organizationPayload,organization);
		
		if(orgClassificationMapping == null) {
			throw new OrganizationException("Request to update classification is invalid");
		}
		organizationRepository.saveAndFlush(organization);
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
		return addressRepository.saveAndFlush(address);
	}
	
	public Boolean updateAddress(Organization organization,AddressPayload addressPayload){
		//Address address = addressRepository.findAddressById(addressPayload.getId());
		if(addressPayload.getId().equals(organization.getAddress().getId())) {
			if(!StringUtils.isEmpty(addressPayload.getCountry())) {
				organization.getAddress().setCountry(addressPayload.getCountry());
			}
			if(!StringUtils.isEmpty(addressPayload.getState())) {
				organization.getAddress().setState(addressPayload.getState());
			}
			if(!StringUtils.isEmpty(addressPayload.getCity())) {
				organization.getAddress().setCity(addressPayload.getCity());
			}
			if(!StringUtils.isEmpty(addressPayload.getCounty())) {
				organization.getAddress().setCounty(addressPayload.getCounty());
			}
			if(!StringUtils.isEmpty(addressPayload.getZip())) {
				organization.getAddress().setZip(addressPayload.getZip());
			}
			if(!StringUtils.isEmpty(addressPayload.getStreet())) {
				organization.getAddress().setStreet(addressPayload.getStreet());
			}
			return true;
		}else {
			return false;
		}
//		if(!StringUtils.isEmpty(address)) {
//			if(!StringUtils.isEmpty(addressPayload.getCountry())) {
//				address.setCountry(addressPayload.getCountry());
//			}
//			if(!StringUtils.isEmpty(addressPayload.getState())) {
//				address.setState(addressPayload.getState());
//			}
//			if(!StringUtils.isEmpty(addressPayload.getCity())) {
//				address.setCity(addressPayload.getCity());
//			}
//			if(!StringUtils.isEmpty(addressPayload.getCounty())) {
//				address.setCounty(addressPayload.getCounty());
//			}
//			if(!StringUtils.isEmpty(addressPayload.getZip())) {
//				address.setZip(addressPayload.getZip());
//			}
//			if(!StringUtils.isEmpty(addressPayload.getStreet())) {
//				address.setStreet(addressPayload.getStreet());
//			}
//			return addressRepository.saveAndFlush(address);
//		}else {
//			return null;
//		}
	}
	
	public OrgClassificationMapping addClassification(OrganizationPayload organizationPayload,Organization organization){
		OrgClassificationMapping  orgClassificationMapping = orgClassificationMapRepository.findMappingForOrg(organizationPayload.getId());
		Classification  classification = classificationRepository.findClassificationById(organizationPayload.getClassificationId());
		if(StringUtils.isEmpty(classification)) {
			return null;
		}else {
			OrgClassificationMapping  orgClassificationMappingObj = new OrgClassificationMapping();
			if(StringUtils.isEmpty(orgClassificationMapping)) {
				orgClassificationMappingObj.setOrgId(organization);
				orgClassificationMappingObj.setClassificationId(classification);
			}else {
				orgClassificationMappingObj.setClassificationId(classification);
			}
			
			return orgClassificationMapRepository.saveAndFlush(orgClassificationMappingObj);
		}
	}
}
