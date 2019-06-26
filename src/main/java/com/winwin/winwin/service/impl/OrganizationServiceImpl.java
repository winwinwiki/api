package com.winwin.winwin.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.Classification;
import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationClassification;
import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.entity.OrganizationSdgData;
import com.winwin.winwin.entity.OrganizationSpiData;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.SdgData;
import com.winwin.winwin.entity.SpiData;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.NaicsDataMappingPayload;
import com.winwin.winwin.payload.NaicsMappingCsvPayload;
import com.winwin.winwin.payload.NteeDataMappingPayload;
import com.winwin.winwin.payload.NteeMappingCsvPayload;
import com.winwin.winwin.payload.OrganizationChartPayload;
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
import com.winwin.winwin.repository.OrgSdgDataMapRepository;
import com.winwin.winwin.repository.OrgSpiDataMapRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.repository.SpiDataRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.OrganizationService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;
import com.winwin.winwin.util.CsvUtils;

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
	ProgramRepository programRepository;
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
	OrganizationNoteService organizationNoteService;
	@Autowired
	protected CustomMessageSource customMessageSource;
	@Autowired
	GetAwsS3ObjectServiceImpl awsS3ObjectServiceImpl;
	@Autowired
	SpiDataRepository spiDataRepository;
	@Autowired
	OrgSpiDataMapRepository orgSpiDataMapRepository;
	@Autowired
	SdgDataRepository sdgDataRepository;
	@Autowired
	OrgSdgDataMapRepository orgSdgDataMapRepository;
	@Autowired
	NteeDataRepository nteeDataRepository;
	@Autowired
	NaicsDataRepository naicsDataRepository;
	@Autowired
	CsvUtils csvUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

	Map<Long, NaicsData> naicsMap = null;
	Map<Long, NteeData> nteeMap = null;

	@Override
	@Transactional
	public Organization createOrganization(OrganizationRequestPayload organizationPayload, ExceptionResponse response) {
		Organization organization = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (null != organizationPayload && null != user) {
				organization = setOrganizationData(organizationPayload, user);
				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization.getId()) {
					orgHistoryService.createOrganizationHistory(user, organization.getId(),
							OrganizationConstants.CREATE, OrganizationConstants.ORGANIZATION, organization.getId(),
							organization.getName(), "");
				}

			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.created"), e);
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return organization;
	}

	@Override
	@Transactional
	public List<Organization> createOrganizations(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response) {
		List<Organization> organizationList = saveOrganizationsForBulkUpload(organizationPayloadList, response,
				OrganizationConstants.CREATE, "org.exception.created");

		return organizationList;
	}

	@Override
	@Transactional
	public List<Organization> updateOrganizations(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response) {
		List<Organization> organizationList = saveOrganizationsForBulkUpload(organizationPayloadList, response,
				OrganizationConstants.UPDATE, "org.exception.updated");

		return organizationList;
	}

	@Override
	@Transactional
	public void deleteOrganization(Long id, String type, ExceptionResponse response) {
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (null != user) {
				Organization organization = organizationRepository.findOrgById(id);
				if (organization != null) {
					Date date = CommonUtils.getFormattedDate();
					organization.setIsActive(false);
					organization.setUpdatedAt(date);
					organization.setUpdatedBy(user.getEmail());
					organization.getAddress().setIsActive(false);
					organization.getAddress().setUpdatedAt(date);
					organization.getAddress().setUpdatedBy(user.getEmail());
					addressRepository.saveAndFlush(organization.getAddress());
					organization = organizationRepository.saveAndFlush(organization);

					if (null != organization) {
						orgHistoryService.createOrganizationHistory(user, organization.getId(),
								OrganizationConstants.DELETE, type, organization.getId(), organization.getName(), "");
					}
				}
			}
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("org.error.deleted"), e);
		}

	}

	@Override
	@Transactional
	public Organization updateOrgDetails(OrganizationRequestPayload organizationPayload, Organization organization,
			String type, ExceptionResponse response) {
		@SuppressWarnings("unused")
		OrganizationClassification orgClassificationMapping = new OrganizationClassification();

		if (null != organizationPayload) {
			Date date = CommonUtils.getFormattedDate();
			try {
				UserPayload user = userService.getCurrentUserDetails();
				if (null != user) {
					BeanUtils.copyProperties(organizationPayload, organization);
					if (organizationPayload.getNaicsCode() != null) {
						NaicsData naicsCode = naicsRepository.findById(organizationPayload.getNaicsCode()).orElse(null);
						organization.setNaicsCode(naicsCode);
					}
					if (organizationPayload.getNteeCode() != null) {
						NteeData naicsCode = nteeRepository.findById(organizationPayload.getNteeCode()).orElse(null);
						organization.setNteeCode(naicsCode);
					}

					Boolean isUpdated = updateAddress(organization, organizationPayload.getAddress(), user);
					if (!isUpdated) {
						throw new OrganizationException(customMessageSource.getMessage("org.exception.address.null"));
					}
					organization.setIsActive(true);
					organization.setUpdatedAt(date);
					organization.setUpdatedBy(user.getEmail());
					orgClassificationMapping = addClassification(organizationPayload, organization);
					organization = organizationRepository.saveAndFlush(organization);

					if (null != organization) {
						orgHistoryService.createOrganizationHistory(user, organization.getId(),
								OrganizationConstants.UPDATE, type, organization.getId(), organization.getName(), "");
					}

				}

			} catch (Exception e) {
				response.setErrorMessage(e.getMessage());
				response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
				LOGGER.error(customMessageSource.getMessage("org.exception.updated"), e);
			}
		}
		return organization;
	}

	@Override
	public List<Organization> getOrganizationList() {
		return organizationRepository.findAllOrganizationList();
	}// end of method getOrganizationList

	@Override
	public List<Organization> getOrganizationList(OrganizationFilterPayload payload, ExceptionResponse response) {
		List<Organization> orgList = new ArrayList<Organization>();
		try {
			if (null != payload.getPageNo() && null != payload.getPageSize()) {
				return organizationRepository.filterOrganization(payload, OrganizationConstants.ORGANIZATION, null,
						payload.getPageNo(), payload.getPageSize());
			} else if (payload.getPageNo() == null) {
				throw new Exception("Page No found as null");
			} else if (payload.getPageSize() == null) {
				throw new Exception("Page Size found as null");
			}

		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("org.error.list"), e);
		}
		return orgList;
	}

	@Override
	public Integer getOrgCounts(OrganizationFilterPayload payload, ExceptionResponse response) {
		Integer noOfRecords = 0;
		try {
			noOfRecords = organizationRepository.getFilterOrganizationCount(payload, OrganizationConstants.ORGANIZATION,
					null);

		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());

			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("org.error.list"), e);
		}
		return noOfRecords;
	}

	@Override
	public List<Organization> getProgramList(Long orgId) {
		return organizationRepository.findAllProgramList(orgId);
	}// end of method getOrganizationList

	@Override
	@Transactional
	public Organization createProgram(OrganizationRequestPayload organizationPayload) {
		Organization organization = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (null != organizationPayload && null != user) {
				Date date = CommonUtils.getFormattedDate();
				Address address = new Address();
				organization = new Organization();
				BeanUtils.copyProperties(organizationPayload, organization);

				if (organizationPayload.getAddress() != null) {
					address = saveAddress(organizationPayload.getAddress(), user);
					organization.setAddress(address);
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
				organization.setIsActive(true);
				;
				organization.setCreatedAt(date);
				organization.setUpdatedAt(date);
				organization.setCreatedBy(user.getEmail());
				organization.setUpdatedBy(user.getEmail());
				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization) {
					orgHistoryService.createOrganizationHistory(user, organization.getId(),
							OrganizationConstants.CREATE, OrganizationConstants.PROGRAM, organization.getId(), "",
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

	@Override
	@Transactional
	public Organization createSubOrganization(SubOrganizationPayload payload) {
		Organization organization = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (null != payload && null != user) {
				Date date = CommonUtils.getFormattedDate();
				Address address = new Address();
				AddressPayload addressPayload = new AddressPayload();
				Long parentId = null;
				addressPayload.setCountry("");
				organization = new Organization();
				if (!(StringUtils.isEmpty(payload.getChildOrgName()))) {
					organization.setName(payload.getChildOrgName());
				}
				if (!(StringUtils.isEmpty(payload.getChildOrgType()))) {
					organization.setType(payload.getChildOrgType());
				}
				if (null != payload.getParentId()) {
					parentId = payload.getParentId();
					organization.setParentId(parentId);
				}
				address = saveAddress(addressPayload, user);
				organization.setAddress(address);
				organization.setCreatedAt(date);
				organization.setUpdatedAt(date);
				organization.setCreatedBy(user.getEmail());
				organization.setUpdatedBy(user.getEmail());
				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization) {
					orgHistoryService.createOrganizationHistory(user, parentId, OrganizationConstants.CREATE,
							OrganizationConstants.ORGANIZATION, organization.getId(), organization.getName(), "");
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
					if (null != organization && history.getProgramId() == null) {
						payload.setParentEntityName(organization.getName());
						payload.setParentEntityType(OrganizationConstants.ORGANIZATION);
					} else if (null != organization && null != history.getProgramId()) {
						Program program = programRepository.findProgramById(history.getProgramId());
						if (null != program)
							payload.setParentEntityName(program.getName());
						payload.setParentEntityType(OrganizationConstants.PROGRAM);
					}
					payload.setEntityType(history.getEntityType());
					payload.setEntityName(history.getEntityName());
					payload.setEntityCode(history.getEntityCode());
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

	public List<Organization> saveOrganizationsForBulkUpload(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response, String operationPerformed, String customMessage) {
		ExceptionResponse errorResForNaics = new ExceptionResponse();
		ExceptionResponse errorResForNtee = new ExceptionResponse();
		List<Organization> organizationList = new ArrayList<Organization>();
		try {
			UserPayload user = userService.getCurrentUserDetails();
			// get NaicsCode AutoTag SpiSdgMapping
			Map<String, NaicsDataMappingPayload> naicsMap = getNaicsSpiSdgMap(errorResForNaics);

			if (!StringUtils.isEmpty(errorResForNaics.getErrorMessage())) {
				throw new Exception(errorResForNaics.getErrorMessage());
			}
			// get NteeCode AutoTag SpiSdgMapping
			Map<String, NteeDataMappingPayload> nteeMap = getNteeSpiSdgMap(errorResForNtee);

			if (!StringUtils.isEmpty(errorResForNtee.getErrorMessage())) {
				throw new Exception(errorResForNtee.getErrorMessage());
			}

			Map<String, String> notesMap = new HashMap<String, String>();
			if (null != organizationPayloadList) {
				// set Naics-Ntee code map
				setNaicsNteeMap();
				for (OrganizationRequestPayload organizationPayload : organizationPayloadList) {
					if (null != organizationPayload && null != user) {
						if (!StringUtils.isEmpty(organizationPayload.getName())
								&& !StringUtils.isEmpty(organizationPayload.getNotes())) {
							notesMap.put(organizationPayload.getName(), organizationPayload.getNotes());
						}

						if (operationPerformed.equals(OrganizationConstants.CREATE)) {
							organizationPayload.setTagStatus(OrganizationConstants.AUTOTAGGED);
							organizationPayload.setPriority(OrganizationConstants.PRIORITY_NORMAL);
							organizationList.add(
									setOrganizationDataForBulkUpload(organizationPayload, user, operationPerformed));

						} else if (operationPerformed.equals(OrganizationConstants.UPDATE)) {
							if (null != organizationPayload.getId()) {
								Organization organization = organizationRepository
										.findOrgById(organizationPayload.getId());
								if (organization == null)
									throw new OrganizationException(
											"organization with Id:" + organizationPayload.getId()
													+ "is not found in DB to perform update operation");
								organizationList.add(setOrganizationDataForBulkUpload(organizationPayload, user,
										operationPerformed));
							} else {
								throw new Exception(
										"Organization id is found as null in the file to perform bulk update operation for organizations");
							}
						}
					}
				}
			}
			organizationList = organizationRepository.saveAll(organizationList);

			// create notes for multiple organizations
			List<OrganizationNote> organizationsNoteList = new ArrayList<OrganizationNote>();
			// List<OrganizationHistory> organizationHistoryList = new
			// ArrayList<OrganizationHistory>();

			Date date = CommonUtils.getFormattedDate();
			for (Organization organization : organizationList) {
				// set notes list for multiple organization's
				if (!StringUtils.isEmpty(organization.getName())) {
					OrganizationNote note = new OrganizationNote();
					note.setName(notesMap.get(organization.getName()));
					note.setOrganizationId(organization.getId());
					note.setCreatedAt(date);
					note.setUpdatedAt(date);
					note.setCreatedBy(user.getEmail());
					note.setUpdatedBy(user.getEmail());
					organizationsNoteList.add(note);
				}

				/*
				 * // set organization history for multiple organization's
				 * OrganizationHistory orgHistory = new OrganizationHistory();
				 * orgHistory.setOrganizationId(organization.getId());
				 * orgHistory.setEntityId(organization.getId());
				 * orgHistory.setEntityName(organization.getName());
				 * orgHistory.setEntityType(OrganizationConstants.ORGANIZATION);
				 * orgHistory.setEntityCode(""); orgHistory.setUpdatedAt(date);
				 * orgHistory.setUpdatedBy(user.getUserDisplayName());
				 * orgHistory.setActionPerformed(operationPerformed);
				 * organizationHistoryList.add(orgHistory);
				 */

				// To set spi and sdg tags by naics code and ntee code
				if (null != organization.getNaicsCode() || null != organization.getNteeCode()) {
					if (operationPerformed.equals(OrganizationConstants.CREATE)) {
						setOrganizationSpiSdgMappingByNaicsCodeForBulkCreation(organization, user, naicsMap);
						setOrganizationSpiSdgMappingByNteeCodeForBulkCreation(organization, user, nteeMap);
					} else if (operationPerformed.equals(OrganizationConstants.UPDATE)) {
						setOrganizationSpiSdgMappingByNaicsCode(organization, user, naicsMap);
						setOrganizationSpiSdgMappingByNteeCode(organization, user, nteeMap);
					}
				}
			}

			// create notes for multiple organization's
			if (!organizationsNoteList.isEmpty())
				organizationsNoteList = organizationNoteService.createOrganizationsNotes(organizationsNoteList);

			/*
			 * // create history for multiple organization's if
			 * (!organizationHistoryList.isEmpty())
			 * orgHistoryService.createOrganizationHistory(
			 * organizationHistoryList);
			 */

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage(customMessage), e);
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return organizationList;
	}

	/**
	 * @param organizationPayload
	 * @param organization
	 * @param user
	 * @return
	 * @throws Exception
	 */
	private Organization setOrganizationData(OrganizationRequestPayload organizationPayload, UserPayload user)
			throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;
		Organization organization = new Organization();
		Address address = new Address();

		BeanUtils.copyProperties(organizationPayload, organization);

		if (organizationPayload.getAddress() != null) {
			address = saveAddress(organizationPayload.getAddress(), user);
			organization.setAddress(address);
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
		organization.setIsActive(true);

		Date date = CommonUtils.getFormattedDate();
		if (organization.getId() == null) {
			organization.setCreatedAt(date);
			organization.setCreatedBy(user.getEmail());
		}
		organization.setUpdatedAt(date);
		organization.setUpdatedBy(user.getEmail());
		organization.setIsActive(true);

		return organization;
	}

	/**
	 * @param organizationPayload
	 * @param organization
	 * @param user
	 * @return
	 * @throws Exception
	 */
	private Organization setOrganizationDataForBulkUpload(OrganizationRequestPayload organizationPayload,
			UserPayload user, String operationPerformed) throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;
		Organization organization = new Organization();
		Address address = new Address();

		BeanUtils.copyProperties(organizationPayload, organization);

		if (organizationPayload.getAddress() != null) {
			address = saveAddress(organizationPayload.getAddress(), user);
			organization.setAddress(address);
		}

		organization.setNaicsCode(naicsMap.get(organizationPayload.getNteeCode()));
		organization.setNteeCode(nteeMap.get(organizationPayload.getNaicsCode()));
		organization.setType(OrganizationConstants.ORGANIZATION);
		organization.setIsActive(true);

		Date date = CommonUtils.getFormattedDate();
		if (organization.getId() == null) {
			organization.setCreatedAt(date);
			organization.setCreatedBy(user.getEmail());
		}
		organization.setUpdatedAt(date);
		organization.setUpdatedBy(user.getEmail());
		organization.setIsActive(true);

		if (organizationPayload.getNaicsCode() == null && organizationPayload.getNteeCode() == null) {
			List<Long> spiTagIds = new ArrayList<>();
			List<Long> sdgTagIds = new ArrayList<>();

			if (!StringUtils.isEmpty(organizationPayload.getSpiTagIds())) {
				String[] spiIdsList = organizationPayload.getSpiTagIds().split(",");
				for (int j = 0; j < spiIdsList.length; j++) {
					spiTagIds.add(Long.parseLong(spiIdsList[j]));
				}
			}

			if (!StringUtils.isEmpty(organizationPayload.getSdgTagIds())) {
				String[] sdgIdsList = organizationPayload.getSdgTagIds().split(",");
				for (int j = 0; j < sdgIdsList.length; j++) {
					sdgTagIds.add(Long.parseLong(sdgIdsList[j]));
				}
			}

			if (operationPerformed.equals(OrganizationConstants.CREATE)) {
				// create organization's spi tags mapping for Bulk Creation
				saveOrgSpiMappingForBulkCreation(organization, user, spiDataMapObj, spiTagIds);
				// create organization's sdg tags mapping for Bulk Creation
				saveOrgSdgMappingForBulkCreation(organization, user, sdgDataMapObj, sdgTagIds);
			} else if (operationPerformed.equals(OrganizationConstants.UPDATE)) {
				// create organization's spi tags mapping
				saveOrgSpiMapping(organization, user, spiDataMapObj, spiTagIds);
				// create organization's sdg tags mapping
				saveOrgSdgMapping(organization, user, sdgDataMapObj, sdgTagIds);
			}

		}
		return organization;
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void setOrganizationSpiSdgMappingByNaicsCodeForBulkCreation(Organization organization, UserPayload user,
			Map<String, NaicsDataMappingPayload> naicsMap) throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;
		if (null != organization.getNaicsCode()) {
			NaicsDataMappingPayload naicsMapPayload = naicsMap.get(organization.getNaicsCode().getCode());
			if (naicsMapPayload != null) {
				// create organization's spi tags mapping
				saveOrgSpiMappingForBulkCreation(organization, user, spiDataMapObj, naicsMapPayload.getSpiTagIds());
				// create organization's sdg tags mapping
				saveOrgSdgMappingForBulkCreation(organization, user, sdgDataMapObj, naicsMapPayload.getSdgTagIds());
			}
		}
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void setOrganizationSpiSdgMappingByNteeCodeForBulkCreation(Organization organization, UserPayload user,
			Map<String, NteeDataMappingPayload> nteeMap) throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;

		if (null != organization.getNteeCode()) {
			NteeDataMappingPayload nteeMapPayload = nteeMap.get(organization.getNteeCode().getCode());
			if (nteeMapPayload != null) {
				// create organization's spi tags mapping
				saveOrgSpiMappingForBulkCreation(organization, user, spiDataMapObj, nteeMapPayload.getSpiTagIds());
				// create organization's sdg tags mapping
				saveOrgSdgMappingForBulkCreation(organization, user, sdgDataMapObj, nteeMapPayload.getSdgTagIds());
			}
		}
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void setOrganizationSpiSdgMappingByNaicsCode(Organization organization, UserPayload user,
			Map<String, NaicsDataMappingPayload> naicsMap) throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;
		if (null != organization.getNaicsCode()) {
			NaicsDataMappingPayload naicsMapPayload = naicsMap.get(organization.getNaicsCode().getCode());
			if (naicsMapPayload != null) {
				// create organization's spi tags mapping
				saveOrgSpiMapping(organization, user, spiDataMapObj, naicsMapPayload.getSpiTagIds());
				// create organization's sdg tags mapping
				saveOrgSdgMapping(organization, user, sdgDataMapObj, naicsMapPayload.getSdgTagIds());
			}
		}
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void setOrganizationSpiSdgMappingByNteeCode(Organization organization, UserPayload user,
			Map<String, NteeDataMappingPayload> nteeMap) throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;

		if (null != organization.getNteeCode()) {
			NteeDataMappingPayload nteeMapPayload = nteeMap.get(organization.getNteeCode().getCode());
			if (nteeMapPayload != null) {
				// create organization's spi tags mapping
				saveOrgSpiMapping(organization, user, spiDataMapObj, nteeMapPayload.getSpiTagIds());
				// create organization's sdg tags mapping
				saveOrgSdgMapping(organization, user, sdgDataMapObj, nteeMapPayload.getSdgTagIds());
			}
		}
	}

	/*	*//**
			 * @param organization
			 * @param user
			 * @param spiDataMapObj
			 * @param sdgDataMapObj
			 * @param spiIdsList
			 * @param sdgIdsList
			 * @throws Exception
			 *//*
			 * private void saveOrgSpiSdgMapping(Organization organization,
			 * UserPayload user, OrganizationSpiData spiDataMapObj,
			 * OrganizationSdgData sdgDataMapObj, List<Long> spiIdsList,
			 * List<Long> sdgIdsList) throws Exception {
			 * 
			 * @SuppressWarnings("unused") List<OrganizationSpiData>
			 * spiDataMapList = saveOrgSpiMapping(organization, user,
			 * spiDataMapObj, spiIdsList);
			 * 
			 * @SuppressWarnings("unused") List<OrganizationSdgData>
			 * sdgDataMapList = saveOrgSdgMapping(organization, user,
			 * sdgDataMapObj, sdgIdsList); }
			 */

	/**
	 * @throws Exception
	 * 
	 */
	private Map<String, NaicsDataMappingPayload> getNaicsSpiSdgMap(ExceptionResponse errorResForNaics)
			throws Exception {
		S3Object s3Object = awsS3ObjectServiceImpl.getS3Object(awsS3ObjectServiceImpl.getNaicsAwsKey());
		Map<String, NaicsDataMappingPayload> naicsMap = new HashMap<>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		InputStream input = s3Object.getObjectContent();
		String csv = IOUtils.toString(input);
		Integer rowNumber = null;
		try {
			if (null != s3Object) {
				List<NaicsMappingCsvPayload> naicsMappingCsvPayloadList = csvUtils.read(NaicsMappingCsvPayload.class,
						csv, exceptionResponse);
				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					throw new Exception(exceptionResponse.getErrorMessage());

				if (null != naicsMappingCsvPayloadList) {
					for (int i = 0; i < naicsMappingCsvPayloadList.size(); i++) {
						rowNumber = i + 2;
						NaicsMappingCsvPayload payloadData = naicsMappingCsvPayloadList.get(i);
						NaicsDataMappingPayload payload = new NaicsDataMappingPayload();

						if (!StringUtils.isEmpty(payloadData.getSpiTagIds())) {
							String[] spiIds = payloadData.getSpiTagIds().split(",");
							List<Long> spiIdsList = new ArrayList<>();
							for (int j = 0; j < spiIds.length; j++) {
								spiIdsList.add(Long.parseLong(spiIds[j]));
							}
							payload.setSpiTagIds(spiIdsList);
						}

						if (!StringUtils.isEmpty(payloadData.getSdgTagIds())) {
							String[] sdgIds = payloadData.getSdgTagIds().split(",");
							List<Long> sdgIdsList = new ArrayList<>();
							for (int j = 0; j < sdgIds.length; j++) {
								sdgIdsList.add(Long.parseLong(sdgIds[j]));
							}
							payload.setSdgTagIds(sdgIdsList);
						}
						payload.setNaicsCode(payloadData.getNaicsCode());
						naicsMap.put(payloadData.getNaicsCode(), payload);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e.toString());
			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null) {
				BeanUtils.copyProperties(exceptionResponse, errorResForNaics);
			} else {
				errorResForNaics.setErrorMessage("error occurred while fetching details of row: " + rowNumber
						+ " from the file " + awsS3ObjectServiceImpl.getNaicsAwsKey() + ", error: " + e.toString());
				errorResForNaics.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return naicsMap;
	}

	/**
	 * @throws Exception
	 * 
	 */
	private Map<String, NteeDataMappingPayload> getNteeSpiSdgMap(ExceptionResponse errorResForNtee) throws Exception {
		Map<String, NteeDataMappingPayload> nteeMap = null;
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		Integer rowNumber = null;
		try {
			nteeMap = new HashMap<>();
			S3Object s3Object = awsS3ObjectServiceImpl.getS3Object(awsS3ObjectServiceImpl.getNteeAwsKey());
			if (null != s3Object) {
				InputStream input = s3Object.getObjectContent();
				String csv = IOUtils.toString(input);
				List<NteeMappingCsvPayload> nteeMappingCsvPayloadList = csvUtils.read(NteeMappingCsvPayload.class, csv,
						exceptionResponse);
				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					throw new Exception(exceptionResponse.getErrorMessage());

				if (null != nteeMappingCsvPayloadList) {
					for (int i = 0; i < nteeMappingCsvPayloadList.size(); i++) {
						NteeMappingCsvPayload payloadData = nteeMappingCsvPayloadList.get(i);
						NteeDataMappingPayload payload = new NteeDataMappingPayload();

						if (!StringUtils.isEmpty(payloadData.getSpiTagIds())) {
							String[] spiIds = payloadData.getSpiTagIds().split(",");
							List<Long> spiIdsList = new ArrayList<>();
							for (int j = 0; j < spiIds.length; j++) {
								spiIdsList.add(Long.parseLong(spiIds[j]));
							}
							payload.setSpiTagIds(spiIdsList);
						}

						if (!StringUtils.isEmpty(payloadData.getSdgTagIds())) {
							String[] sdgIds = payloadData.getSdgTagIds().split(",");
							List<Long> sdgIdsList = new ArrayList<>();
							for (int j = 0; j < sdgIds.length; j++) {
								sdgIdsList.add(Long.parseLong(sdgIds[j]));
							}
							payload.setSdgTagIds(sdgIdsList);
						}

						payload.setNteeCode(payloadData.getNteeCode());
						nteeMap.put(payloadData.getNteeCode(), payload);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e.toString());
			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null) {
				BeanUtils.copyProperties(exceptionResponse, errorResForNtee);
			} else {
				errorResForNtee.setErrorMessage("error occurred while fetching details of row: " + rowNumber
						+ " from the file " + awsS3ObjectServiceImpl.getNteeAwsKey() + ", error: " + e.toString());
				errorResForNtee.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return nteeMap;
	}

	/**
	 * @param organization
	 * @param user
	 * @param sdgDataMapObj
	 * @param sdgIdsList
	 * @return
	 * @throws Exception
	 */
	private List<OrganizationSdgData> saveOrgSdgMappingForBulkCreation(Organization organization, UserPayload user,
			OrganizationSdgData sdgDataMapObj, List<Long> sdgIdsList) throws Exception {
		Date date = CommonUtils.getFormattedDate();
		List<OrganizationSdgData> sdgDataMapList = new ArrayList<OrganizationSdgData>();
		List<SdgData> sdgDataList = sdgDataRepository.findAllSdgData();

		if (null != sdgIdsList) {

			Map<Long, SdgData> sdgDataMap = sdgDataList.stream()
					.collect(Collectors.toMap(SdgData::getId, SdgData -> SdgData));
			for (Long sdgId : sdgIdsList) {
				SdgData sdgData = sdgDataMap.get(sdgId);
				if (null != sdgData) {
					sdgDataMapObj = new OrganizationSdgData();
					sdgDataMapObj.setSdgData(sdgData);
					sdgDataMapObj.setOrganizationId(organization.getId());
					sdgDataMapObj.setIsChecked(true);
					sdgDataMapObj.setCreatedAt(date);
					sdgDataMapObj.setUpdatedAt(date);
					sdgDataMapObj.setCreatedBy(user.getEmail());
					sdgDataMapObj.setUpdatedBy(user.getEmail());
					sdgDataMapList.add(sdgDataMapObj);
				}
			}
			if (!sdgDataMapList.isEmpty())
				sdgDataMapList = orgSdgDataMapRepository.saveAll(sdgDataMapList);

			/*
			 * orgHistoryService.createOrganizationHistory(user,
			 * sdgDataMapObj.getOrganizationId(), OrganizationConstants.CREATE,
			 * OrganizationConstants.SDG, sdgDataMapObj.getId(),
			 * sdgDataMapObj.getSdgData().getShortName(),
			 * sdgDataMapObj.getSdgData().getShortNameCode());
			 */
		}
		return sdgDataMapList;
	}

	/**
	 * @param organization
	 * @param user
	 * @param spiDataMapObj
	 * @param spiIdsList
	 * @return
	 * @throws Exception
	 */
	private List<OrganizationSpiData> saveOrgSpiMappingForBulkCreation(Organization organization, UserPayload user,
			OrganizationSpiData spiDataMapObj, List<Long> spiIdsList) throws Exception {
		Date date = CommonUtils.getFormattedDate();
		List<OrganizationSpiData> spiDataMapList = new ArrayList<OrganizationSpiData>();
		List<SpiData> spiDataList = spiDataRepository.findAllSpiData();
		if (null != spiIdsList) {
			Map<Long, SpiData> spiDataMap = spiDataList.stream()
					.collect(Collectors.toMap(SpiData::getId, SpiData -> SpiData));
			for (Long spiId : spiIdsList) {
				SpiData spiData = spiDataMap.get(spiId);
				if (null != spiData) {
					spiDataMapObj = new OrganizationSpiData();
					spiDataMapObj.setSpiData(spiData);
					spiDataMapObj.setOrganizationId(organization.getId());
					spiDataMapObj.setIsChecked(true);
					spiDataMapObj.setCreatedAt(date);
					spiDataMapObj.setUpdatedAt(date);
					spiDataMapObj.setCreatedBy(user.getEmail());
					spiDataMapObj.setUpdatedBy(user.getEmail());
					spiDataMapList.add(spiDataMapObj);
				}
			}
			if (!spiDataMapList.isEmpty())
				spiDataMapList = orgSpiDataMapRepository.saveAll(spiDataMapList);

		}

		/*
		 * orgHistoryService.createOrganizationHistory(user,
		 * spiDataMapObj.getOrganizationId(), OrganizationConstants.CREATE,
		 * OrganizationConstants.SPI, spiDataMapObj.getId(),
		 * spiDataMapObj.getSpiData().getIndicatorName(),
		 * spiDataMapObj.getSpiData().getIndicatorId());
		 */

		return spiDataMapList;
	}

	/**
	 * @param organization
	 * @param user
	 * @param sdgDataMapObj
	 * @param sdgIdsList
	 * @return
	 * @throws Exception
	 */
	private List<OrganizationSdgData> saveOrgSdgMapping(Organization organization, UserPayload user,
			OrganizationSdgData sdgDataMapObj, List<Long> sdgIdsList) throws Exception {
		List<OrganizationSdgData> sdgDataMapList = new ArrayList<OrganizationSdgData>();
		List<OrganizationSdgData> organizationSdgDataMappingList = null;
		Date date = CommonUtils.getFormattedDate();

		if (null != sdgIdsList) {
			for (Long sdgId : sdgIdsList) {
				SdgData orgSdgDataObj = sdgDataRepository.findSdgObjById(sdgId);

				if (null != orgSdgDataObj) {
					sdgDataMapObj = new OrganizationSdgData();
					sdgDataMapObj.setOrganizationId(organization.getId());
					sdgDataMapObj.setIsChecked(true);
					sdgDataMapObj.setCreatedAt(date);
					sdgDataMapObj.setUpdatedAt(date);
					sdgDataMapObj.setCreatedBy(user.getEmail());
					sdgDataMapObj.setUpdatedBy(user.getEmail());
					organizationSdgDataMappingList = orgSdgDataMapRepository
							.getAllOrgSdgMapDataByOrgId(organization.getId());

					if (!organizationSdgDataMappingList.isEmpty()) {
						Map<Long, Long> sdgIdsMap = new HashMap<Long, Long>();
						for (OrganizationSdgData organizationSdgData : organizationSdgDataMappingList) {
							if (null != organizationSdgData.getSdgData()) {
								sdgIdsMap.put(organizationSdgData.getSdgData().getId(),
										organizationSdgData.getSdgData().getId());
							}

						}
						Boolean isSdgMapFound = false;
						for (OrganizationSdgData organizationSdgData : organizationSdgDataMappingList) {
							if (null != organizationSdgData.getSdgData()) {
								if (orgSdgDataObj.getId()
										.equals(sdgIdsMap.get(organizationSdgData.getSdgData().getId()))) {
									organizationSdgData.setIsChecked(true);
									organizationSdgData.setUpdatedAt(date);
									organizationSdgData.setUpdatedBy(user.getEmail());
									organizationSdgData = orgSdgDataMapRepository.saveAndFlush(organizationSdgData);
									sdgDataMapList.add(organizationSdgData);
									isSdgMapFound = true;
									break;
								}
							}
						}
						if (!isSdgMapFound) {
							sdgDataMapObj.setSdgData(orgSdgDataObj);
							sdgDataMapObj = orgSdgDataMapRepository.saveAndFlush(sdgDataMapObj);
							sdgDataMapList.add(sdgDataMapObj);
						}

					} else {
						sdgDataMapObj.setSdgData(orgSdgDataObj);
						sdgDataMapObj = orgSdgDataMapRepository.saveAndFlush(sdgDataMapObj);
						sdgDataMapList.add(sdgDataMapObj);
					}

					/*
					 * orgHistoryService.createOrganizationHistory(user,
					 * sdgDataMapObj.getOrganizationId(),
					 * OrganizationConstants.CREATE, OrganizationConstants.SDG,
					 * sdgDataMapObj.getId(),
					 * sdgDataMapObj.getSdgData().getShortName(),
					 * sdgDataMapObj.getSdgData().getShortNameCode());
					 */
				}
			}
		}
		return sdgDataMapList;
	}

	/**
	 * @param organization
	 * @param user
	 * @param spiDataMapObj
	 * @param spiIdsList
	 * @return
	 * @throws Exception
	 */
	private List<OrganizationSpiData> saveOrgSpiMapping(Organization organization, UserPayload user,
			OrganizationSpiData spiDataMapObj, List<Long> spiIdsList) throws Exception {
		List<OrganizationSpiData> spiDataMapList = new ArrayList<OrganizationSpiData>();
		List<OrganizationSpiData> organizationSpiDataMappingList = null;
		Date date = CommonUtils.getFormattedDate();

		if (null != spiIdsList) {
			for (Long spiId : spiIdsList) {
				SpiData orgSpiDataObj = spiDataRepository.findSpiObjById(spiId);

				if (null != orgSpiDataObj) {
					spiDataMapObj = new OrganizationSpiData();
					spiDataMapObj.setOrganizationId(organization.getId());
					spiDataMapObj.setIsChecked(true);
					spiDataMapObj.setCreatedAt(date);
					spiDataMapObj.setUpdatedAt(date);
					spiDataMapObj.setCreatedBy(user.getEmail());
					spiDataMapObj.setUpdatedBy(user.getEmail());
					organizationSpiDataMappingList = orgSpiDataMapRepository
							.getAllOrgSpiMapDataByOrgId(organization.getId());

					if (!organizationSpiDataMappingList.isEmpty()) {
						Map<Long, Long> spiIdsMap = new HashMap<Long, Long>();
						for (OrganizationSpiData organizationSpiData : organizationSpiDataMappingList) {
							if (null != organizationSpiData.getSpiData()) {
								spiIdsMap.put(organizationSpiData.getSpiData().getId(),
										organizationSpiData.getSpiData().getId());
							}
						}

						Boolean isSpiMapFound = false;
						for (OrganizationSpiData organizationSpiData : organizationSpiDataMappingList) {
							if (null != organizationSpiData.getSpiData()) {
								if (orgSpiDataObj.getId()
										.equals(spiIdsMap.get(organizationSpiData.getSpiData().getId()))) {
									organizationSpiData.setIsChecked(true);
									organizationSpiData.setUpdatedAt(date);
									organizationSpiData.setUpdatedBy(user.getEmail());
									organizationSpiData = orgSpiDataMapRepository.saveAndFlush(spiDataMapObj);
									spiDataMapList.add(organizationSpiData);
									isSpiMapFound = true;
									break;
								}
							}
						}
						if (!isSpiMapFound) {
							spiDataMapObj.setSpiData(orgSpiDataObj);
							spiDataMapObj = orgSpiDataMapRepository.saveAndFlush(spiDataMapObj);
							spiDataMapList.add(spiDataMapObj);
						}
					} else {
						spiDataMapObj.setSpiData(orgSpiDataObj);
						spiDataMapObj = orgSpiDataMapRepository.saveAndFlush(spiDataMapObj);
						spiDataMapList.add(spiDataMapObj);
					}
					/*
					 * orgHistoryService.createOrganizationHistory(user,
					 * spiDataMapObj.getOrganizationId(),
					 * OrganizationConstants.CREATE, OrganizationConstants.SPI,
					 * spiDataMapObj.getId(),
					 * spiDataMapObj.getSpiData().getIndicatorName(),
					 * spiDataMapObj.getSpiData().getIndicatorId());
					 */
				}
			}
		}
		return spiDataMapList;
	}

	public Address saveAddress(AddressPayload addressPayload, UserPayload user) {
		Address address = new Address();
		try {
			if (null != addressPayload) {
				Date date = CommonUtils.getFormattedDate();
				BeanUtils.copyProperties(addressPayload, address);
				if (addressPayload.getId() == null) {
					address.setCreatedAt(date);
					address.setCreatedBy(user.getEmail());
				}
				address.setUpdatedAt(date);
				address.setUpdatedBy(user.getEmail());
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.address.created"), e);
		}
		return addressRepository.saveAndFlush(address);
	}

	public Boolean updateAddress(Organization organization, AddressPayload addressPayload, UserPayload user) {
		if (null != addressPayload && null != addressPayload.getId()) {
			Date date = CommonUtils.getFormattedDate();
			try {
				BeanUtils.copyProperties(addressPayload, organization.getAddress());
				organization.getAddress().setUpdatedAt(date);
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

	/**
	 * @param organization
	 * @param addressPayload
	 * @return
	 */
	private AddressPayload getLocationPayload(Organization organization, AddressPayload addressPayload) {
		if (null != organization.getAddress()) {
			addressPayload = new AddressPayload();
			BeanUtils.copyProperties(organization.getAddress(), addressPayload);
		}
		return addressPayload;
	}

	/**
	 * 
	 */
	private void setNaicsNteeMap() {
		List<NaicsData> naicsCodeList = naicsDataRepository.findAll();
		if (null != naicsCodeList) {
			naicsMap = new HashMap<Long, NaicsData>();
			for (NaicsData naicsData : naicsCodeList)
				naicsMap.put(naicsData.getId(), naicsData);
		}
		List<NteeData> nteeCodeList = nteeDataRepository.findAll();
		if (null != nteeCodeList) {
			nteeMap = new HashMap<Long, NteeData>();
			for (NteeData nteeData : nteeCodeList)
				nteeMap.put(nteeData.getId(), nteeData);
		}
	}

}
