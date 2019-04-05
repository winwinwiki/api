package com.winwin.winwin.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.Classification;
import com.winwin.winwin.entity.OrgClassificationMapping;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrgChartPayload;
import com.winwin.winwin.payload.OrgDepartmentPayload;
import com.winwin.winwin.payload.OrgDivisionPayload;
import com.winwin.winwin.payload.OrganizationPayload;
import com.winwin.winwin.payload.SubOrganizationPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.ClassificationRepository;
import com.winwin.winwin.repository.OrgClassificationMapRepository;
import com.winwin.winwin.repository.OrganizationRepository;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class OrganizationService implements IOrganizationService {
	@Autowired
	AddressRepository addressRepository;

	@Autowired
	OrganizationRepository organizationRepository;

	@Autowired
	OrgClassificationMapRepository orgClassificationMapRepository;

	@Autowired
	ClassificationRepository classificationRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationService.class);

	@Override
	public Organization createOrganization(OrganizationPayload organizationPayload) {
		Organization organization = null;
		try {
			if (organizationPayload != null) {
				Address address = new Address();
				organization = new Organization();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organization.setName(organizationPayload.getName());
				organization.setSector(organizationPayload.getSector());
				organization.setSectorLevel(organizationPayload.getSectorLevel());
				organization.setDescription(organizationPayload.getDescription());
				if (organizationPayload.getAddress() != null) {
					address = saveAddress(organizationPayload.getAddress());
				}
				organization.setType(OrganizationConstants.ORGANIZATION);
				organization.setParentId(organizationPayload.getParentId());
				organization.setAddress(address);
				organization.setCreatedAt(sdf.parse(formattedDte));
				organization.setUpdatedAt(sdf.parse(formattedDte));
				organization.setCreatedBy(OrganizationConstants.CREATED_BY);
				organization.setUpdatedBy(OrganizationConstants.UPDATED_BY);
				organization = organizationRepository.saveAndFlush(organization);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.created"), e);
		}

		return organization;
	}

	@Override
	public void deleteOrganization(Long id) {
		Organization organization = organizationRepository.findOrgById(id);
		if (organization != null) {
			organization.setIsActive(false);
			organization.getAddress().setIsActive(false);
			addressRepository.saveAndFlush(organization.getAddress());
			organizationRepository.saveAndFlush(organization);
		}

	}

	@Override
	public Organization updateOrgDetails(OrganizationPayload organizationPayload, Organization organization) {
		@SuppressWarnings("unused")
		OrgClassificationMapping orgClassificationMapping = new OrgClassificationMapping();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		try {
			if (!StringUtils.isEmpty(organizationPayload.getName())) {
				organization.setName(organizationPayload.getName());
			}
			if (!StringUtils.isEmpty(organizationPayload.getDescription())) {
				organization.setDescription(organizationPayload.getDescription());
			}
			if (!StringUtils.isEmpty(organizationPayload.getPriority())) {
				organization.setPriority(organizationPayload.getPriority());
			}
			if (null != organizationPayload.getRevenue()) {
				organization.setRevenue(organizationPayload.getRevenue());
			}
			if (null != organizationPayload.getAssets()) {
				organization.setAssets(organizationPayload.getAssets());
			}
			if (!StringUtils.isEmpty(organizationPayload.getSector())) {
				organization.setSector(organizationPayload.getSector());
			}
			if (!StringUtils.isEmpty(organizationPayload.getSectorLevel())) {
				organization.setSectorLevel(organizationPayload.getSectorLevel());
			}
			if (!StringUtils.isEmpty(organizationPayload.getSectorLevelName())) {
				organization.setSectorLevelName(organizationPayload.getSectorLevelName());
			}
			if (!StringUtils.isEmpty(organizationPayload.getWebsiteUrl())) {
				organization.setWebsiteUrl(organizationPayload.getWebsiteUrl());
			}
			if (!StringUtils.isEmpty(organizationPayload.getSocialUrl())) {
				organization.setSocialUrl(organizationPayload.getSocialUrl());
			}
			if (!StringUtils.isEmpty(organizationPayload.getValues())) {
				organization.setValues(organizationPayload.getValues());
			}
			if (!StringUtils.isEmpty(organizationPayload.getPurpose())) {
				organization.setPurpose(organizationPayload.getPurpose());
			}
			if (!StringUtils.isEmpty(organizationPayload.getSelfInterest())) {
				organization.setSelfInterest(organizationPayload.getSelfInterest());
			}

			if (!StringUtils.isEmpty(organizationPayload.getBusinessModel())) {
				organization.setBusinessModel(organizationPayload.getBusinessModel());
			}
			if (!StringUtils.isEmpty(organizationPayload.getMissionStatement())) {
				organization.setMissionStatement(organizationPayload.getMissionStatement());
			}
			if (!StringUtils.isEmpty(organizationPayload.getContactInfo())) {
				organization.setContactInfo(organizationPayload.getContactInfo());
			}
			if (null != organizationPayload.getPopulationServed()) {
				organization.setPopulationServed(organizationPayload.getPopulationServed());
			}

			Boolean isUpdated = updateAddress(organization, organizationPayload.getAddress());
			if (!isUpdated) {
				throw new OrganizationException(customMessageSource.getMessage("org.exception.address.null"));
			}

			organization.setUpdatedAt(sdf.parse(formattedDte));
			organization.setUpdatedBy(OrganizationConstants.UPDATED_BY);

			orgClassificationMapping = addClassification(organizationPayload, organization);

			/*
			 * if (orgClassificationMapping == null) { throw new
			 * OrganizationException(
			 * "Request to update classification is invalid"); }
			 */

			organization = organizationRepository.saveAndFlush(organization);
		} catch (ParseException e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.updated"), e);
		}
		return organization;
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

	public Boolean updateAddress(Organization organization, AddressPayload addressPayload) {
		if (null != addressPayload && null != addressPayload.getId()) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				if (!StringUtils.isEmpty(addressPayload.getCountry())) {
					organization.getAddress().setCountry(addressPayload.getCountry());
				}
				if (!StringUtils.isEmpty(addressPayload.getState())) {
					organization.getAddress().setState(addressPayload.getState());
				}
				if (!StringUtils.isEmpty(addressPayload.getCity())) {
					organization.getAddress().setCity(addressPayload.getCity());
				}
				if (!StringUtils.isEmpty(addressPayload.getCounty())) {
					organization.getAddress().setCounty(addressPayload.getCounty());
				}
				if (!StringUtils.isEmpty(addressPayload.getZip())) {
					organization.getAddress().setZip(addressPayload.getZip());
				}
				if (!StringUtils.isEmpty(addressPayload.getStreet())) {
					organization.getAddress().setStreet(addressPayload.getStreet());
				}
				if (!StringUtils.isEmpty(addressPayload.getPlaceId())) {
					organization.getAddress().setPlaceId(addressPayload.getPlaceId());
				}

				organization.getAddress().setUpdatedAt(sdf.parse(formattedDte));
				organization.getAddress().setUpdatedBy(OrganizationConstants.UPDATED_BY);

				return true;
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.exception.address.updated"), e);
			}

		}
		return false;
	}

	public OrgClassificationMapping addClassification(OrganizationPayload organizationPayload,
			Organization organization) {
		Classification classification = null;
		OrgClassificationMapping orgClassificationMapping = null;
		if (null != organizationPayload && null != organizationPayload.getId()) {
			orgClassificationMapping = orgClassificationMapRepository.findMappingForOrg(organizationPayload.getId());
		}

		if (null != organizationPayload.getClassificationId()) {
			classification = classificationRepository.findClassificationById(organizationPayload.getClassificationId());
		}

		if (StringUtils.isEmpty(classification)) {
			return null;
		} else {
			OrgClassificationMapping orgClassificationMappingObj = new OrgClassificationMapping();
			if (StringUtils.isEmpty(orgClassificationMapping)) {
				orgClassificationMappingObj.setOrgId(organization);
				orgClassificationMappingObj.setClassificationId(classification);
			} else {
				orgClassificationMappingObj.setClassificationId(classification);
			}

			return orgClassificationMapRepository.saveAndFlush(orgClassificationMappingObj);
		}
	}

	@Override
	public List<Organization> getOrganizationList() {
		return organizationRepository.findAllOrganizationList();
	}// end of method getOrganizationList

	@Override
	public List<Organization> getProgramList(Long orgId) {
		return organizationRepository.findAllProgramList(orgId);
	}// end of method getOrganizationList

	@Override
	public Organization createProgram(OrganizationPayload organizationPayload) {
		Organization organization = null;
		try {
			if (organizationPayload != null) {
				Address address = new Address();
				organization = new Organization();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organization.setName(organizationPayload.getName());
				organization.setSector(organizationPayload.getSector());
				organization.setSectorLevel(organizationPayload.getSectorLevel());
				organization.setDescription(organizationPayload.getDescription());
				if (organizationPayload.getAddress() != null) {
					address = saveAddress(organizationPayload.getAddress());
				}
				organization.setType(OrganizationConstants.PROGRAM);
				organization.setParentId(organizationPayload.getParentId());
				organization.setAddress(address);
				organization.setCreatedAt(sdf.parse(formattedDte));
				organization.setUpdatedAt(sdf.parse(formattedDte));
				organization.setCreatedBy(OrganizationConstants.CREATED_BY);
				organization.setUpdatedBy(OrganizationConstants.UPDATED_BY);
				organization = organizationRepository.saveAndFlush(organization);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("prg.exception.created"), e);
		}

		return organization;
	}

	@Override
	public OrgChartPayload getOrgCharts(Organization organization, Long orgId) {
		List<Organization> divisions = organizationRepository.findAllDivisionList(orgId);
		List<Organization> departments = organizationRepository.findAllDepartmentList();
		OrgChartPayload payload = new OrgChartPayload();
		try {
			AddressPayload orgAddress = null;
			payload.setId(organization.getId());
			payload.setName(organization.getName());
			orgAddress = getLocationPayload(organization, orgAddress);
			payload.setLocation(orgAddress);
			payload.setChildrenType(OrganizationConstants.DIVISION);

			if (null != divisions) {
				List<OrgDivisionPayload> divisionPayloadList = new ArrayList<OrgDivisionPayload>();
				HashMap<Long, List<OrgDepartmentPayload>> orgDeptMap = new HashMap<Long, List<OrgDepartmentPayload>>();
				if (null != departments) {
					setOrgDepartmentsMap(departments, orgDeptMap);
				}

				for (Organization orgDivision : divisions) {
					OrgDivisionPayload divPayload = new OrgDivisionPayload();
					AddressPayload divAddress = null;
					divPayload.setId(orgDivision.getId());
					divPayload.setName(orgDivision.getName());
					divAddress = getLocationPayload(organization, divAddress);
					divPayload.setLocation(divAddress);
					divPayload.setChildrenType(OrganizationConstants.DEPARTMENT);

					if (!orgDeptMap.isEmpty()) {
						divPayload.setChildren(orgDeptMap.get(orgDivision.getId()));
					}
					divisionPayloadList.add(divPayload);
				}

				payload.setChildren(divisionPayloadList);
			} // end of if( null != divisions)
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.chart.error.list"), e);
		}
		return payload;
	}

	/**
	 * @param organization
	 * @param addressPayload
	 * @return
	 */
	private AddressPayload getLocationPayload(Organization organization, AddressPayload addressPayload) {
		if (null != organization.getAddress()) {
			addressPayload = new AddressPayload();
			addressPayload.setId(organization.getAddress().getId());
			addressPayload.setCountry(organization.getAddress().getCountry());
			addressPayload.setState(organization.getAddress().getState());
			addressPayload.setCity(organization.getAddress().getCity());
			addressPayload.setCounty(organization.getAddress().getCounty());
			addressPayload.setZip(organization.getAddress().getZip());
			addressPayload.setStreet(organization.getAddress().getStreet());
			addressPayload.setPlaceId(organization.getAddress().getPlaceId());
		}
		return addressPayload;
	}

	/**
	 * @param spiList
	 * @param orgDepartmentsMap
	 */
	private void setOrgDepartmentsMap(List<Organization> departments,
			HashMap<Long, List<OrgDepartmentPayload>> orgDepartmentsMap) {
		for (Organization organization : departments) {
			OrgDepartmentPayload payload = new OrgDepartmentPayload();
			AddressPayload deptAddress = null;
			payload.setId(organization.getId());
			payload.setName(organization.getName());
			deptAddress = getLocationPayload(organization, deptAddress);
			payload.setLocation(deptAddress);
			if (!orgDepartmentsMap.containsKey(organization.getParentId())) {
				List<OrgDepartmentPayload> orgList = new ArrayList<OrgDepartmentPayload>();
				orgList.add(payload);
				orgDepartmentsMap.put(organization.getParentId(), orgList);
			} else {
				orgDepartmentsMap.get(organization.getParentId()).add(payload);
			}
		}
	}// end of method setSpiDimensionsMap

	@Override
	public Organization createSubOrganization(SubOrganizationPayload payload) {
		Organization organization = null;
		try {
			if (null != payload) {
				Address address = new Address();
				AddressPayload addressPayload = new AddressPayload();
				addressPayload.setCountry("");
				organization = new Organization();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

				if (!(StringUtils.isEmpty(payload.getChildOrgName()))) {
					organization.setName(payload.getChildOrgName());
				}

				if (!(StringUtils.isEmpty(payload.getChildOrgType()))) {
					organization.setType(payload.getChildOrgType());
				}

				if (null != payload.getParentId()) {
					organization.setParentId(payload.getParentId());
				}
				address = saveAddress(addressPayload);

				organization.setAddress(address);

				organization.setCreatedAt(sdf.parse(formattedDte));
				organization.setUpdatedAt(sdf.parse(formattedDte));
				organization.setCreatedBy(OrganizationConstants.CREATED_BY);
				organization.setUpdatedBy(OrganizationConstants.UPDATED_BY);
				organization = organizationRepository.saveAndFlush(organization);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.created"), e);
		}

		return organization;
	}
}
