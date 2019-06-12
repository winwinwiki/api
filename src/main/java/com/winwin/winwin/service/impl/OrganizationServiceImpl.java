package com.winwin.winwin.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.winwin.winwin.payload.OrganizationNotePayload;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

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
					/*
					 * organization.setAdminUrl(OrganizationConstants.BASE_URL +
					 * OrganizationConstants.ORGANIZATIONS + "/" +
					 * organization.getId()); organization =
					 * organizationRepository.saveAndFlush(organization);
					 */

					orgHistoryService.createOrganizationHistory(user, organization.getId(),
							OrganizationConstants.CREATE, OrganizationConstants.ORGANIZATION, organization.getId(),
							organization.getName());
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
								OrganizationConstants.DELETE, type, organization.getId(), organization.getName());
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
					/*
					 * if (orgClassificationMapping == null) { throw new
					 * OrganizationException(
					 * "Request to update classification is invalid"); }
					 */
					organization = organizationRepository.saveAndFlush(organization);

					if (null != organization) {
						orgHistoryService.createOrganizationHistory(user, organization.getId(),
								OrganizationConstants.UPDATE, type, organization.getId(), organization.getName());
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
			payload.setAdminUrl(organization.getAdminUrl());
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.chart.error.list"), e);
		}
		return payload;
	}

	@Override
	@Transactional
	public Organization createSubOrganization(SubOrganizationPayload payload) {
		UserPayload user = userService.getCurrentUserDetails();
		Organization organization = null;
		try {
			if (null != payload && null != user) {
				Date date = CommonUtils.getFormattedDate();
				Address address = new Address();
				AddressPayload addressPayload = new AddressPayload();
				addressPayload.setCountry("");
				organization = new Organization();

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

				organization.setCreatedAt(date);
				organization.setUpdatedAt(date);
				organization.setCreatedBy(user.getEmail());
				organization.setUpdatedBy(user.getEmail());
				organization.setAdminUrl(payload.getAdminUrl());

				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization) {
					orgHistoryService.createOrganizationHistory(user, organization.getId(),
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
			for (OrganizationRequestPayload organizationPayload : organizationPayloadList) {
				if (null != organizationPayload && null != user) {
					if (!StringUtils.isEmpty(organizationPayload.getNotes())) {
						if (null != organizationPayload.getName())
							notesMap.put(organizationPayload.getName(), organizationPayload.getNotes());
					}
					if (operationPerformed.equals(OrganizationConstants.CREATE)) {
						organizationPayload.setTagStatus(OrganizationConstants.AUTOTAGGED);
						organizationList.add(setOrganizationData(organizationPayload, user));
					} else if (operationPerformed.equals(OrganizationConstants.UPDATE)) {
						if (null != organizationPayload.getId()) {
							if (null != organizationPayload.getId()) {
								Organization organization = organizationRepository
										.findOrgById(organizationPayload.getId());
								if (organization == null)
									throw new OrganizationException(
											"organization with Id:" + organizationPayload.getId()
													+ "is not found in DB to perform update operation");
							}
							organizationList.add(setOrganizationData(organizationPayload, user));
						} else {
							throw new Exception(
									"Organization id is found as null in the file to perform bulk update operation for organizations");
						}
					}
				}
			}
			organizationList = organizationRepository.saveAll(organizationList);

			for (Organization organization : organizationList) {
				/*
				 * organization.setAdminUrl(OrganizationConstants.BASE_URL +
				 * OrganizationConstants.ORGANIZATIONS + "/" +
				 * organization.getId()); organization =
				 * organizationRepository.saveAndFlush(organization);
				 */
				if (null != organization.getName()) {
					String notes = notesMap.get(organization.getName());
					OrganizationNotePayload organizationNotePayload = new OrganizationNotePayload();
					Date date = CommonUtils.getFormattedDate();
					organizationNotePayload.setName(notes);
					organizationNotePayload.setOrganizationId(organization.getId());
					organizationNotePayload.setCreatedAt(date);
					organizationNotePayload.setCreatedBy(user.getEmail());
					OrganizationNote note = organizationNoteService.createOrganizationNote(organizationNotePayload);
				}
				if (null != organization.getNaicsCode() || null != organization.getNteeCode()) {
					// To set spi and sdg tags by naics code
					setSpiSdgMapByNaicsCode(organization, user, naicsMap);
					// To set spi and sdg tags by ntee code
					setSpiSdgMapByNteeCode(organization, user, nteeMap);
				}
				orgHistoryService.createOrganizationHistory(user, organization.getId(), operationPerformed,
						OrganizationConstants.ORGANIZATION, organization.getId(), organization.getName());
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
		;

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
			saveOrgSpiSdgMapping(organization, user, spiDataMapObj, sdgDataMapObj, spiTagIds, sdgTagIds);

		}
		return organization;
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void setSpiSdgMapByNaicsCode(Organization organization, UserPayload user,
			Map<String, NaicsDataMappingPayload> naicsMap) throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;

		NaicsDataMappingPayload naicsMapPayload = naicsMap.get(organization.getNaicsCode().getCode());

		if (naicsMapPayload != null) {
			List<Long> spiIdsList = naicsMapPayload.getSpiTagIds();
			List<Long> sdgIdsList = naicsMapPayload.getSdgTagIds();

			saveOrgSpiSdgMapping(organization, user, spiDataMapObj, sdgDataMapObj, spiIdsList, sdgIdsList);
		}

	}

	/**
	 * @throws Exception
	 * 
	 */
	private void setSpiSdgMapByNteeCode(Organization organization, UserPayload user,
			Map<String, NteeDataMappingPayload> nteeMap) throws Exception {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;

		NteeDataMappingPayload nteeMapPayload = nteeMap.get(organization.getNteeCode().getCode());

		if (nteeMapPayload != null) {
			List<Long> spiIdsList = nteeMapPayload.getSpiTagIds();
			List<Long> sdgIdsList = nteeMapPayload.getSdgTagIds();

			saveOrgSpiSdgMapping(organization, user, spiDataMapObj, sdgDataMapObj, spiIdsList, sdgIdsList);
		}

	}

	/**
	 * @param organization
	 * @param user
	 * @param spiDataMapObj
	 * @param sdgDataMapObj
	 * @param spiIdsList
	 * @param sdgIdsList
	 * @throws Exception
	 */
	private void saveOrgSpiSdgMapping(Organization organization, UserPayload user, OrganizationSpiData spiDataMapObj,
			OrganizationSdgData sdgDataMapObj, List<Long> spiIdsList, List<Long> sdgIdsList) throws Exception {
		@SuppressWarnings("unused")
		List<OrganizationSpiData> spiDataMapList = saveOrgSpiMapping(organization, user, spiDataMapObj, spiIdsList);

		@SuppressWarnings("unused")
		List<OrganizationSdgData> sdgDataMapList = saveOrgSdgMapping(organization, user, sdgDataMapObj, sdgIdsList);
	}

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
				List<NaicsMappingCsvPayload> naicsMappingCsvPayloadList = CsvUtils.read(NaicsMappingCsvPayload.class,
						csv, exceptionResponse);
				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					throw new Exception(exceptionResponse.getErrorMessage());

				if (null != naicsMappingCsvPayloadList) {
					for (int i = 0; i < naicsMappingCsvPayloadList.size(); i++) {
						rowNumber = i + 2;
						;
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
				List<NteeMappingCsvPayload> nteeMappingCsvPayloadList = CsvUtils.read(NteeMappingCsvPayload.class, csv,
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
					sdgDataMapObj.setAdminUrl("");

					organizationSdgDataMappingList = orgSdgDataMapRepository
							.getOrgSdgMapDataByOrgId(organization.getId());
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
									sdgDataMapObj.setId(organizationSdgData.getId());
									sdgDataMapObj.setSdgData(organizationSdgData.getSdgData());
									isSdgMapFound = true;
									break;
								} else {
									sdgDataMapObj.setSdgData(orgSdgDataObj);
								}
							}
						}
						if (!isSdgMapFound) {
							sdgDataMapObj.setSdgData(orgSdgDataObj);
						}

					} else {
						sdgDataMapObj.setSdgData(orgSdgDataObj);
					}

					sdgDataMapObj = orgSdgDataMapRepository.saveAndFlush(sdgDataMapObj);
					sdgDataMapList.add(sdgDataMapObj);

					orgHistoryService.createOrganizationHistory(user, sdgDataMapObj.getOrganizationId(),
							OrganizationConstants.CREATE, OrganizationConstants.SDG, sdgDataMapObj.getId(),
							sdgDataMapObj.getSdgData().getShortName());
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
					spiDataMapObj.setAdminUrl("");

					organizationSpiDataMappingList = orgSpiDataMapRepository
							.getOrgSpiMapDataByOrgId(organization.getId());

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
									spiDataMapObj.setId(organizationSpiData.getId());
									spiDataMapObj.setSpiData(organizationSpiData.getSpiData());
									isSpiMapFound = true;
									break;
								}
							}
						}
						if (!isSpiMapFound) {
							spiDataMapObj.setSpiData(orgSpiDataObj);
						}
					} else {
						spiDataMapObj.setSpiData(orgSpiDataObj);
					}

					spiDataMapObj = orgSpiDataMapRepository.saveAndFlush(spiDataMapObj);
					spiDataMapList.add(spiDataMapObj);

					orgHistoryService.createOrganizationHistory(user, spiDataMapObj.getOrganizationId(),
							OrganizationConstants.CREATE, OrganizationConstants.SPI, spiDataMapObj.getId(),
							spiDataMapObj.getSpiData().getIndicatorName());
				}
			}
		}
		return spiDataMapList;
	}

	@Transactional
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

}
