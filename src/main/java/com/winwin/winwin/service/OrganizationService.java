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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.Classification;
import com.winwin.winwin.entity.OrgClassificationMapping;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrgChartPayload;
import com.winwin.winwin.payload.OrgDepartmentPayload;
import com.winwin.winwin.payload.OrgDivisionPayload;
import com.winwin.winwin.payload.OrgHistoryPayload;
import com.winwin.winwin.payload.OrganizationPayload;
import com.winwin.winwin.payload.SubOrganizationPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.ClassificationRepository;
import com.winwin.winwin.repository.OrgClassificationMapRepository;
import com.winwin.winwin.repository.OrgHistoryRepository;
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
	OrgHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationService.class);

	@Override
	public Organization createOrganization(OrganizationPayload organizationPayload) {
		Organization organization = null;
		UserPayload user = getUserDetails();
		try {
			if (null != organizationPayload && null != user) {
				Address address = new Address();
				organization = new Organization();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organization.setName(organizationPayload.getName());
				organization.setSector(organizationPayload.getSector());
				organization.setSectorLevel(organizationPayload.getSectorLevel());
				organization.setDescription(organizationPayload.getDescription());
				if (organizationPayload.getAddress() != null) {
					address = saveAddress(organizationPayload.getAddress(), user);
				}
				organization.setType(OrganizationConstants.ORGANIZATION);
				organization.setParentId(organizationPayload.getParentId());
				organization.setAddress(address);
				organization.setCreatedAt(sdf.parse(formattedDte));
				organization.setUpdatedAt(sdf.parse(formattedDte));
				organization.setCreatedBy(user.getEmail());
				organization.setUpdatedBy(user.getEmail());
				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization.getId()) {
					OrganizationHistory orgHistory = new OrganizationHistory();
					orgHistory.setOrganizationId(organization.getId());
					orgHistory.setUpdatedAt(sdf.parse(formattedDte));
					orgHistory.setUpdatedBy(user.getUserDisplayName());
					orgHistory.setActionPerformed(OrganizationConstants.CREATE);
					orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
				}

			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.created"), e);
		}

		return organization;
	}

	@Override
	public void deleteOrganization(Long id) {
		UserPayload user = getUserDetails();

		if (null != user) {
			try {
				Organization organization = organizationRepository.findOrgById(id);
				if (organization != null) {
					organization.setIsActive(false);
					organization.getAddress().setIsActive(false);
					addressRepository.saveAndFlush(organization.getAddress());
					organizationRepository.saveAndFlush(organization);

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
					if (null != organization.getId()) {
						OrganizationHistory orgHistory = new OrganizationHistory();
						orgHistory.setOrganizationId(organization.getId());
						orgHistory.setUpdatedAt(sdf.parse(formattedDte));
						orgHistory.setUpdatedBy(user.getUserDisplayName());
						orgHistory.setActionPerformed(OrganizationConstants.DELETE);
						orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);

					}
				}
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.error.deleted"), e);
			}
		}

	}

	@Override
	public Organization updateOrgDetails(OrganizationPayload organizationPayload, Organization organization) {
		@SuppressWarnings("unused")
		OrgClassificationMapping orgClassificationMapping = new OrgClassificationMapping();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		UserPayload user = getUserDetails();
		if (null != organizationPayload && null != user) {
			try {
				if (!StringUtils.isEmpty(organizationPayload.getName())) {
					organization.setName(organizationPayload.getName());
				}
				if (!StringUtils.isEmpty(organizationPayload.getSector())) {
					organization.setSector(organizationPayload.getSector());
				}
				if (!StringUtils.isEmpty(organizationPayload.getSectorLevel())) {
					organization.setSectorLevel(organizationPayload.getSectorLevel());
				}

				organization.setDescription(organizationPayload.getDescription());
				organization.setPriority(organizationPayload.getPriority());
				organization.setRevenue(organizationPayload.getRevenue());
				organization.setAssets(organizationPayload.getAssets());
				organization.setSectorLevelName(organizationPayload.getSectorLevelName());
				organization.setNaicsCode(organizationPayload.getNaicsCode());
				organization.setNteeCode(organizationPayload.getNteeCode());
				organization.setWebsiteUrl(organizationPayload.getWebsiteUrl());
				organization.setFacebookUrl(organizationPayload.getFacebookUrl());
				organization.setLinkedinUrl(organizationPayload.getLinkedinUrl());
				organization.setTwitterUrl(organizationPayload.getTwitterUrl());
				organization.setValues(organizationPayload.getValues());
				organization.setPurpose(organizationPayload.getPurpose());
				organization.setSelfInterest(organizationPayload.getSelfInterest());
				organization.setBusinessModel(organizationPayload.getBusinessModel());
				organization.setMissionStatement(organizationPayload.getMissionStatement());
				organization.setContactInfo(organizationPayload.getContactInfo());
				organization.setPopulationServed(organizationPayload.getPopulationServed());
				organization.setTagStatus(organizationPayload.getTagStatus());

				Boolean isUpdated = updateAddress(organization, organizationPayload.getAddress(), user);
				if (!isUpdated) {
					throw new OrganizationException(customMessageSource.getMessage("org.exception.address.null"));
				}

				organization.setUpdatedAt(sdf.parse(formattedDte));
				organization.setUpdatedBy(user.getEmail());

				orgClassificationMapping = addClassification(organizationPayload, organization);

				/*
				 * if (orgClassificationMapping == null) { throw new
				 * OrganizationException(
				 * "Request to update classification is invalid"); }
				 */

				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization.getId()) {
					OrganizationHistory orgHistory = new OrganizationHistory();
					orgHistory.setOrganizationId(organization.getId());
					orgHistory.setUpdatedAt(sdf.parse(formattedDte));
					orgHistory.setUpdatedBy(user.getUserDisplayName());
					orgHistory.setActionPerformed(OrganizationConstants.UPDATE);
					orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
				}
			} catch (ParseException e) {
				LOGGER.error(customMessageSource.getMessage("org.exception.updated"), e);
			}
		}
		return organization;
	}

	public Address saveAddress(AddressPayload addressPayload, UserPayload user) {
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
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.address.created"), e);
		}
		return addressRepository.saveAndFlush(address);
	}

	public Boolean updateAddress(Organization organization, AddressPayload addressPayload, UserPayload user) {
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
				organization.getAddress().setStreet(addressPayload.getStreet());
				organization.getAddress().setPlaceId(addressPayload.getPlaceId());
				organization.getAddress().setUpdatedAt(sdf.parse(formattedDte));
				organization.getAddress().setUpdatedBy(user.getEmail());

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
		UserPayload user = getUserDetails();

		Organization organization = null;
		try {
			if (null != organizationPayload && null != user) {
				Address address = new Address();
				organization = new Organization();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				organization.setName(organizationPayload.getName());
				organization.setSector(organizationPayload.getSector());
				organization.setSectorLevel(organizationPayload.getSectorLevel());
				organization.setDescription(organizationPayload.getDescription());
				if (organizationPayload.getAddress() != null) {
					address = saveAddress(organizationPayload.getAddress(), user);
				}
				organization.setType(OrganizationConstants.PROGRAM);
				organization.setParentId(organizationPayload.getParentId());
				organization.setAddress(address);
				organization.setCreatedAt(sdf.parse(formattedDte));
				organization.setUpdatedAt(sdf.parse(formattedDte));
				organization.setCreatedBy(user.getEmail());
				organization.setUpdatedBy(user.getEmail());
				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization.getId()) {
					OrganizationHistory orgHistory = new OrganizationHistory();
					orgHistory.setOrganizationId(organization.getId());
					orgHistory.setUpdatedAt(sdf.parse(formattedDte));
					orgHistory.setUpdatedBy(user.getUserDisplayName());
					orgHistory.setActionPerformed(OrganizationConstants.CREATE);
					orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
				}
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
					divAddress = getLocationPayload(orgDivision, divAddress);
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
		UserPayload user = getUserDetails();
		Organization organization = null;
		try {
			if (null != payload && null != user) {
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
				address = saveAddress(addressPayload, user);

				organization.setAddress(address);

				organization.setCreatedAt(sdf.parse(formattedDte));
				organization.setUpdatedAt(sdf.parse(formattedDte));
				organization.setCreatedBy(user.getEmail());
				organization.setUpdatedBy(user.getEmail());
				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization.getId()) {
					OrganizationHistory orgHistory = new OrganizationHistory();
					orgHistory.setOrganizationId(organization.getId());
					orgHistory.setUpdatedAt(sdf.parse(formattedDte));
					orgHistory.setUpdatedBy(user.getUserDisplayName());
					orgHistory.setActionPerformed(OrganizationConstants.CREATE);
					orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.created"), e);
		}

		return organization;
	}

	@Override
	public List<OrgHistoryPayload> getOrgHistoryDetails(Long orgId) {
		List<OrgHistoryPayload> payloadList = null;

		try {
			List<OrganizationHistory> orgHistoryList = orgHistoryRepository.findOrgHistoryDetails(orgId);

			if (null != orgHistoryList) {
				payloadList = new ArrayList<OrgHistoryPayload>();

				for (OrganizationHistory history : orgHistoryList) {
					OrgHistoryPayload payload = new OrgHistoryPayload();
					payload.setId(history.getId());
					payload.setModifiedBy(history.getUpdatedBy());
					payload.setModifiedAt(history.getUpdatedAt());

					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			throw e;
		}

		return payloadList;
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

}
