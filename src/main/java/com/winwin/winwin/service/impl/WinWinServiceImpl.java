/**
 * 
 */
package com.winwin.winwin.service.impl;

import java.io.InputStream;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.entity.OrganizationRegionServed;
import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.OrganizationSdgData;
import com.winwin.winwin.entity.OrganizationSpiData;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.ProgramDataSet;
import com.winwin.winwin.entity.ProgramRegionServed;
import com.winwin.winwin.entity.ProgramResource;
import com.winwin.winwin.entity.ProgramSdgData;
import com.winwin.winwin.entity.ProgramSpiData;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.entity.SdgData;
import com.winwin.winwin.entity.SlackMessage;
import com.winwin.winwin.entity.SpiData;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.OrganizationDataMigrationCsvPayload;
import com.winwin.winwin.payload.ProgramBulkFailedPayload;
import com.winwin.winwin.payload.NaicsDataMappingPayload;
import com.winwin.winwin.payload.NaicsMappingCsvPayload;
import com.winwin.winwin.payload.NteeDataMappingPayload;
import com.winwin.winwin.payload.NteeMappingCsvPayload;
import com.winwin.winwin.payload.OrganizationBulkFailedPayload;
import com.winwin.winwin.payload.OrganizationBulkResultPayload;
import com.winwin.winwin.payload.ProgramBulkResultPayload;
import com.winwin.winwin.payload.ProgramDataMigrationCsvPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.DataSetCategoryRepository;
import com.winwin.winwin.repository.NaicsDataRepository;
import com.winwin.winwin.repository.NteeDataRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.repository.RegionMasterRepository;
import com.winwin.winwin.repository.ResourceCategoryRepository;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.repository.SpiDataRepository;
import com.winwin.winwin.service.SlackNotificationSenderService;
import com.winwin.winwin.service.WinWinService;
import com.winwin.winwin.util.CommonUtils;
import com.winwin.winwin.util.CsvUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class WinWinServiceImpl implements WinWinService {
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private ProgramRepository programRepository;
	@Autowired
	private CustomMessageSource customMessageSource;
	@Autowired
	private AwsS3ObjectServiceImpl awsS3ObjectServiceImpl;
	@Autowired
	private CsvUtils csvUtils;
	@Autowired
	private SpiDataRepository spiDataRepository;
	@Autowired
	private SdgDataRepository sdgDataRepository;
	@Autowired
	private ResourceCategoryRepository resourceCategoryRepository;
	@Autowired
	private DataSetCategoryRepository dataSetCategoryRepository;
	@Autowired
	private RegionMasterRepository regionMasterRepository;
	@Autowired
	private NteeDataRepository nteeDataRepository;
	@Autowired
	private NaicsDataRepository naicsDataRepository;
	@Autowired
	private SlackNotificationSenderService slackNotificationSenderService;

	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinServiceImpl.class);

	private Map<String, NaicsData> naicsMap = null;
	private Map<String, NteeData> nteeMap = null;

	@Value("${slack.channel}")
	private String slackChannelName;

	/**
	 * create organizations in bulk, call this method only when the organization
	 * id's are already imported into organization table. This is the utility
	 * method for data migration and can only be used for new environment setup
	 * 
	 * @param organizationPayloadList
	 * @param response
	 * @param user
	 * @return
	 */
	@Override
	@Async
	public void createOrganizationsOffline(List<OrganizationDataMigrationCsvPayload> organizationPayloadList,
			ExceptionResponse response, UserPayload user) {
		// for Slack Notification
		Date date = CommonUtils.getFormattedDate();
		SlackMessage slackMessage = SlackMessage.builder().username("WinWinMessageNotifier")
				.text("WinWinWiki Organization Data Migration Process has been started successfully for app env: "
						+ System.getenv("WINWIN_ENV") + " , initiated by user: " + user.getUserDisplayName() + " at "
						+ date)
				.channel(slackChannelName).as_user("true").build();
		slackNotificationSenderService.sendSlackMessageNotification(slackMessage);

		saveOrganizationsOfflineForBulkUpload(organizationPayloadList, response, OrganizationConstants.CREATE,
				"org.exception.created", user);

		date = CommonUtils.getFormattedDate();
		slackMessage.setText(("WinWinWiki Organization Data Migration Process has been ended successfully for app env: "
				+ System.getenv("WINWIN_ENV") + " , initiated by user: " + user.getUserDisplayName() + " at " + date));
		slackNotificationSenderService.sendSlackMessageNotification(slackMessage);
	}

	/**
	 * create programs in bulk, call this method only when the program id's are
	 * already imported into program table. This is the utility method for data
	 * migration and can only be used for new environment setup
	 * 
	 * @param programPayloadList
	 * @param response
	 * @param user
	 * @return
	 */
	@Override
	@Async
	public void createProgramsOffline(List<ProgramDataMigrationCsvPayload> programPayloadList,
			ExceptionResponse response, UserPayload user) {
		// for Slack Notification
		Date date = CommonUtils.getFormattedDate();
		SlackMessage slackMessage = SlackMessage.builder().username("WinWinMessageNotifier")
				.text("WinWinWiki Program Data Migration Process has been started successfully for app env: "
						+ System.getenv("WINWIN_ENV") + " , initiated by user: " + user.getUserDisplayName() + " at "
						+ date)
				.channel(slackChannelName).as_user("true").build();
		slackNotificationSenderService.sendSlackMessageNotification(slackMessage);

		saveProgramOfflineForBulkUpload(programPayloadList, response, OrganizationConstants.CREATE,
				"prg.exception.created", user);

		date = CommonUtils.getFormattedDate();
		slackMessage.setText(("WinWinWiki Program Data Migration Process has been ended successfully for app env: "
				+ System.getenv("WINWIN_ENV") + " , initiated by user: " + user.getUserDisplayName() + " at " + date));
		slackNotificationSenderService.sendSlackMessageNotification(slackMessage);
	}

	/**
	 * save organizations in bulk
	 * 
	 * @param organizationPayloadList
	 * @param response
	 * @param operationPerformed
	 * @param customMessage
	 * @param user
	 * @return
	 */
	public void saveOrganizationsOfflineForBulkUpload(List<OrganizationDataMigrationCsvPayload> organizationPayloadList,
			ExceptionResponse response, String operationPerformed, String customMessage, UserPayload user) {
		ExceptionResponse errorResForNaics = new ExceptionResponse();
		ExceptionResponse errorResForNtee = new ExceptionResponse();
		try {
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
				for (OrganizationDataMigrationCsvPayload organizationPayload : organizationPayloadList) {
					if (null != organizationPayload && null != organizationPayload.getId()) {
						if (operationPerformed.equals(OrganizationConstants.CREATE)) {
							// set Organization Object into list to save into DB
							organizationsListToSaveIntoDB.add(setOrganizationDataForBulkUpload(organizationPayload,
									user, operationPerformed, naicsMapForS3, nteeMapForS3, spiDataMap, sdgDataMap));

							// save the organizations in the batches of 1000 and
							// save the remaining organizations
							if (i % 1000 == 0) {
								saveOrganizationsIntoDB(organizationsListToSaveIntoDB, i);
								// refresh the data after added into list
								organizationsListToSaveIntoDB = new ArrayList<Organization>();
								// refresh the data after added into list
								organizationsListToSaveIntoDB = new ArrayList<Organization>();
								// save the remaining organizations when total
								// size is less than 1000
							} else if (numOfOrganizationsToSaveByBatchSize == 0
									&& (organizationsListToSaveIntoDB.size() == remainingOrganizationsToSave)) {
								saveOrganizationsIntoDB(organizationsListToSaveIntoDB, i);
								// refresh the data after added into list
								organizationsListToSaveIntoDB = new ArrayList<Organization>();
								// refresh the data after added into list
								organizationsListToSaveIntoDB = new ArrayList<Organization>();
								// save the remaining organizations when total
								// size is greater than 1000
							} else if (i > numOfOrganizationsToSaveByBatchSize
									&& (organizationsListToSaveIntoDB.size() == remainingOrganizationsToSave)) {
								saveOrganizationsIntoDB(organizationsListToSaveIntoDB, i);
								// refresh the data after added into list
								organizationsListToSaveIntoDB = new ArrayList<Organization>();
								// refresh the data after added into list
								organizationsListToSaveIntoDB = new ArrayList<Organization>();
							}
							i++;
						}
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage(customMessage), e);
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			// for Slack Notification
			Date date = CommonUtils.getFormattedDate();
			SlackMessage slackMessage = SlackMessage.builder().username("WinWinMessageNotifier")
					.text(("WinWinWiki Organization Data Migration Process has failed to run for app env: "
							+ System.getenv("WINWIN_ENV") + " , initiated by user: " + user.getUserDisplayName()
							+ " at " + date + " due to error: \n" + e.getMessage()))
					.channel(slackChannelName).as_user("true").build();
			slackNotificationSenderService.sendSlackMessageNotification(slackMessage);
		}
	}

	/**
	 * prepare data for Organization to save into DB
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
	private Organization setOrganizationDataForBulkUpload(OrganizationDataMigrationCsvPayload csvPayload,
			UserPayload user, String operationPerformed, Map<String, NaicsDataMappingPayload> naicsMapForS3,
			Map<String, NteeDataMappingPayload> nteeMapForS3, Map<Long, SpiData> spiDataMap,
			Map<Long, SdgData> sdgDataMap) throws Exception {

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
			if (naicsMap == null || nteeMap == null)
				setNaicsNteeMap();
			organization.setNteeCode(nteeMap.get(csvPayload.getNteeCode()));

		}

		if (organization.getParentId() == null || organization.getRootParentId() == null)
			organization.setType(OrganizationConstants.ORGANIZATION);
		else
			organization.setType(OrganizationConstants.SUBORGANIZATION);

		organization.setPriority(OrganizationConstants.PRIORITY_NORMAL);
		organization.setIsActive(true);

		Date date = CommonUtils.getFormattedDate();
		organization.setCreatedAt(date);
		organization.setCreatedBy(user.getUserDisplayName());
		organization.setCreatedByEmail(user.getEmail());
		organization.setUpdatedAt(date);
		organization.setUpdatedBy(user.getUserDisplayName());
		organization.setUpdatedByEmail(user.getEmail());
		organization.setIsActive(true);

		// set sector for organization as null to apply sorting NULLS LAST
		if (StringUtils.isEmpty(csvPayload.getSector()))
			organization.setSector(null);

		// set note for organization
		if (!StringUtils.isEmpty(csvPayload.getNotes()))
			organization.setNote(saveOrganizationNotesForBulkUpload(csvPayload, user, organization));

		// set all the regionServed for organization
		if (!StringUtils.isEmpty(csvPayload.getRegionServedIds()))
			organization.setOrganizationRegionServed(
					saveOrganizationRegionServedForBulkUpload(csvPayload, user, organization));

		// set all the resources for organization
		if (!StringUtils.isEmpty(csvPayload.getResourceIds()))
			organization
					.setOrganizationResource(saveOrganizationResourcesForBulkUpload(csvPayload, user, organization));

		// set all the dataset for organization
		if (!StringUtils.isEmpty(csvPayload.getDatasetIds()))
			organization.setOrganizationDataSet(saveOrganizationDataSetsForBulkUpload(csvPayload, user, organization));

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
		}

		return organization;
	}// end of method

	/**
	 * save and Flush Organizations into DB
	 * 
	 * @param organizations
	 * @param i
	 * @return
	 */
	//@Transactional
	@Async
	private OrganizationBulkResultPayload saveOrganizationsIntoDB(List<Organization> organizations, int i) {
		// Implemented below logic to log failed and success
		// organizations for bulk upload
		List<Organization> successOrganizationList = new ArrayList<Organization>();
		List<OrganizationBulkFailedPayload> failedOrganizationList = new ArrayList<OrganizationBulkFailedPayload>();
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
		}
		OrganizationBulkResultPayload payload = new OrganizationBulkResultPayload();
		payload.setSuccessOrganizationList(successOrganizationList);
		payload.setFailedOrganizationList(failedOrganizationList);
		return payload;

	}

	/**
	 * @throws Exception
	 * 
	 */
	@SuppressWarnings("unused")
	private Organization setOrganizationSpiSdgMappingForBulkCreation(Organization organization, UserPayload user,
			Map<String, NaicsDataMappingPayload> naicsMapForS3, Map<String, NteeDataMappingPayload> nteeMapForS3,
			Map<Long, SpiData> spiDataMap, Map<Long, SdgData> sdgDataMap) throws Exception {
		List<Long> spiIdsByNaicsCode = new ArrayList<>();
		List<Long> sdgIdsByNaicsCode = new ArrayList<>();
		List<Long> spiIdsByNteeCode = new ArrayList<>();
		List<Long> sdgIdsByNteeCode = new ArrayList<>();
		List<Long> spiIds = new ArrayList<>();
		List<Long> sdgIds = new ArrayList<>();

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
	 * prepare OrganizationNote for Organization
	 * 
	 * @param payload
	 * @param user
	 * @param organization
	 * @return
	 */
	private List<OrganizationNote> saveOrganizationNotesForBulkUpload(OrganizationDataMigrationCsvPayload payload,
			UserPayload user, Organization organization) {
		List<OrganizationNote> notes = new ArrayList<>();
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

	/**
	 * prepare OrganizationRegionServed for Organization
	 * 
	 * @param payload
	 * @param user
	 * @param organization
	 * @return
	 */
	private List<OrganizationRegionServed> saveOrganizationRegionServedForBulkUpload(
			OrganizationDataMigrationCsvPayload payload, UserPayload user, Organization organization) {
		List<Long> regionIds = new ArrayList<>();
		List<OrganizationRegionServed> orgRegionServedList = new ArrayList<>();
		try {
			if (null != payload) {
				// split string with comma separated values with removing
				// leading and trailing
				// whitespace
				String[] regionMasterIdsList = payload.getRegionServedIds().split(",");
				for (int j = 0; j < regionMasterIdsList.length; j++) {
					if (!StringUtils.isEmpty(regionMasterIdsList[j]))
						regionIds.add(Long.parseLong(regionMasterIdsList[j].trim()));
				}
				Date date = CommonUtils.getFormattedDate();

				if (null != regionIds) {
					for (Long regionId : regionIds) {
						// for organization regionServed creation
						OrganizationRegionServed orgRegionServed = new OrganizationRegionServed();
						// find regionMaster object by id
						RegionMaster regionMaster = regionMasterRepository.findRegionById(regionId);
						orgRegionServed.setCreatedAt(date);
						orgRegionServed.setUpdatedAt(date);
						orgRegionServed.setCreatedBy(user.getUserDisplayName());
						orgRegionServed.setCreatedByEmail(user.getEmail());
						orgRegionServed.setUpdatedBy(user.getUserDisplayName());
						orgRegionServed.setUpdatedByEmail(user.getEmail());
						orgRegionServed.setIsActive(true);
						orgRegionServed.setOrganization(organization);
						orgRegionServed.setRegionMaster(regionMaster);

						orgRegionServedList.add(orgRegionServed);

					}
				}

			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.region.error.created"), e);
		}
		return orgRegionServedList;

	}

	/**
	 * prepare OrganizationResource for Organization
	 * 
	 * @param payload
	 * @param user
	 * @param organization
	 * @return
	 */
	private List<OrganizationResource> saveOrganizationResourcesForBulkUpload(
			OrganizationDataMigrationCsvPayload payload, UserPayload user, Organization organization) {
		List<Long> resourceIds = new ArrayList<>();
		List<OrganizationResource> orgResourceList = new ArrayList<>();
		try {
			if (null != payload) {
				// split string with comma separated values with removing
				// leading and trailing
				// whitespace
				String[] resourceCategoryIdsList = payload.getResourceIds().split(",");
				for (int j = 0; j < resourceCategoryIdsList.length; j++) {
					if (!StringUtils.isEmpty(resourceCategoryIdsList[j]))
						resourceIds.add(Long.parseLong(resourceCategoryIdsList[j].trim()));
				}
				Date date = CommonUtils.getFormattedDate();

				if (null != resourceIds) {
					for (Long resourceId : resourceIds) {
						// for organization resource creation
						OrganizationResource organizationResource = new OrganizationResource();
						// find resourceCategory object by id
						ResourceCategory resourceCategory = resourceCategoryRepository
								.findResourceCategoryById(resourceId);

						organizationResource.setCreatedAt(date);
						organizationResource.setUpdatedAt(date);
						organizationResource.setCreatedBy(user.getUserDisplayName());
						organizationResource.setCreatedByEmail(user.getEmail());
						organizationResource.setUpdatedBy(user.getUserDisplayName());
						organizationResource.setUpdatedByEmail(user.getEmail());
						organizationResource.setIsActive(true);
						organizationResource.setOrganization(organization);
						organizationResource.setResourceCategory(resourceCategory);
						orgResourceList.add(organizationResource);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.resource.error.created"), e);
		}
		return orgResourceList;

	}

	/**
	 * prepare OrganizationDataSet for Organization
	 * 
	 * @param payload
	 * @param user
	 * @param organization
	 * @return
	 */
	private List<OrganizationDataSet> saveOrganizationDataSetsForBulkUpload(OrganizationDataMigrationCsvPayload payload,
			UserPayload user, Organization organization) {
		List<Long> datasetIds = new ArrayList<>();
		List<OrganizationDataSet> orgDatasetList = new ArrayList<>();
		try {
			if (null != payload) {
				// split string with comma separated values with removing
				// leading and trailing
				// whitespace
				String[] datasetCategoryIdsList = payload.getDatasetIds().split(",");
				for (int j = 0; j < datasetCategoryIdsList.length; j++) {
					if (!StringUtils.isEmpty(datasetCategoryIdsList[j]))
						datasetIds.add(Long.parseLong(datasetCategoryIdsList[j].trim()));
				}
				Date date = CommonUtils.getFormattedDate();

				if (null != datasetIds) {
					for (Long datasetId : datasetIds) {
						// for organization dataset creation
						OrganizationDataSet organizationDataSet = new OrganizationDataSet();
						// find regionMaster object by id
						DataSetCategory dataSetCategory = dataSetCategoryRepository.findDataSetCategoryById(datasetId);
						organizationDataSet.setCreatedAt(date);
						organizationDataSet.setUpdatedAt(date);
						organizationDataSet.setCreatedBy(user.getUserDisplayName());
						organizationDataSet.setCreatedByEmail(user.getEmail());
						organizationDataSet.setUpdatedBy(user.getUserDisplayName());
						organizationDataSet.setUpdatedByEmail(user.getEmail());
						organizationDataSet.setIsActive(true);
						organizationDataSet.setOrganization(organization);
						organizationDataSet.setDataSetCategory(dataSetCategory);
						organizationDataSet.setType(payload.getDatasetType());
						orgDatasetList.add(organizationDataSet);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.error.created"), e);
		}
		return orgDatasetList;
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
		List<OrganizationSdgData> sdgDataMapList = new ArrayList<>();

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
		List<OrganizationSpiData> spiDataMapList = new ArrayList<>();
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
	 * save programs in bulk
	 * 
	 * @param programPayloadList
	 * @param response
	 * @param operationPerformed
	 * @param customMessage
	 * @return
	 */
	public void saveProgramOfflineForBulkUpload(List<ProgramDataMigrationCsvPayload> programPayloadList,
			ExceptionResponse response, String operationPerformed, String customMessage, UserPayload user) {
		ExceptionResponse errorResForNaics = new ExceptionResponse();
		ExceptionResponse errorResForNtee = new ExceptionResponse();
		try {
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

			if (null != programPayloadList) {
				List<Program> programsListToSaveIntoDB = new ArrayList<>();
				int batchInsertSize = 1000;
				int totalProgramsToSave = programPayloadList.size();
				int remainingProgramsToSave = totalProgramsToSave % batchInsertSize;
				int numOfProgramsToSaveByBatchSize = (totalProgramsToSave - remainingProgramsToSave);

				int i = 1;
				for (ProgramDataMigrationCsvPayload programPayload : programPayloadList) {
					if (null != programPayload && null != programPayload.getId()) {
						if (operationPerformed.equals(OrganizationConstants.CREATE)) {
							// set Program Object into list to save into DB
							programsListToSaveIntoDB.add(setProgramDataForBulkUpload(programPayload, user,
									operationPerformed, naicsMapForS3, nteeMapForS3, spiDataMap, sdgDataMap));

							// save the programs in the batches of 1000 and
							// save the remaining programs
							if (i % 1000 == 0) {
								saveProgramsIntoDB(programsListToSaveIntoDB, i);
								// refresh the data after added into list
								programsListToSaveIntoDB = new ArrayList<Program>();
								// refresh the data after added into list
								programsListToSaveIntoDB = new ArrayList<Program>();
								// save the remaining programs when total
								// size is less than 1000
							} else if (numOfProgramsToSaveByBatchSize == 0
									&& (programsListToSaveIntoDB.size() == remainingProgramsToSave)) {
								saveProgramsIntoDB(programsListToSaveIntoDB, i);
								// refresh the data after added into list
								programsListToSaveIntoDB = new ArrayList<Program>();
								// refresh the data after added into list
								programsListToSaveIntoDB = new ArrayList<Program>();
								// save the remaining programs when total
								// size is greater than 1000
							} else if (i > numOfProgramsToSaveByBatchSize
									&& (programsListToSaveIntoDB.size() == remainingProgramsToSave)) {
								saveProgramsIntoDB(programsListToSaveIntoDB, i);
								// refresh the data after added into list
								programsListToSaveIntoDB = new ArrayList<Program>();
								programsListToSaveIntoDB = new ArrayList<Program>();
							}
							i++;

						} // end of if
					}
				}

			}

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage(customMessage), e);
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			// for Slack Notification
			Date date = CommonUtils.getFormattedDate();
			SlackMessage slackMessage = SlackMessage.builder().username("WinWinMessageNotifier")
					.text(("WinWinWiki Program Data Migration Process has failed to run for app env: "
							+ System.getenv("WINWIN_ENV") + " , initiated by user: " + user.getUserDisplayName()
							+ " at " + date + " due to error: \n" + e.getMessage()))
					.channel(slackChannelName).as_user("true").build();
			slackNotificationSenderService.sendSlackMessageNotification(slackMessage);
		}
	}

	/**
	 * prepare data for Program to save into DB
	 * 
	 * @param requestPayload
	 * @param user
	 * @param operationPerformed
	 * @param naicsMapForS3
	 * @param nteeMapForS3
	 * @param spiDataMap
	 * @param sdgDataMap
	 * @return
	 * @throws Exception
	 */
	private Program setProgramDataForBulkUpload(ProgramDataMigrationCsvPayload requestPayload, UserPayload user,
			String operationPerformed, Map<String, NaicsDataMappingPayload> naicsMapForS3,
			Map<String, NteeDataMappingPayload> nteeMapForS3, Map<Long, SpiData> spiDataMap,
			Map<Long, SdgData> sdgDataMap) throws Exception {

		Program program = new Program();
		BeanUtils.copyProperties(requestPayload, program);

		// set all the regionServed for program
		if (!StringUtils.isEmpty(requestPayload.getRegionServedIds()))
			program.setProgramRegionServed(saveProgramRegionServedForBulkUpload(requestPayload, user, program));

		// set all the resources for program
		if (!StringUtils.isEmpty(requestPayload.getResourceIds()))
			program.setProgramResource(saveProgramResourcesForBulkUpload(requestPayload, user, program));

		// set all the dataset for program
		if (!StringUtils.isEmpty(requestPayload.getDatasetIds()))
			program.setProgramDataSet(saveProgramDataSetsForBulkUpload(requestPayload, user, program));

		program.setIsActive(true);

		Date date = CommonUtils.getFormattedDate();
		program.setCreatedAt(date);
		program.setUpdatedAt(date);
		program.setCreatedBy(user.getUserDisplayName());
		program.setUpdatedBy(user.getUserDisplayName());
		program.setCreatedByEmail(user.getEmail());
		program.setUpdatedByEmail(user.getEmail());
		program.setIsActive(true);

		if (requestPayload.getParentId() != null) {
			Organization organization = organizationRepository.findOrgById(requestPayload.getParentId());
			program.setOrganization(organization);
		}

		List<Long> spiTagIds = new ArrayList<>();
		List<Long> sdgTagIds = new ArrayList<>();

		if (!StringUtils.isEmpty(requestPayload.getSpiTagIds())) {
			// split string with comma separated values with removing
			// leading and trailing
			// whitespace
			String[] spiIdsList = requestPayload.getSpiTagIds().split(",");
			for (int j = 0; j < spiIdsList.length; j++) {
				if (!StringUtils.isEmpty(spiIdsList[j])) {
					if (!StringUtils.isEmpty(spiIdsList[j].trim()))
						spiTagIds.add(Long.parseLong(spiIdsList[j].trim()));
				}

			}
		}
		if (!StringUtils.isEmpty(requestPayload.getSdgTagIds())) {
			// split string with comma separated values with removing
			// leading and trailing
			// whitespace
			String[] sdgIdsList = requestPayload.getSdgTagIds().split(",");
			for (int j = 0; j < sdgIdsList.length; j++) {
				if (!StringUtils.isEmpty(sdgIdsList[j])) {
					if (!StringUtils.isEmpty(sdgIdsList[j].trim()))
						sdgTagIds.add(Long.parseLong(sdgIdsList[j].trim()));
				}

			}
		}
		if (operationPerformed.equals(OrganizationConstants.CREATE)) {
			// create program's spi tags mapping for Bulk Creation
			program.setProgramSpiData(saveProgramSpiMappingForBulkCreation(program, user, spiTagIds, spiDataMap));
			// create program's sdg tags mapping for Bulk Creation
			program.setProgramSdgData(saveProgramSdgMappingForBulkCreation(program, user, sdgTagIds, sdgDataMap));
		}

		return program;
	}// end of method

	/**
	 * save and Flush Programs into DB
	 * 
	 * @param programs
	 * @param i
	 * @return
	 */
	//@Transactional
	@Async
	private ProgramBulkResultPayload saveProgramsIntoDB(List<Program> programs, int i) {
		// Implemented below logic to log failed and success
		// programs for bulk upload
		List<Program> successProgramList = new ArrayList<Program>();
		List<ProgramBulkFailedPayload> failedProgramList = new ArrayList<ProgramBulkFailedPayload>();
		try {
			LOGGER.info("Saving programs : " + programs.size() + " Starting from: " + (i - programs.size()));
			successProgramList.addAll(programRepository.saveAll(programs));
			// Flush all pending changes to the database
			programRepository.flush();
			LOGGER.info("Saved programs: " + programs.size());
		} catch (Exception e) {
			LOGGER.info("Failed to save programs starting from: " + (i - programs.size()));

			// Added the below logic to save the program one by one when
			// the bulk program batch failed because of some faulty
			// program and save the perfect program into
			// successProgramList
			LOGGER.info("Saving Failed programs one by one starting from : " + (i - programs.size()));
			for (Program failedProgram : programs) {
				try {
					successProgramList.add(programRepository.saveAndFlush(failedProgram));
				} catch (Exception e1) {
					ProgramBulkFailedPayload failedProg = new ProgramBulkFailedPayload();
					failedProg.setFailedProgram(failedProgram);
					failedProg.setFailedMessage(e1.getMessage());
					failedProgramList.add(failedProg);
				}
			}
			successProgramList.addAll(programs);
		}
		ProgramBulkResultPayload payload = new ProgramBulkResultPayload();
		payload.setSuccessProgramList(successProgramList);
		payload.setFailedProgramList(failedProgramList);

		return payload;

	}

	/**
	 * prepare ProgramRegionServed for Program
	 * 
	 * @param payload
	 * @param user
	 * @param program
	 * @return
	 */
	private List<ProgramRegionServed> saveProgramRegionServedForBulkUpload(ProgramDataMigrationCsvPayload payload,
			UserPayload user, Program program) {
		List<Long> regionIds = new ArrayList<>();
		List<ProgramRegionServed> programRegionServedList = new ArrayList<ProgramRegionServed>();
		try {
			if (null != payload) {
				// split string with comma separated values with removing
				// leading and trailing
				// whitespace
				String[] regionMasterIdsList = payload.getRegionServedIds().split(",");
				for (int j = 0; j < regionMasterIdsList.length; j++) {
					if (!StringUtils.isEmpty(regionMasterIdsList[j]))
						regionIds.add(Long.parseLong(regionMasterIdsList[j].trim()));
				}
				Date date = CommonUtils.getFormattedDate();

				if (null != regionIds) {
					for (Long regionId : regionIds) {
						// for program regionServed creation
						ProgramRegionServed programRegionServed = new ProgramRegionServed();
						// find regionMaster obj by id
						RegionMaster regionMaster = regionMasterRepository.findRegionById(regionId);
						programRegionServed.setCreatedAt(date);
						programRegionServed.setUpdatedAt(date);
						programRegionServed.setCreatedBy(user.getUserDisplayName());
						programRegionServed.setCreatedByEmail(user.getEmail());
						programRegionServed.setUpdatedBy(user.getUserDisplayName());
						programRegionServed.setUpdatedByEmail(user.getEmail());
						programRegionServed.setIsActive(true);
						programRegionServed.setProgram(program);
						programRegionServed.setRegionMaster(regionMaster);
						programRegionServedList.add(programRegionServed);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("prog.region.error.created"), e);
		}
		return programRegionServedList;

	}

	/**
	 * prepare ProgramResource for Program
	 * 
	 * @param payload
	 * @param user
	 * @param Program
	 * @return
	 */
	private List<ProgramResource> saveProgramResourcesForBulkUpload(ProgramDataMigrationCsvPayload payload,
			UserPayload user, Program program) {
		List<Long> resourceIds = new ArrayList<>();
		List<ProgramResource> programResourceList = new ArrayList<>();
		try {
			if (null != payload) {
				// split string with comma separated values with removing
				// leading and trailing
				// whitespace
				String[] resourceCategoryIdsList = payload.getResourceIds().split(",");
				for (int j = 0; j < resourceCategoryIdsList.length; j++) {
					if (!StringUtils.isEmpty(resourceCategoryIdsList[j]))
						resourceIds.add(Long.parseLong(resourceCategoryIdsList[j].trim()));
				}
				Date date = CommonUtils.getFormattedDate();

				if (null != resourceIds) {
					for (Long resourceId : resourceIds) {
						// for program resource creation
						ProgramResource programResource = new ProgramResource();
						// find ResourceCategory object by id
						ResourceCategory resourceCategory = resourceCategoryRepository
								.findResourceCategoryById(resourceId);

						programResource.setCreatedAt(date);
						programResource.setUpdatedAt(date);
						programResource.setCreatedBy(user.getUserDisplayName());
						programResource.setCreatedByEmail(user.getEmail());
						programResource.setUpdatedBy(user.getUserDisplayName());
						programResource.setUpdatedByEmail(user.getEmail());
						programResource.setIsActive(true);
						programResource.setProgram(program);
						programResource.setResourceCategory(resourceCategory);
						programResourceList.add(programResource);

					}
				}

			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("prog.resource.error.created"), e);
		}
		return programResourceList;

	}// end of method

	/**
	 * prepare ProgramDataSet for Program
	 * 
	 * @param payload
	 * @param user
	 * @param program
	 * @return
	 */
	private List<ProgramDataSet> saveProgramDataSetsForBulkUpload(ProgramDataMigrationCsvPayload payload,
			UserPayload user, Program program) {
		List<Long> datasetIds = new ArrayList<>();
		List<ProgramDataSet> programDatasetList = new ArrayList<>();
		try {
			if (null != payload) {
				// split string with comma separated values with removing
				// leading and trailing
				// whitespace
				String[] datasetCategoryIdsList = payload.getDatasetIds().split(",");
				for (int j = 0; j < datasetCategoryIdsList.length; j++) {
					if (!StringUtils.isEmpty(datasetCategoryIdsList[j]))
						datasetIds.add(Long.parseLong(datasetCategoryIdsList[j].trim()));
				}
				Date date = CommonUtils.getFormattedDate();

				if (null != datasetIds) {
					for (Long datasetId : datasetIds) {
						// for ProgramDataSet creation
						ProgramDataSet programDataSet = new ProgramDataSet();
						// find DataSetCategory object by id
						DataSetCategory dataSetCategory = dataSetCategoryRepository.findDataSetCategoryById(datasetId);

						programDataSet.setCreatedAt(date);
						programDataSet.setUpdatedAt(date);
						programDataSet.setCreatedBy(user.getUserDisplayName());
						programDataSet.setCreatedByEmail(user.getEmail());
						programDataSet.setUpdatedBy(user.getUserDisplayName());
						programDataSet.setUpdatedByEmail(user.getEmail());
						programDataSet.setIsActive(true);
						programDataSet.setProgram(program);
						programDataSet.setDataSetCategory(dataSetCategory);
						programDataSet.setType(payload.getDatasetType());
						programDatasetList.add(programDataSet);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("prog.dataset.error.created"), e);
		}
		return programDatasetList;

	}// end of method

	/**
	 * @throws Exception
	 * 
	 */
	@SuppressWarnings("unused")
	private Program setProgramSpiSdgMappingForBulkCreation(ProgramDataMigrationCsvPayload requestPayload,
			Program program, UserPayload user, Map<String, NaicsDataMappingPayload> naicsMapForS3,
			Map<String, NteeDataMappingPayload> nteeMapForS3, Map<Long, SpiData> spiDataMap,
			Map<Long, SdgData> sdgDataMap) throws Exception {
		List<Long> spiIdsByNaicsCode = new ArrayList<>();
		List<Long> sdgIdsByNaicsCode = new ArrayList<>();
		List<Long> spiIdsByNteeCode = new ArrayList<>();
		List<Long> sdgIdsByNteeCode = new ArrayList<>();
		List<Long> spiIds = new ArrayList<>();
		List<Long> sdgIds = new ArrayList<>();
		NaicsData naicsData = null;
		NteeData nteeData = null;

		if (null != requestPayload.getNaicsCode()) {
			// set Naics-Ntee code map
			if (naicsMap == null || nteeMap == null)
				setNaicsNteeMap();
			naicsData = naicsMap.get(requestPayload.getNaicsCode());
		}

		if (null != requestPayload.getNteeCode()) {
			// set Naics-Ntee code map
			if (naicsMap == null || nteeMap == null)
				setNaicsNteeMap();
			nteeData = nteeMap.get(requestPayload.getNteeCode());
		}

		if (null != naicsData && (!StringUtils.isEmpty(naicsData.getCode()))) {
			NaicsDataMappingPayload naicsMapPayload = naicsMapForS3.get(naicsData.getCode());
			spiIdsByNaicsCode = naicsMapPayload.getSpiTagIds();
			sdgIdsByNaicsCode = naicsMapPayload.getSdgTagIds();
		}

		if (null != nteeData && (!StringUtils.isEmpty(nteeData.getCode()))) {
			NteeDataMappingPayload nteeMapPayload = nteeMapForS3.get(nteeData.getCode());
			spiIdsByNteeCode = nteeMapPayload.getSpiTagIds();
			sdgIdsByNteeCode = nteeMapPayload.getSdgTagIds();
		}

		if (null != spiIdsByNaicsCode && null != spiIdsByNteeCode)
			spiIds = Stream.of(spiIdsByNaicsCode, spiIdsByNteeCode).flatMap(x -> x.stream())
					.collect(Collectors.toList());

		if (null != sdgIdsByNaicsCode && null != sdgIdsByNteeCode)
			sdgIds = Stream.of(sdgIdsByNaicsCode, sdgIdsByNteeCode).flatMap(x -> x.stream())
					.collect(Collectors.toList());

		// create program's spi tags mapping
		program.setProgramSpiData(saveProgramSpiMappingForBulkCreation(program, user, spiIds, spiDataMap));
		// create program's sdg tags mapping
		program.setProgramSdgData(saveProgramSdgMappingForBulkCreation(program, user, sdgIds, sdgDataMap));
		return program;
	}

	/**
	 * @param program
	 * @param user
	 * @param sdgDataMapObj
	 * @param sdgIdsList
	 * @return
	 * @throws Exception
	 */
	private List<ProgramSdgData> saveProgramSdgMappingForBulkCreation(Program program, UserPayload user,
			List<Long> sdgIdsList, Map<Long, SdgData> sdgDataMap) throws Exception {
		ProgramSdgData sdgDataMapObj = null;
		Date date = CommonUtils.getFormattedDate();
		List<ProgramSdgData> sdgDataMapList = new ArrayList<>();

		if (null != sdgIdsList) {
			for (Long sdgId : sdgIdsList) {
				SdgData sdgData = sdgDataMap.get(sdgId);
				if (null != sdgData) {
					sdgDataMapObj = new ProgramSdgData();
					sdgDataMapObj.setSdgData(sdgData);
					sdgDataMapObj.setProgram(program);
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
	 * @param program
	 * @param user
	 * @param spiDataMapObj
	 * @param spiIdsList
	 * @return
	 * @throws Exception
	 */
	private List<ProgramSpiData> saveProgramSpiMappingForBulkCreation(Program program, UserPayload user,
			List<Long> spiIdsList, Map<Long, SpiData> spiDataMap) throws Exception {
		ProgramSpiData spiDataMapObj = null;
		Date date = CommonUtils.getFormattedDate();
		List<ProgramSpiData> spiDataMapList = new ArrayList<>();
		if (null != spiIdsList) {
			for (Long spiId : spiIdsList) {
				SpiData spiData = spiDataMap.get(spiId);
				if (null != spiData) {
					spiDataMapObj = new ProgramSpiData();
					spiDataMapObj.setSpiData(spiData);
					spiDataMapObj.setProgram(program);
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
	 * 
	 */
	private void setNaicsNteeMap() {
		if (naicsMap == null) {
			List<NaicsData> naicsCodeList = naicsDataRepository.findAll();
			if (null != naicsCodeList) {
				naicsMap = new HashMap<>();
				for (NaicsData naicsData : naicsCodeList)
					naicsMap.put(naicsData.getCode(), naicsData);
			}
		}
		if (nteeMap == null) {
			List<NteeData> nteeCodeList = nteeDataRepository.findAll();
			if (null != nteeCodeList) {
				nteeMap = new HashMap<>();
				for (NteeData nteeData : nteeCodeList)
					nteeMap.put(nteeData.getCode(), nteeData);
			}
		}

	}

	private Address saveAddressForBulkUpload(OrganizationDataMigrationCsvPayload payload, UserPayload user) {
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

				// set city, state for address as null to apply org list sorting
				// NULLS
				// LAST
				if (StringUtils.isEmpty(payload.getCity()))
					address.setCity(null);
				if (StringUtils.isEmpty(payload.getState()))
					address.setState(null);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.address.created"), e);
		}
		return address;
	}
}
