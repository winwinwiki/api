package com.winwin.winwin.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.Classification;
import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationClassification;
import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrganizationChartPayload;
import com.winwin.winwin.payload.OrganizationDepartmentPayload;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.payload.OrganizationHistoryPayload;
import com.winwin.winwin.payload.OrganizationRequestPayload;
import com.winwin.winwin.payload.SubOrganizationPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.ClassificationRepository;
import com.winwin.winwin.repository.NaicsDataRepository;
import com.winwin.winwin.repository.NteeDataRepository;
import com.winwin.winwin.repository.OrgClassificationMapRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationService;
import com.winwin.winwin.service.UserService;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {
	@Autowired
	AddressRepository addressRepository;

	@Autowired
	OrganizationRepository organizationRepository;

	@Autowired
	OrgClassificationMapRepository orgClassificationMapRepository;

	@Autowired
	ClassificationRepository classificationRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	NaicsDataRepository naicsRepository;

	@Autowired
	NteeDataRepository nteeRepository;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService orgHistoryService;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

	@Override
	public Organization createOrganization(OrganizationRequestPayload organizationPayload) {
		Organization organization = null;
		UserPayload user = userService.getCurrentUserDetails();
		try {
			if (null != organizationPayload && null != user) {
				Address address = new Address();
				if (organizationPayload.getId() != null)
					organization = organizationRepository.findOrgById(organizationPayload.getId());

				if (organization == null)
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
				if (organizationPayload.getNaicsCode() != null) {
					NaicsData naicsCode = naicsRepository.findById(organizationPayload.getNaicsCode()).orElse(null);
					organization.setNaicsCode(naicsCode);
				}
				if (organizationPayload.getNteeCode() != null) {
					NteeData naicsCode = nteeRepository.findById(organizationPayload.getNteeCode()).orElse(null);
					organization.setNteeCode(naicsCode);
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
					orgHistoryService.createOrganizationHistory(user, organization.getId(), sdf, formattedDte,
							OrganizationConstants.CREATE, OrganizationConstants.ORGANIZATION, organization.getId(),
							organization.getName());
				}

			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.created"), e);
		}

		return organization;
	}

	public List<Organization> createOrganizations(List<OrganizationRequestPayload> organizationPayloadList) {

		List<Organization> organizationList = new ArrayList<>();
		Iterator<OrganizationRequestPayload> itr = organizationPayloadList.iterator();
		while (itr.hasNext()) {

			Organization org = createOrganization(itr.next());
			if (org != null)
				organizationList.add(org);
			else
				itr.remove();
		}
		return organizationList;
	}

	@Override
	public void deleteOrganization(Long id, String type) {
		UserPayload user = userService.getCurrentUserDetails();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		if (null != user) {
			try {
				Organization organization = organizationRepository.findOrgById(id);
				if (organization != null) {
					organization.setIsActive(false);
					organization.getAddress().setIsActive(false);
					addressRepository.saveAndFlush(organization.getAddress());
					organization = organizationRepository.saveAndFlush(organization);

					if (null != organization) {
						orgHistoryService.createOrganizationHistory(user, organization.getId(), sdf, formattedDte,
								OrganizationConstants.DELETE, type, organization.getId(), organization.getName());
					}
				}
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.error.deleted"), e);
			}
		}

	}

	@Override
	public Organization updateOrgDetails(OrganizationRequestPayload organizationPayload, Organization organization,
			String type) {
		@SuppressWarnings("unused")
		OrganizationClassification orgClassificationMapping = new OrganizationClassification();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		UserPayload user = userService.getCurrentUserDetails();
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

				if (organizationPayload.getNaicsCode() != null) {
					NaicsData naicsCode = naicsRepository.findById(organizationPayload.getNaicsCode()).orElse(null);
					organization.setNaicsCode(naicsCode);
				}
				if (organizationPayload.getNteeCode() != null) {
					NteeData naicsCode = nteeRepository.findById(organizationPayload.getNteeCode()).orElse(null);
					organization.setNteeCode(naicsCode);
				}

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
				 * if (orgClassificationMapping == null) { throw new OrganizationException(
				 * "Request to update classification is invalid"); }
				 */

				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization) {
					orgHistoryService.createOrganizationHistory(user, organization.getId(), sdf, formattedDte,
							OrganizationConstants.UPDATE, type, organization.getId(), organization.getName());
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

	public OrganizationClassification addClassification(OrganizationRequestPayload organizationPayload,
			Organization organization) {
		Classification classification = null;
		OrganizationClassification orgClassificationMapping = null;
		if (null != organizationPayload && null != organizationPayload.getId()) {
			orgClassificationMapping = orgClassificationMapRepository.findMappingForOrg(organizationPayload.getId());
		}

		if (null != organizationPayload.getClassificationId()) {
			classification = classificationRepository.findClassificationById(organizationPayload.getClassificationId());
		}

		if (StringUtils.isEmpty(classification)) {
			return null;
		} else {
			OrganizationClassification orgClassificationMappingObj = new OrganizationClassification();
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

	public List<Organization> getOrganizationList(OrganizationFilterPayload payload) {
		if (payload.getNameSearch() != null)
			return organizationRepository.findByNameIgnoreCaseContaining(payload.getNameSearch());
		else
			return organizationRepository.filterOrganization(payload, OrganizationConstants.ORGANIZATION, null);
	}

	public List<Organization> getProgramList(Long orgId, OrganizationFilterPayload payload) {
		if (payload.getNameSearch() != null)
			return organizationRepository.findProgramByNameIgnoreCaseContaining(payload.getNameSearch(), orgId);
		else
			return organizationRepository.filterOrganization(payload, OrganizationConstants.PROGRAM, orgId);
	}

	@Override
	public List<Organization> getProgramList(Long orgId) {
		return organizationRepository.findAllProgramList(orgId);
	}// end of method getOrganizationList

	@Override
	public Organization createProgram(OrganizationRequestPayload organizationPayload) {
		UserPayload user = userService.getCurrentUserDetails();

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
				if (organizationPayload.getNaicsCode() != null) {
					NaicsData naicsCode = naicsRepository.findById(organizationPayload.getNaicsCode()).orElse(null);
					organization.setNaicsCode(naicsCode);
				}
				if (organizationPayload.getNteeCode() != null) {
					NteeData naicsCode = nteeRepository.findById(organizationPayload.getNteeCode()).orElse(null);
					organization.setNteeCode(naicsCode);
				}

				organization.setType(OrganizationConstants.PROGRAM);
				organization.setParentId(organizationPayload.getParentId());
				organization.setAddress(address);
				organization.setCreatedAt(sdf.parse(formattedDte));
				organization.setUpdatedAt(sdf.parse(formattedDte));
				organization.setCreatedBy(user.getEmail());
				organization.setUpdatedBy(user.getEmail());
				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization) {
					orgHistoryService.createOrganizationHistory(user, organization.getId(), sdf, formattedDte,
							OrganizationConstants.CREATE, OrganizationConstants.PROGRAM, organization.getId(),
							organization.getName());
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("prg.exception.created"), e);
		}

		return organization;
	}

	@Override
	public OrganizationChartPayload getOrgCharts(Organization organization, Long orgId) {
		List<Organization> children = organizationRepository.findAllChildren(orgId);
		OrganizationChartPayload payload = new OrganizationChartPayload();
		List<OrganizationChartPayload> child = new ArrayList<>();
		for (Organization childOrg : children) {
			child.add(getOrgCharts(childOrg, childOrg.getId()));
		}
		payload.setChildren(child);
		try {
			AddressPayload orgAddress = null;
			payload.setId(organization.getId());
			payload.setName(organization.getName());
			orgAddress = getLocationPayload(organization, orgAddress);
			payload.setLocation(orgAddress);

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
			HashMap<Long, List<OrganizationDepartmentPayload>> orgDepartmentsMap) {
		for (Organization organization : departments) {
			OrganizationDepartmentPayload payload = new OrganizationDepartmentPayload();
			AddressPayload deptAddress = null;
			payload.setId(organization.getId());
			payload.setName(organization.getName());
			deptAddress = getLocationPayload(organization, deptAddress);
			payload.setLocation(deptAddress);
			if (!orgDepartmentsMap.containsKey(organization.getParentId())) {
				List<OrganizationDepartmentPayload> orgList = new ArrayList<OrganizationDepartmentPayload>();
				orgList.add(payload);
				orgDepartmentsMap.put(organization.getParentId(), orgList);
			} else {
				orgDepartmentsMap.get(organization.getParentId()).add(payload);
			}
		}
	}// end of method setSpiDimensionsMap

	@Override
	public Organization createSubOrganization(SubOrganizationPayload payload) {
		UserPayload user = userService.getCurrentUserDetails();
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

				if (null != organization) {
					orgHistoryService.createOrganizationHistory(user, organization.getId(), sdf, formattedDte,
							OrganizationConstants.CREATE, OrganizationConstants.ORGANIZATION, organization.getId(),
							organization.getName());
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.created"), e);
		}

		return organization;
	}

	@Override
	public List<OrganizationHistoryPayload> getOrgHistoryDetails(Long orgId) {
		List<OrganizationHistoryPayload> payloadList = null;

		try {

			List<OrganizationHistory> orgHistoryList = orgHistoryRepository.findOrgHistoryDetails(orgId);

			if (null != orgHistoryList) {
				payloadList = new ArrayList<OrganizationHistoryPayload>();
				Organization organization = organizationRepository.findOrgById(orgId);

				for (OrganizationHistory history : orgHistoryList) {
					OrganizationHistoryPayload payload = new OrganizationHistoryPayload();
					payload.setId(history.getId());

					if (null != organization) {
						payload.setParentEntityName(organization.getName());
					}
					payload.setEntityType(history.getEntityType());
					payload.setEntityName(history.getEntityName());
					payload.setActionPerformed(history.getActionPerformed());
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

}
