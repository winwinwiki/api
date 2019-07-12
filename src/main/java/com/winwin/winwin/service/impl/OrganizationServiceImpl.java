package com.winwin.winwin.service.impl;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
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
import com.winwin.winwin.payload.OrganizationCsvPayload;
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
import com.winwin.winwin.repository.OrganizationNoteRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.repository.SpiDataRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.OrganizationService;
import com.winwin.winwin.service.SlackNotificationSenderService;
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
	@Autowired
	OrganizationNoteRepository organizationNoteRepository;
	@Autowired
	SlackNotificationSenderService slackNotificationSenderService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

	private Map<String, NaicsData> naicsMap = null;
	private Map<String, NteeData> nteeMap = null;

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
	@Async
	@Transactional
	public List<Organization> createOrganizations(List<OrganizationCsvPayload> organizationPayloadList,
			ExceptionResponse response, UserPayload user) {
		List<Organization> organizationList = saveOrganizationsForBulkUpload(organizationPayloadList, response,
				OrganizationConstants.CREATE, "org.exception.created", user);

		return organizationList;
	}

	@Override
	@Transactional
	public List<Organization> updateOrganizations(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response) {
		List<Organization> organizationList = null;
		/*
		 * saveOrganizationsForBulkUpload (organizationPayloadList, response,
		 * OrganizationConstants.UPDATE,"org.exception.updated");
		 */

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

					if (null != organization && null != organization.getParentId()) {
						orgHistoryService.createOrganizationHistory(user, organization.getParentId(),
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
	@Transactional(readOnly = true)
	@Cacheable("organization_filter_list")
	public List<Organization> getOrganizationList(OrganizationFilterPayload payload, ExceptionResponse response) {
		List<Organization> orgList = new ArrayList<Organization>();
		try {
			if (null != payload.getPageNo() && null != payload.getPageSize()) {
				orgList = organizationRepository.filterOrganization(payload, OrganizationConstants.ORGANIZATION, null,
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
	@Cacheable("organization_filter_count")
	public BigInteger getOrgCounts(OrganizationFilterPayload payload, ExceptionResponse response) {
		BigInteger noOfRecords = null;
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
	@Cacheable("organization_chart_list")
	public OrganizationChartPayload getOrgCharts(Organization organization) {
		List<Organization> childOrganizations = organizationRepository.findAllChildren(organization.getId());
		OrganizationChartPayload payload = null;
		AddressPayload location = null;
		List<OrganizationChartPayload> orgChartList = new ArrayList<>();
		for (Organization childOrg : childOrganizations)
			orgChartList.add(getOrgCharts(childOrg));

		payload = new OrganizationChartPayload();
		payload.setChildren(orgChartList);
		try {
			payload.setId(organization.getId());
			payload.setName(organization.getName());
			location = getLocationPayload(organization, location);
			payload.setLocation(location);
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

	public List<Organization> saveOrganizationsForBulkUpload(List<OrganizationCsvPayload> organizationPayloadList,
			ExceptionResponse response, String operationPerformed, String customMessage, UserPayload user) {
		ExceptionResponse errorResForNaics = new ExceptionResponse();
		ExceptionResponse errorResForNtee = new ExceptionResponse();
		List<Organization> organizationList = new ArrayList<Organization>();
		try {
			Date date = CommonUtils.getFormattedDate();
			// get NaicsCode AutoTag SpiSdgMapping
			Map<String, NaicsDataMappingPayload> naicsMapForS3 = getNaicsSpiSdgMap(errorResForNaics);
			if (!StringUtils.isEmpty(errorResForNaics.getErrorMessage())) {
				throw new Exception(errorResForNaics.getErrorMessage());
			}
			// get NteeCode AutoTag SpiSdgMapping
			Map<String, NteeDataMappingPayload> nteeMapForS3 = getNteeSpiSdgMap(errorResForNtee);
			if (!StringUtils.isEmpty(errorResForNtee.getErrorMessage())) {
				throw new Exception(errorResForNtee.getErrorMessage());
			}

			List<SpiData> spiDataList = spiDataRepository.findAllSpiData();
			Map<Long, SpiData> spiDataMap = spiDataList.stream()
					.collect(Collectors.toMap(SpiData::getId, SpiData -> SpiData));

			List<SdgData> sdgDataList = sdgDataRepository.findAllSdgData();
			Map<Long, SdgData> sdgDataMap = sdgDataList.stream()
					.collect(Collectors.toMap(SdgData::getId, SdgData -> SdgData));
			int i = 1;
			if (null != organizationPayloadList) {
				for (OrganizationCsvPayload organizationPayload : organizationPayloadList) {
					if (null != organizationPayload) {
						if (operationPerformed.equals(OrganizationConstants.CREATE)) {
							organizationPayload.setTagStatus(OrganizationConstants.AUTOTAGGED);
							organizationPayload.setPriority(OrganizationConstants.PRIORITY_NORMAL);

							// Implemented below logic to log failed and success
							// organizations for bulk upload
							try {
								Organization organization = setOrganizationDataForBulkUpload(organizationPayload, user,
										operationPerformed, naicsMapForS3, nteeMapForS3, spiDataMap, sdgDataMap);
								LOGGER.info("Saving organization : " + i);
								organization = organizationRepository.save(organization);
								LOGGER.info("Saved organization " + i + "with id as:" + organization.getId());
								organizationList.add(organization);
							} catch (Exception e) {
								Organization failedOrganization = new Organization();
								LOGGER.info("Logging failed organization for notifications: " + i);
								failedOrganization.setName(organizationPayload.getName());
								organizationList.add(failedOrganization);
							}
							i++;

						} /*
							 * else if (operationPerformed.equals(OrganizationConstants. UPDATE)) { if (null
							 * != organizationPayload.getId()) { Organization organization =
							 * organizationRepository .findOrgById(organizationPayload.getId()); if
							 * (organization == null) throw new OrganizationException(
							 * "organization with Id:" + organizationPayload.getId() +
							 * "is not found in DB to perform update operation" ); organizationList.add(
							 * setOrganizationDataForBulkUpload( organizationPayload, user,
							 * operationPerformed)); } else { throw new Exception(
							 * "Organization id is found as null in the file to perform bulk update operation for organizations"
							 * ); } }
							 */
					}
				}
			}
			// To send failed and success organization through slack
			// notification for bulk upload
			if (null != organizationList) {
				slackNotificationSenderService.sendSlackNotification(organizationList, user, date);
			}
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
	private Organization setOrganizationDataForBulkUpload(OrganizationCsvPayload csvPayload, UserPayload user,
			String operationPerformed, Map<String, NaicsDataMappingPayload> naicsMapForS3,
			Map<String, NteeDataMappingPayload> nteeMapForS3, Map<Long, SpiData> spiDataMap,
			Map<Long, SdgData> sdgDataMap) throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;

		Organization organization = new Organization();
		BeanUtils.copyProperties(csvPayload, organization);
		organization.setAddress(saveAddressForBulkUpload(csvPayload, user));

		if (!StringUtils.isEmpty(csvPayload.getNaicsCode())) {
			// set Naics-Ntee code map
			if (naicsMap == null || nteeMap == null)
				setNaicsNteeMap();
			organization.setNaicsCode(naicsMap.get(csvPayload.getNaicsCode()));
		}

		if (!StringUtils.isEmpty(csvPayload.getNteeCode())) {
			// set Naics-Ntee code map
			// set Naics-Ntee code map
			if (naicsMap == null || nteeMap == null)
				setNaicsNteeMap();
			organization.setNteeCode(nteeMap.get(csvPayload.getNteeCode()));
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

		if (!StringUtils.isEmpty(csvPayload.getNotes()))
			organization.setNote(saveOrganizationNotesForBulkUpload(csvPayload, user, organization));

		if (csvPayload.getNaicsCode() == null && csvPayload.getNteeCode() == null) {
			List<Long> spiTagIds = new ArrayList<>();
			List<Long> sdgTagIds = new ArrayList<>();

			if (!StringUtils.isEmpty(csvPayload.getSpiTagIds())) {
				// split string with comma separated values with removing leading and trailing
				// whitespace
				String[] spiIdsList = csvPayload.getSpiTagIds().split("\\S*,\\S*");
				for (int j = 0; j < spiIdsList.length; j++) {
					spiTagIds.add(Long.parseLong(spiIdsList[j]));
				}
			}
			if (!StringUtils.isEmpty(csvPayload.getSdgTagIds())) {
				// split string with comma separated values with removing leading and trailing
				// whitespace
				String[] sdgIdsList = csvPayload.getSdgTagIds().split("\\S*,\\S*");
				for (int j = 0; j < sdgIdsList.length; j++) {
					sdgTagIds.add(Long.parseLong(sdgIdsList[j]));
				}
			}
			if (operationPerformed.equals(OrganizationConstants.CREATE)) {
				// create organization's spi tags mapping for Bulk Creation
				organization.setOrganizationSpiData(
						saveOrgSpiMappingForBulkCreation(organization, user, spiTagIds, spiDataMap));
				// create organization's sdg tags mapping for Bulk Creation
				organization.setOrganizationSdgData(
						saveOrgSdgMappingForBulkCreation(organization, user, sdgTagIds, sdgDataMap));
			} else if (operationPerformed.equals(OrganizationConstants.UPDATE)) {
				// create organization's spi tags mapping
				saveOrgSpiMapping(organization, user, spiDataMapObj, spiTagIds);
				// create organization's sdg tags mapping
				saveOrgSdgMapping(organization, user, sdgDataMapObj, sdgTagIds);
			}
		}

		// To set spi and sdg tags by naics code and ntee code from s3
		if (null != organization.getNaicsCode() || null != organization.getNteeCode()) {
			if (operationPerformed.equals(OrganizationConstants.CREATE)) {
				organization = setOrganizationSpiSdgMappingForBulkCreation(organization, user, naicsMapForS3,
						nteeMapForS3, spiDataMap, sdgDataMap);
			} else if (operationPerformed.equals(OrganizationConstants.UPDATE)) {
				setOrganizationSpiSdgMappingByNaicsCode(organization, user, naicsMapForS3);
				setOrganizationSpiSdgMappingByNteeCode(organization, user, nteeMapForS3);
			}
		}

		return organization;
	}

	/**
	 * @throws Exception
	 * 
	 */
	private Organization setOrganizationSpiSdgMappingForBulkCreation(Organization organization, UserPayload user,
			Map<String, NaicsDataMappingPayload> naicsMapForS3, Map<String, NteeDataMappingPayload> nteeMapForS3,
			Map<Long, SpiData> spiDataMap, Map<Long, SdgData> sdgDataMap) throws Exception {
		List<Long> spiIdsByNaicsCode = new ArrayList<Long>();
		List<Long> sdgIdsByNaicsCode = new ArrayList<Long>();
		List<Long> spiIdsByNteeCode = new ArrayList<Long>();
		List<Long> sdgIdsByNteeCode = new ArrayList<Long>();
		List<Long> spiIds = new ArrayList<Long>();
		List<Long> sdgIds = new ArrayList<Long>();

		if (null != organization.getNaicsCode()) {
			NaicsDataMappingPayload naicsMapPayload = naicsMapForS3.get(organization.getNaicsCode().getCode());
			spiIdsByNaicsCode = naicsMapPayload.getSpiTagIds();
			sdgIdsByNaicsCode = naicsMapPayload.getSdgTagIds();
		}

		if (null != organization.getNteeCode()) {
			NteeDataMappingPayload nteeMapPayload = nteeMapForS3.get(organization.getNteeCode().getCode());
			spiIdsByNteeCode = nteeMapPayload.getSpiTagIds();
			sdgIdsByNteeCode = nteeMapPayload.getSdgTagIds();
		}

		if (null != spiIdsByNaicsCode && null != spiIdsByNteeCode)
			spiIds = Stream.of(spiIdsByNaicsCode, spiIdsByNteeCode).flatMap(x -> x.stream())
					.collect(Collectors.toList());

		if (null != sdgIdsByNaicsCode && null != sdgIdsByNteeCode)
			sdgIds = Stream.of(sdgIdsByNaicsCode, sdgIdsByNteeCode).flatMap(x -> x.stream())
					.collect(Collectors.toList());

		// create organization's spi tags mapping
		organization.setOrganizationSpiData(saveOrgSpiMappingForBulkCreation(organization, user, spiIds, spiDataMap));
		// create organization's sdg tags mapping
		organization.setOrganizationSdgData(saveOrgSdgMappingForBulkCreation(organization, user, sdgIds, sdgDataMap));
		return organization;
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

	/**
	 * @throws Exception
	 * 
	 */
	private Map<String, NaicsDataMappingPayload> getNaicsSpiSdgMap(ExceptionResponse errorResForNaics)
			throws Exception {
		S3Object s3Object = awsS3ObjectServiceImpl.getS3Object(awsS3ObjectServiceImpl.getNaicsAwsKey());
		Map<String, NaicsDataMappingPayload> naicsMapForS3 = new HashMap<>();
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
							// split string with comma separated values with removing leading and trailing
							// whitespace
							String[] spiIds = payloadData.getSpiTagIds().split("\\S*,\\S*");
							List<Long> spiIdsList = new ArrayList<>();
							for (int j = 0; j < spiIds.length; j++) {
								spiIdsList.add(Long.parseLong(spiIds[j]));
							}
							payload.setSpiTagIds(spiIdsList);
						}

						if (!StringUtils.isEmpty(payloadData.getSdgTagIds())) {
							// split string with comma separated values with removing leading and trailing
							// whitespace
							String[] sdgIds = payloadData.getSdgTagIds().split("\\S*,\\S*");
							List<Long> sdgIdsList = new ArrayList<>();
							for (int j = 0; j < sdgIds.length; j++) {
								sdgIdsList.add(Long.parseLong(sdgIds[j]));
							}
							payload.setSdgTagIds(sdgIdsList);
						}
						payload.setNaicsCode(payloadData.getNaicsCode());
						naicsMapForS3.put(payloadData.getNaicsCode(), payload);
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
		return naicsMapForS3;
	}

	/**
	 * @throws Exception
	 * 
	 */
	private Map<String, NteeDataMappingPayload> getNteeSpiSdgMap(ExceptionResponse errorResForNtee) throws Exception {
		Map<String, NteeDataMappingPayload> nteeMapForS3 = new HashMap<>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		Integer rowNumber = null;
		try {
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
							// split string with comma separated values with removing leading and trailing
							// whitespace
							String[] spiIds = payloadData.getSpiTagIds().split("\\S*,\\S*");
							List<Long> spiIdsList = new ArrayList<>();
							for (int j = 0; j < spiIds.length; j++) {
								spiIdsList.add(Long.parseLong(spiIds[j]));
							}
							payload.setSpiTagIds(spiIdsList);
						}

						if (!StringUtils.isEmpty(payloadData.getSdgTagIds())) {
							// split string with comma separated values with removing leading and trailing
							// whitespace
							String[] sdgIds = payloadData.getSdgTagIds().split("\\S*,\\S*");
							List<Long> sdgIdsList = new ArrayList<>();
							for (int j = 0; j < sdgIds.length; j++) {
								sdgIdsList.add(Long.parseLong(sdgIds[j]));
							}
							payload.setSdgTagIds(sdgIdsList);
						}

						payload.setNteeCode(payloadData.getNteeCode());
						nteeMapForS3.put(payloadData.getNteeCode(), payload);
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
		return nteeMapForS3;
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
			List<Long> sdgIdsList, Map<Long, SdgData> sdgDataMap) throws Exception {
		OrganizationSdgData sdgDataMapObj = null;
		Date date = CommonUtils.getFormattedDate();
		List<OrganizationSdgData> sdgDataMapList = new ArrayList<OrganizationSdgData>();

		if (null != sdgIdsList) {
			for (Long sdgId : sdgIdsList) {
				SdgData sdgData = sdgDataMap.get(sdgId);
				if (null != sdgData) {
					sdgDataMapObj = new OrganizationSdgData();
					sdgDataMapObj.setSdgData(sdgData);
					sdgDataMapObj.setOrganization(organization);
					sdgDataMapObj.setIsChecked(true);
					sdgDataMapObj.setCreatedAt(date);
					sdgDataMapObj.setUpdatedAt(date);
					sdgDataMapObj.setCreatedBy(user.getEmail());
					sdgDataMapObj.setUpdatedBy(user.getEmail());
					sdgDataMapList.add(sdgDataMapObj);
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
	private List<OrganizationSpiData> saveOrgSpiMappingForBulkCreation(Organization organization, UserPayload user,
			List<Long> spiIdsList, Map<Long, SpiData> spiDataMap) throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		Date date = CommonUtils.getFormattedDate();
		List<OrganizationSpiData> spiDataMapList = new ArrayList<OrganizationSpiData>();
		if (null != spiIdsList) {
			for (Long spiId : spiIdsList) {
				SpiData spiData = spiDataMap.get(spiId);
				if (null != spiData) {
					spiDataMapObj = new OrganizationSpiData();
					spiDataMapObj.setSpiData(spiData);
					spiDataMapObj.setOrganization(organization);
					spiDataMapObj.setIsChecked(true);
					spiDataMapObj.setCreatedAt(date);
					spiDataMapObj.setUpdatedAt(date);
					spiDataMapObj.setCreatedBy(user.getEmail());
					spiDataMapObj.setUpdatedBy(user.getEmail());
					spiDataMapList.add(spiDataMapObj);
				}
			}
		}

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
					sdgDataMapObj.setOrganization(organization);
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
					 * sdgDataMapObj.getOrganizationId(), OrganizationConstants.CREATE,
					 * OrganizationConstants.SDG, sdgDataMapObj.getId(),
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
					spiDataMapObj.setOrganization(organization);
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
					 * spiDataMapObj.getOrganizationId(), OrganizationConstants.CREATE,
					 * OrganizationConstants.SPI, spiDataMapObj.getId(),
					 * spiDataMapObj.getSpiData().getIndicatorName(),
					 * spiDataMapObj.getSpiData().getIndicatorId());
					 */
				}
			}
		}
		return spiDataMapList;
	}

	public Address saveAddressForBulkUpload(OrganizationCsvPayload payload, UserPayload user) {
		Address address = null;
		try {
			if (null != payload) {
				address = new Address();
				Date date = CommonUtils.getFormattedDate();
				BeanUtils.copyProperties(payload, address);
				if (payload.getAddressId() == null) {
					address.setId(null);
					address.setCreatedAt(date);
					address.setCreatedBy(user.getEmail());
				}
				address.setUpdatedAt(date);
				address.setUpdatedBy(user.getEmail());
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.address.created"), e);
		}
		return address;
	}

	private List<OrganizationNote> saveOrganizationNotesForBulkUpload(OrganizationCsvPayload payload, UserPayload user,
			Organization organization) {
		List<OrganizationNote> notes = new ArrayList<OrganizationNote>();
		OrganizationNote note = null;
		try {
			if (null != payload) {
				Date date = CommonUtils.getFormattedDate();
				// for organization notes creation
				note = new OrganizationNote();
				note.setName(payload.getNotes());
				note.setOrganization(organization);
				note.setCreatedAt(date);
				note.setUpdatedAt(date);
				note.setCreatedBy(user.getEmail());
				note.setUpdatedBy(user.getEmail());
				notes.add(note);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.address.created"), e);
		}
		return notes;

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
		if (naicsMap == null) {
			List<NaicsData> naicsCodeList = naicsDataRepository.findAll();
			if (null != naicsCodeList) {
				naicsMap = new HashMap<String, NaicsData>();
				for (NaicsData naicsData : naicsCodeList)
					naicsMap.put(naicsData.getCode(), naicsData);
			}
		}
		if (nteeMap == null) {
			List<NteeData> nteeCodeList = nteeDataRepository.findAll();
			if (null != nteeCodeList) {
				nteeMap = new HashMap<String, NteeData>();
				for (NteeData nteeData : nteeCodeList)
					nteeMap.put(nteeData.getCode(), nteeData);
			}
		}

	}

}
