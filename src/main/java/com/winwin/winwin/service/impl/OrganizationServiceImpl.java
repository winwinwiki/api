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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
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
import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.entity.Organization;
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
import com.winwin.winwin.payload.OrganizationBulkFailedPayload;
import com.winwin.winwin.payload.OrganizationBulkResultPayload;
import com.winwin.winwin.payload.OrganizationChartPayload;
import com.winwin.winwin.payload.OrganizationCsvPayload;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.payload.OrganizationHistoryPayload;
import com.winwin.winwin.payload.OrganizationRequestPayload;
import com.winwin.winwin.payload.SubOrganizationPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.NaicsDataRepository;
import com.winwin.winwin.repository.NteeDataRepository;
import com.winwin.winwin.repository.OrgSdgDataMapRepository;
import com.winwin.winwin.repository.OrgSpiDataMapRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.repository.SpiDataRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationService;
import com.winwin.winwin.service.SlackNotificationSenderService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;
import com.winwin.winwin.util.CsvUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private ProgramRepository programRepository;
	@Autowired
	private OrganizationHistoryRepository orgHistoryRepository;
	@Autowired
	private NaicsDataRepository naicsRepository;
	@Autowired
	private NteeDataRepository nteeRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationHistoryService orgHistoryService;
	@Autowired
	protected CustomMessageSource customMessageSource;
	@Autowired
	private AwsS3ObjectServiceImpl awsS3ObjectServiceImpl;
	@Autowired
	private SpiDataRepository spiDataRepository;
	@Autowired
	private OrgSpiDataMapRepository orgSpiDataMapRepository;
	@Autowired
	private SdgDataRepository sdgDataRepository;
	@Autowired
	private OrgSdgDataMapRepository orgSdgDataMapRepository;
	@Autowired
	private NteeDataRepository nteeDataRepository;
	@Autowired
	private NaicsDataRepository naicsDataRepository;
	@Autowired
	private CsvUtils csvUtils;
	@Autowired
	private SlackNotificationSenderService slackNotificationSenderService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

	private Map<String, NaicsData> naicsMap = null;
	private Map<String, NteeData> nteeMap = null;

	/**
	 * create new Organization
	 * 
	 * @param organizationPayload
	 * @param response
	 */
	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(value = "organization_chart_list"),
			@CacheEvict(value = "organization_filter_list") })
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

	/**
	 * create bulk Organizations from OrganizationCsvPayload
	 * 
	 * @param organizationPayloadList
	 * @param response
	 */
	@Override
	@Async
	@Caching(evict = { @CacheEvict(value = "organization_chart_list"),
			@CacheEvict(value = "organization_filter_list") })
	public void createOrganizations(List<OrganizationCsvPayload> organizationPayloadList, ExceptionResponse response,
			UserPayload user) {
		saveOrganizationsForBulkUpload(organizationPayloadList, response, OrganizationConstants.CREATE,
				"org.exception.created", user);
	}

	/**
	 * update bulk Organization from OrganizationRequestPayload
	 * 
	 * @param organizationPayloadList
	 * @param response
	 */
	@Override
	public void updateOrganizations(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response) {
		/*
		 * saveOrganizationsForBulkUpload (organizationPayloadList, response,
		 * OrganizationConstants.UPDATE,"org.exception.updated");
		 */
	}

	/**
	 * delete Organization by Id
	 * 
	 * @param id
	 * @param type
	 * @param response
	 */
	@Override
	@Transactional
	// @CacheEvict(value =
	// "organization_chart_list")//,organization_filter_list,organization_filter_count")
	@Caching(evict = { @CacheEvict(value = "organization_chart_list"), // ,organization_filter_list,organization_filter_count")
			@CacheEvict(value = "organization_filter_list", key = "#id") })
	public void deleteOrganization(Long id, String type, ExceptionResponse response) {
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (null != user) {
				Organization organization = organizationRepository.findOrgById(id);
				if (organization != null) {
					Date date = CommonUtils.getFormattedDate();
					organization.setIsActive(false);
					organization.setUpdatedAt(date);
					organization.setUpdatedBy(user.getUserDisplayName());
					organization.setUpdatedByEmail(user.getEmail());
					if (null != organization.getAddress()) {
						organization.getAddress().setIsActive(false);
						organization.getAddress().setUpdatedAt(date);
						organization.getAddress().setUpdatedBy(user.getUserDisplayName());
						organization.getAddress().setUpdatedByEmail(user.getEmail());
						addressRepository.saveAndFlush(organization.getAddress());
					}

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

	/**
	 * update Organization by Id
	 * 
	 * @param organizationPayload
	 * @param organization
	 * @param type
	 * @param response
	 */
	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(value = "organization_chart_list"), // ,organization_filter_list,organization_filter_count")
			@CacheEvict(value = "organization_filter_list", key = "#organization.getId()") })
	public Organization updateOrgDetails(OrganizationRequestPayload organizationPayload, Organization organization,
			String type, ExceptionResponse response) {
		if (null != organizationPayload) {
			Date date = CommonUtils.getFormattedDate();
			try {
				UserPayload user = userService.getCurrentUserDetails();
				if (null != user) {
					BeanUtils.copyProperties(organizationPayload, organization);
					if (organizationPayload.getNaicsCode() != null) {
						NaicsData naicsCode = naicsRepository.findById(organizationPayload.getNaicsCode()).orElse(null);
						organization.setNaicsCode(naicsCode);
					} else {
						organization.setNaicsCode(null);
					}
					if (organizationPayload.getNteeCode() != null) {
						NteeData nteeCode = nteeRepository.findById(organizationPayload.getNteeCode()).orElse(null);
						organization.setNteeCode(nteeCode);
					} else {
						organization.setNteeCode(null);
					}

					Boolean isUpdated = updateAddress(organization, organizationPayload.getAddress(), user);
					if (!isUpdated) {
						throw new OrganizationException(customMessageSource.getMessage("org.exception.address.null"));
					}
					organization.setIsActive(true);
					organization.setUpdatedAt(date);
					organization.setUpdatedBy(user.getUserDisplayName());
					organization.setUpdatedByEmail(user.getEmail());
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

	/**
	 * returns OrganizationFilterPayload based Organization List
	 * 
	 * @param payload
	 * @param orgId
	 * @param response
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	@CachePut(value = "organization_filter_list")
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

	/**
	 * returns OrganizationFilterPayload based Organization Count
	 * 
	 * @param payload
	 * @param orgId
	 * @param response
	 * @return
	 */
	@Override
	// @Cacheable("organization_filter_count")
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

	/**
	 * return Organization Chart for Organization
	 * 
	 * @param organization
	 * @return
	 */
	@Override
	@CachePut(value = "organization_chart_list")
	public OrganizationChartPayload getOrgCharts(Organization organization) {
		List<Organization> childOrganizations = organizationRepository.findAllChildren(organization.getId());
		AddressPayload location = null;
		List<OrganizationChartPayload> orgChartList = new ArrayList<>();
		for (Organization childOrg : childOrganizations)
			orgChartList.add(getOrgCharts(childOrg));

		OrganizationChartPayload payload = new OrganizationChartPayload();
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

	/**
	 * create new sub organization for Organization
	 * 
	 * @param payload
	 * @return
	 */
	@Override
	@Transactional
	@CacheEvict(value = "organization_chart_list")
	public Organization createSubOrganization(SubOrganizationPayload payload) {
		Organization organization = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
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
				// save suborganization parent Id
				if (null != payload.getParentId())
					organization.setParentId(payload.getParentId());

				// save suborganization root parent Id
				if (null != payload.getRootParentId())
					organization.setRootParentId(payload.getRootParentId());

				// save address for sub organization
				address = saveAddress(addressPayload, user);
				organization.setAddress(address);
				organization.setCreatedAt(date);
				organization.setUpdatedAt(date);
				organization.setCreatedBy(user.getUserDisplayName());
				organization.setUpdatedBy(user.getUserDisplayName());
				organization.setCreatedByEmail(user.getEmail());
				organization.setUpdatedByEmail(user.getEmail());
				organization = organizationRepository.saveAndFlush(organization);

				if (null != organization) {
					orgHistoryService.createOrganizationHistory(user, payload.getParentId(),
							OrganizationConstants.CREATE, OrganizationConstants.ORGANIZATION, organization.getId(),
							organization.getName(), "");
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.created"), e);
		}

		return organization;
	}

	/**
	 * return OrganizationHistory List by orgId
	 * 
	 * @param orgId
	 * @return
	 */
	@Override
	public List<OrganizationHistoryPayload> getOrgHistoryDetails(Long orgId) {
		List<OrganizationHistoryPayload> payloadList = new ArrayList<OrganizationHistoryPayload>();
		try {
			List<OrganizationHistory> orgHistoryList = orgHistoryRepository.findOrgHistoryDetails(orgId);
			if (null != orgHistoryList) {
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

	/**
	 * save Organization list for bulk upload
	 * 
	 * @param organizationPayloadList
	 * @param response
	 * @param operationPerformed
	 * @param customMessage
	 * @param user
	 */
	private void saveOrganizationsForBulkUpload(List<OrganizationCsvPayload> organizationPayloadList,
			ExceptionResponse response, String operationPerformed, String customMessage, UserPayload user) {
		ExceptionResponse errorResForNaics = new ExceptionResponse();
		ExceptionResponse errorResForNtee = new ExceptionResponse();
		List<Organization> successOrganizationList = new ArrayList<Organization>();
		List<OrganizationBulkFailedPayload> failedOrganizationList = new ArrayList<OrganizationBulkFailedPayload>();
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

			List<SpiData> spiDataList = spiDataRepository.findAllActiveSpiData();
			Map<Long, SpiData> spiDataMap = spiDataList.stream()
					.collect(Collectors.toMap(SpiData::getId, SpiData -> SpiData));

			List<SdgData> sdgDataList = sdgDataRepository.findAllActiveSdgData();
			Map<Long, SdgData> sdgDataMap = sdgDataList.stream()
					.collect(Collectors.toMap(SdgData::getId, SdgData -> SdgData));

			if (null != organizationPayloadList) {
				List<Organization> organizationsListToSaveIntoDB = new ArrayList<Organization>();
				int batchInsertSize = 1000;
				int totalOrganizationsToSave = organizationPayloadList.size();
				int remainingOrganizationsToSave = totalOrganizationsToSave % batchInsertSize;
				int numOfOrganizationsToSaveByBatchSize = (totalOrganizationsToSave - remainingOrganizationsToSave);

				int i = 1;
				for (OrganizationCsvPayload organizationPayload : organizationPayloadList) {
					if (operationPerformed.equals(OrganizationConstants.CREATE)) {
						organizationPayload.setTagStatus(OrganizationConstants.AUTOTAGGED);
						organizationPayload.setPriority(OrganizationConstants.PRIORITY_NORMAL);

						// prepare the data to save into DB
						Organization organization = setOrganizationDataForBulkUpload(organizationPayload, user,
								operationPerformed, naicsMapForS3, nteeMapForS3, spiDataMap, sdgDataMap);
						organizationsListToSaveIntoDB.add(organization);

						// save the organizations in the batches of 1000 and
						// save the remaining organizations
						if (i % 1000 == 0) {
							OrganizationBulkResultPayload payload = saveOrganizationsIntoDB(
									organizationsListToSaveIntoDB, i);
							successOrganizationList.addAll(payload.getSuccessOrganizationList());
							// refresh the data after added into list
							failedOrganizationList.addAll(payload.getFailedOrganizationList());
							// refresh the data after added into list
							organizationsListToSaveIntoDB = new ArrayList<Organization>();
							// save the remaining organizations when total size
							// is less than 1000
						} else if (i == numOfOrganizationsToSaveByBatchSize
								&& (organizationsListToSaveIntoDB.size() == remainingOrganizationsToSave)) {
							OrganizationBulkResultPayload payload = saveOrganizationsIntoDB(
									organizationsListToSaveIntoDB, i);
							successOrganizationList.addAll(payload.getSuccessOrganizationList());
							failedOrganizationList.addAll(payload.getFailedOrganizationList());
							// refresh the data after added into list
							organizationsListToSaveIntoDB = new ArrayList<Organization>();
							// save the remaining organizations when total size
							// is greater than 1000
						} else if (i > numOfOrganizationsToSaveByBatchSize
								&& (organizationsListToSaveIntoDB.size() == remainingOrganizationsToSave)) {
							OrganizationBulkResultPayload payload = saveOrganizationsIntoDB(
									organizationsListToSaveIntoDB, i);
							// if (!payload.getIsFailed()) {
							successOrganizationList.addAll(payload.getSuccessOrganizationList());
							// refresh the data after added into list
							// organizationsListToSaveIntoDB = new
							// ArrayList<Organization>();
							// } else {
							failedOrganizationList.addAll(payload.getFailedOrganizationList());
							// refresh the data after added into list
							organizationsListToSaveIntoDB = new ArrayList<Organization>();
							// }
						}

						i++;

					} // end of if (operationPerformed.
				} // end of for loop
			} // end of if (null != organizationPayloadList)

			// To send failed and success organization through slack
			// notification for bulk upload
			if (null != successOrganizationList || null != failedOrganizationList) {
				slackNotificationSenderService.sendSlackNotification(successOrganizationList, failedOrganizationList,
						user, date);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage(customMessage), e);
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * save and Flush Organizations into DB
	 * 
	 * @param organizations
	 * @param i
	 * @return
	 */
	@Transactional
	@Async
	OrganizationBulkResultPayload saveOrganizationsIntoDB(List<Organization> organizations, int i) {
		// Implemented below logic to log failed and success
		// organizations for bulk upload
		List<Organization> successOrganizationList = new ArrayList<Organization>();
		List<OrganizationBulkFailedPayload> failedOrganizationList = new ArrayList<OrganizationBulkFailedPayload>();
		// Boolean isFailed = false;
		try {
			LOGGER.info(
					"Saving organizations : " + organizations.size() + " Starting from: " + (i - organizations.size()));
			successOrganizationList.addAll(organizationRepository.saveAll(organizations));

			// Flush all pending changes to the database
			organizationRepository.flush();

			LOGGER.info("Saved organizations: " + organizations.size());
		} catch (Exception e) {
			LOGGER.info("Failed to save organizations starting from : " + (i - organizations.size()));

			// Added the below logic to save the organization one by one when
			// the bulk organization batch failed because of some faulty
			// organization and save the perfect organization into
			// successOrganizationList
			LOGGER.info("Saving Failed organizations one by one starting from : " + (i - organizations.size()));
			for (Organization failedOrganization : organizations) {
				try {
					successOrganizationList.add(organizationRepository.saveAndFlush(failedOrganization));
				} catch (Exception e1) {
					OrganizationBulkFailedPayload failedOrg = new OrganizationBulkFailedPayload();
					failedOrg.setFailedOrganization(failedOrganization);
					failedOrg.setFailedMessage(e1.getMessage());
					failedOrganizationList.add(failedOrg);
				}
			}
			// isFailed = true;
		}
		OrganizationBulkResultPayload payload = new OrganizationBulkResultPayload();
		payload.setSuccessOrganizationList(successOrganizationList);
		payload.setFailedOrganizationList(failedOrganizationList);
		// payload.setIsFailed(isFailed);
		return payload;

	}

	/**
	 * prepare Organization Data from OrganizationRequestPayload
	 * 
	 * @param organizationPayload
	 * @param organization
	 * @param user
	 * @return
	 * @throws Exception
	 */
	private Organization setOrganizationData(OrganizationRequestPayload organizationPayload, UserPayload user)
			throws Exception {
		Organization organization = new Organization();

		if (null != organizationPayload) {
			BeanUtils.copyProperties(organizationPayload, organization);
			if (organizationPayload.getAddress() != null) {
				Address address = saveAddress(organizationPayload.getAddress(), user);
				organization.setAddress(address);
			}
			if (organizationPayload.getNaicsCode() != null) {
				NaicsData naicsCode = naicsRepository.findById(organizationPayload.getNaicsCode()).orElse(null);
				organization.setNaicsCode(naicsCode);
			} else {
				organization.setNaicsCode(null);
			}
			if (organizationPayload.getNteeCode() != null) {
				NteeData nteeCode = nteeRepository.findById(organizationPayload.getNteeCode()).orElse(null);
				organization.setNteeCode(nteeCode);
			} else {
				organization.setNteeCode(null);
			}
			organization.setType(OrganizationConstants.ORGANIZATION);
			organization.setIsActive(true);
			Date date = CommonUtils.getFormattedDate();
			if (organization.getId() == null) {
				organization.setCreatedAt(date);
				organization.setCreatedBy(user.getUserDisplayName());
				organization.setCreatedByEmail(user.getEmail());
			}
			organization.setUpdatedAt(date);
			organization.setUpdatedBy(user.getUserDisplayName());
			organization.setUpdatedByEmail(user.getEmail());
			organization.setIsActive(true);
		}
		return organization;
	}

	/**
	 * prepare Organization Data from OrganizationCsvPayload
	 * 
	 * @param csvPayload
	 * @param user
	 * @param operationPerformed
	 * @param naicsMapForS3
	 * @param nteeMapForS3
	 * @param spiDataMap
	 * @param sdgDataMap
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
			organization.setCreatedBy(user.getUserDisplayName());
			organization.setCreatedByEmail(user.getEmail());
		}
		organization.setUpdatedAt(date);
		organization.setUpdatedBy(user.getUserDisplayName());
		organization.setUpdatedByEmail(user.getEmail());
		organization.setIsActive(true);

		if (!StringUtils.isEmpty(csvPayload.getNotes()))
			organization.setNote(saveOrganizationNotesForBulkUpload(csvPayload, user, organization));

		if (csvPayload.getNaicsCode() == null && csvPayload.getNteeCode() == null) {
			List<Long> spiTagIds = new ArrayList<>();
			List<Long> sdgTagIds = new ArrayList<>();

			if (!StringUtils.isEmpty(csvPayload.getSpiTagIds())) {
				// split string with comma separated values with removing
				// leading and trailing
				// whitespace
				String[] spiIdsList = csvPayload.getSpiTagIds().split(",");
				for (int j = 0; j < spiIdsList.length; j++) {
					if (!StringUtils.isEmpty(spiIdsList[j]))
						spiTagIds.add(Long.parseLong(spiIdsList[j].trim()));
				}
			}
			if (!StringUtils.isEmpty(csvPayload.getSdgTagIds())) {
				// split string with comma separated values with removing
				// leading and trailing
				// whitespace
				String[] sdgIdsList = csvPayload.getSdgTagIds().split(",");
				for (int j = 0; j < sdgIdsList.length; j++) {
					if (!StringUtils.isEmpty(sdgIdsList[j]))
						sdgTagIds.add(Long.parseLong(sdgIdsList[j].trim()));
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
	 * return Organization Object by setting OrganizationSpiData,
	 * OrganizationSdgData
	 * 
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
							// split string with comma separated values with
							// removing leading and trailing
							// whitespace
							String[] spiIds = payloadData.getSpiTagIds().split(",");
							List<Long> spiIdsList = new ArrayList<>();
							for (int j = 0; j < spiIds.length; j++) {
								if (!StringUtils.isEmpty(spiIds[j]))
									spiIdsList.add(Long.parseLong(spiIds[j].trim()));
							}
							payload.setSpiTagIds(spiIdsList);
						}

						if (!StringUtils.isEmpty(payloadData.getSdgTagIds())) {
							// split string with comma separated values with
							// removing leading and trailing
							// whitespace
							String[] sdgIds = payloadData.getSdgTagIds().split(",");
							List<Long> sdgIdsList = new ArrayList<>();
							for (int j = 0; j < sdgIds.length; j++) {
								if (!StringUtils.isEmpty(sdgIds[j]))
									sdgIdsList.add(Long.parseLong(sdgIds[j].trim()));
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
							// split string with comma separated values with
							// removing leading and trailing
							// whitespace
							String[] spiIds = payloadData.getSpiTagIds().split(",");
							List<Long> spiIdsList = new ArrayList<>();
							for (int j = 0; j < spiIds.length; j++) {
								if (!StringUtils.isEmpty(spiIds[j]))
									spiIdsList.add(Long.parseLong(spiIds[j].trim()));
							}
							payload.setSpiTagIds(spiIdsList);
						}

						if (!StringUtils.isEmpty(payloadData.getSdgTagIds())) {
							// split string with comma separated values with
							// removing leading and trailing
							// whitespace
							String[] sdgIds = payloadData.getSdgTagIds().split(",");
							List<Long> sdgIdsList = new ArrayList<>();
							for (int j = 0; j < sdgIds.length; j++) {
								if (!StringUtils.isEmpty(sdgIds[j]))
									sdgIdsList.add(Long.parseLong(sdgIds[j].trim()));
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
					sdgDataMapObj.setCreatedBy(user.getUserDisplayName());
					sdgDataMapObj.setUpdatedBy(user.getUserDisplayName());
					sdgDataMapObj.setCreatedByEmail(user.getEmail());
					sdgDataMapObj.setUpdatedByEmail(user.getEmail());
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
					spiDataMapObj.setCreatedBy(user.getUserDisplayName());
					spiDataMapObj.setUpdatedBy(user.getUserDisplayName());
					spiDataMapObj.setCreatedByEmail(user.getEmail());
					spiDataMapObj.setUpdatedByEmail(user.getEmail());
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
					sdgDataMapObj.setCreatedBy(user.getUserDisplayName());
					sdgDataMapObj.setUpdatedBy(user.getUserDisplayName());
					sdgDataMapObj.setCreatedByEmail(user.getEmail());
					sdgDataMapObj.setUpdatedByEmail(user.getEmail());
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
									organizationSdgData.setUpdatedBy(user.getUserDisplayName());
									organizationSdgData.setUpdatedByEmail(user.getEmail());
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
					spiDataMapObj.setOrganization(organization);
					spiDataMapObj.setIsChecked(true);
					spiDataMapObj.setCreatedAt(date);
					spiDataMapObj.setUpdatedAt(date);
					spiDataMapObj.setCreatedBy(user.getUserDisplayName());
					spiDataMapObj.setUpdatedBy(user.getUserDisplayName());
					spiDataMapObj.setCreatedByEmail(user.getEmail());
					spiDataMapObj.setUpdatedByEmail(user.getEmail());
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
									organizationSpiData.setUpdatedBy(user.getUserDisplayName());
									organizationSpiData.setUpdatedByEmail(user.getEmail());
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

	private Address saveAddressForBulkUpload(OrganizationCsvPayload payload, UserPayload user) {
		Address address = null;
		try {
			if (null != payload) {
				address = new Address();
				Date date = CommonUtils.getFormattedDate();
				BeanUtils.copyProperties(payload, address);
				if (payload.getAddressId() == null) {
					address.setId(null);
					address.setCreatedAt(date);
					address.setCreatedBy(user.getUserDisplayName());
					address.setCreatedByEmail(user.getEmail());

				}
				address.setUpdatedAt(date);
				address.setUpdatedBy(user.getUserDisplayName());
				address.setUpdatedByEmail(user.getEmail());
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
				note.setCreatedBy(user.getUserDisplayName());
				note.setUpdatedBy(user.getUserDisplayName());
				note.setCreatedByEmail(user.getEmail());
				note.setUpdatedByEmail(user.getEmail());
				notes.add(note);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.address.created"), e);
		}
		return notes;

	}

	private Address saveAddress(AddressPayload addressPayload, UserPayload user) {
		Address address = new Address();
		try {
			if (null != addressPayload) {
				Date date = CommonUtils.getFormattedDate();
				BeanUtils.copyProperties(addressPayload, address);
				if (addressPayload.getId() == null) {
					address.setCreatedAt(date);
					address.setCreatedBy(user.getUserDisplayName());
					address.setCreatedByEmail(user.getEmail());
				}
				address.setUpdatedAt(date);
				address.setUpdatedBy(user.getUserDisplayName());
				address.setUpdatedByEmail(user.getEmail());
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
				if (null != organization.getAddress()) {
					BeanUtils.copyProperties(addressPayload, organization.getAddress());
					organization.getAddress().setUpdatedAt(date);
					organization.getAddress().setUpdatedBy(user.getUserDisplayName());
					organization.getAddress().setUpdatedByEmail(user.getEmail());
					return true;
				}

			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.exception.address.updated"), e);
			}
		}
		return false;
	}

	/**
	 * @param organization
	 * @param addressPayload
	 * @return
	 */
	private AddressPayload getLocationPayload(Organization organization, AddressPayload addressPayload) {
		if (null != organization.getAddress()) {
			addressPayload = new AddressPayload();
			if (null != organization.getAddress())
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
