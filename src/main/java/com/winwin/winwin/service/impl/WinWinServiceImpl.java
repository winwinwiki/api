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
import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.OrganizationSdgData;
import com.winwin.winwin.entity.OrganizationSpiData;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.ProgramDataSet;
import com.winwin.winwin.entity.ProgramResource;
import com.winwin.winwin.entity.ProgramSdgData;
import com.winwin.winwin.entity.ProgramSpiData;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.entity.SdgData;
import com.winwin.winwin.entity.SpiData;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.DataMigrationCsvPayload;
import com.winwin.winwin.payload.NaicsDataMappingPayload;
import com.winwin.winwin.payload.NaicsMappingCsvPayload;
import com.winwin.winwin.payload.NteeDataMappingPayload;
import com.winwin.winwin.payload.NteeMappingCsvPayload;
import com.winwin.winwin.payload.ProgramRequestPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.AddressRepository;
import com.winwin.winwin.repository.ClassificationRepository;
import com.winwin.winwin.repository.DataSetCategoryRepository;
import com.winwin.winwin.repository.NaicsDataRepository;
import com.winwin.winwin.repository.NteeDataRepository;
import com.winwin.winwin.repository.OrgClassificationMapRepository;
import com.winwin.winwin.repository.OrgSdgDataMapRepository;
import com.winwin.winwin.repository.OrgSpiDataMapRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.repository.ProgramDataSetRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.repository.ProgramResourceRepository;
import com.winwin.winwin.repository.ProgramSdgDataMapRepository;
import com.winwin.winwin.repository.ProgramSpiDataMapRepository;
import com.winwin.winwin.repository.ResourceCategoryRepository;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.repository.SpiDataRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.service.WinWinService;
import com.winwin.winwin.util.CommonUtils;
import com.winwin.winwin.util.CsvUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class WinWinServiceImpl implements WinWinService {
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
	CsvUtils csvUtils;
	@Autowired
	SpiDataRepository spiDataRepository;
	@Autowired
	OrgSpiDataMapRepository orgSpiDataMapRepository;
	@Autowired
	ProgramSpiDataMapRepository programSpiDataMapRepository;
	@Autowired
	SdgDataRepository sdgDataRepository;
	@Autowired
	OrgSdgDataMapRepository orgSdgDataMapRepository;
	@Autowired
	ProgramSdgDataMapRepository programSdgDataMapRepository;
	@Autowired
	OrganizationResourceRepository organizationResourceRepository;
	@Autowired
	ProgramResourceRepository programResourceRepository;
	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;
	@Autowired
	ProgramDataSetRepository programDataSetRepository;
	@Autowired
	ResourceCategoryRepository resourceCategoryRepository;
	@Autowired
	DataSetCategoryRepository dataSetCategoryRepository;
	@Autowired
	NteeDataRepository nteeDataRepository;
	@Autowired
	NaicsDataRepository naicsDataRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinServiceImpl.class);

	private Map<String, NaicsData> naicsMap = null;
	private Map<String, NteeData> nteeMap = null;

	@Override
	@Transactional
	public List<Organization> createOrganizationsOffline(List<DataMigrationCsvPayload> organizationPayloadList,
			ExceptionResponse response, UserPayload user) {
		List<Organization> organizationList = saveOrganizationsOfflineForBulkUpload(organizationPayloadList, response,
				OrganizationConstants.CREATE, "org.exception.created", user);

		return organizationList;
	}

	@Override
	@Transactional
	@Async
	public List<Program> createProgramsOffline(List<DataMigrationCsvPayload> programPayloadList,
			ExceptionResponse response, UserPayload user) {
		List<Program> programList = saveProgramOfflineForBulkUpload(programPayloadList, response,
				OrganizationConstants.CREATE, "prg.exception.created", user);

		return programList;
	}

	public List<Organization> saveOrganizationsOfflineForBulkUpload(
			List<DataMigrationCsvPayload> organizationPayloadList, ExceptionResponse response,
			String operationPerformed, String customMessage, UserPayload user) {
		List<Organization> organizationList = new ArrayList<Organization>();
		Map<Long, String> datasetsTypeMap = new HashMap<Long, String>();
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

			List<SpiData> spiDataList = spiDataRepository.findAllSpiData();
			Map<Long, SpiData> spiDataMap = spiDataList.stream()
					.collect(Collectors.toMap(SpiData::getId, SpiData -> SpiData));

			List<SdgData> sdgDataList = sdgDataRepository.findAllSdgData();
			Map<Long, SdgData> sdgDataMap = sdgDataList.stream()
					.collect(Collectors.toMap(SdgData::getId, SdgData -> SdgData));

			Map<Long, List<Long>> dataSetMapById = new HashMap<Long, List<Long>>();
			Map<Long, List<Long>> resourceMapById = new HashMap<Long, List<Long>>();

			if (null != organizationPayloadList) {
				for (DataMigrationCsvPayload organizationPayload : organizationPayloadList) {
					if (null != organizationPayload && null != organizationPayload.getId()) {
						if (!StringUtils.isEmpty(organizationPayload.getResourceIds())) {
							// split string with comma separated values with removing leading and trailing
							// whitespace
							String[] resourceIdsList = organizationPayload.getResourceIds().split("\\S*,\\S*");
							List<Long> resourceIds = new ArrayList<Long>();
							for (int j = 0; j < resourceIdsList.length; j++) {
								resourceIds.add(Long.parseLong(resourceIdsList[j]));
							}
							resourceMapById.put(organizationPayload.getId(), resourceIds);
						}
						if (!StringUtils.isEmpty(organizationPayload.getDatasetIds())) {
							// split string with comma separated values with removing leading and trailing
							// whitespace
							String[] datasetIdsList = organizationPayload.getDatasetIds().split("\\S*,\\S*");
							List<Long> datasetIds = new ArrayList<Long>();
							for (int j = 0; j < datasetIdsList.length; j++) {
								datasetIds.add(Long.parseLong(datasetIdsList[j]));
							}
							dataSetMapById.put(organizationPayload.getId(), datasetIds);
						}
						if (!StringUtils.isEmpty(organizationPayload.getDatasetType())) {
							if (null != organizationPayload.getId())
								datasetsTypeMap.put(organizationPayload.getId(), organizationPayload.getDatasetType());
						}
						if (operationPerformed.equals(OrganizationConstants.CREATE)) {
							organizationList.add(setOrganizationDataForBulkUpload(organizationPayload, user,
									operationPerformed, naicsMapForS3, nteeMapForS3, spiDataMap, sdgDataMap));
							// organizationList.add(setOrganizationDataForBulkUpload(organizationPayload,
							// user, date));
						}
					}
				}
			}
			organizationList = organizationRepository.saveAll(organizationList);

			if (null != organizationList) {
				for (Organization organization : organizationList) {
					// To save list of resourceIds and datasetIds fetched
					// from .csv file
					saveOrgDatasetAndResources(organization, user, resourceMapById.get(organization.getId()),
							dataSetMapById.get(organization.getId()), datasetsTypeMap.get(organization.getId()));
				}
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
	private Organization setOrganizationDataForBulkUpload(DataMigrationCsvPayload csvPayload, UserPayload user,
			String operationPerformed, Map<String, NaicsDataMappingPayload> naicsMapForS3,
			Map<String, NteeDataMappingPayload> nteeMapForS3, Map<Long, SpiData> spiDataMap,
			Map<Long, SdgData> sdgDataMap) throws Exception {

		Organization organization = new Organization();
		BeanUtils.copyProperties(csvPayload, organization);
		organization.setAddress(saveAddressForBulkUpload(csvPayload, user));

		if (null != csvPayload.getNaicsCode()) {
			// set Naics-Ntee code map
			if (naicsMap == null || nteeMap == null)
				setNaicsNteeMap();
			organization.setNaicsCode(naicsMap.get(csvPayload.getNaicsCode()));
		}

		if (null != csvPayload.getNteeCode()) {
			// set Naics-Ntee code map
			if (naicsMap == null || nteeMap == null)
				setNaicsNteeMap();
			organization.setNteeCode(nteeMap.get(csvPayload.getNteeCode()));

		}

		organization.setType(OrganizationConstants.ORGANIZATION);
		organization.setPriority(OrganizationConstants.PRIORITY_NORMAL);
		organization.setIsActive(true);

		Date date = CommonUtils.getFormattedDate();
		organization.setCreatedAt(date);
		organization.setCreatedBy(user.getEmail());
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
			}
		}

		// To set spi and sdg tags by naics code and ntee code from s3
		if (null != organization.getNaicsCode() || null != organization.getNteeCode()) {
			if (operationPerformed.equals(OrganizationConstants.CREATE)) {
				organization = setOrganizationSpiSdgMappingForBulkCreation(organization, user, naicsMapForS3,
						nteeMapForS3, spiDataMap, sdgDataMap);
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

	private List<OrganizationNote> saveOrganizationNotesForBulkUpload(DataMigrationCsvPayload payload, UserPayload user,
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
	 * @param resourceIdsList
	 * @param datasetIdsList
	 * @throws Exception method saveOrgDatasetAndResources fetches list of
	 *                   resourceIdsList and datasetIdsList from .csv file and
	 *                   create entries for particular organization
	 */
	private void saveOrgDatasetAndResources(Organization organization, UserPayload user, List<Long> resourceIdsList,
			List<Long> datasetIdsList, String datasetType) throws Exception {
		@SuppressWarnings("unused")
		List<OrganizationResource> resourceList = createOrgResourcesByResourceCategory(organization, user,
				resourceIdsList);

		@SuppressWarnings("unused")
		List<OrganizationDataSet> datasetList = createOrgDataSetByDataSetCategory(organization, user, datasetIdsList,
				datasetType);
	}

	/**
	 * @param organization
	 * @param user
	 * @param resourceCategoryList
	 * @return
	 * @throws Exception
	 */
	private List<OrganizationResource> createOrgResourcesByResourceCategory(Organization organization, UserPayload user,
			List<Long> resourceCategoryList) throws Exception {
		List<OrganizationResource> resourceList = new ArrayList<OrganizationResource>();
		Date date = CommonUtils.getFormattedDate();
		if (null != resourceCategoryList) {
			for (Long categoryId : resourceCategoryList) {
				ResourceCategory resourceCategory = resourceCategoryRepository.findResourceCategoryById(categoryId);
				if (null != resourceCategory) {
					OrganizationResource resource = null;
					/*
					 * List<OrganizationResource> resources = organizationResourceRepository
					 * .findAllOrgResourceById(organization.getId()); if (null != resources &&
					 * !resources.isEmpty()) { for (OrganizationResource organizationResource :
					 * resources) { resource = organizationResource; if (null !=
					 * organizationResource.getResourceCategory() && organizationResource
					 * .getResourceCategory().getId().equals(resourceCategory. getId())) {
					 * resource.setResourceCategory(organizationResource. getResourceCategory()); }
					 * else { resource.setResourceCategory(resourceCategory); } } } else {
					 */
					resource = new OrganizationResource();
					resource.setResourceCategory(resourceCategory);
					// }
					resource.setOrganizationId(organization.getId());
					resource.setCreatedAt(date);
					resource.setUpdatedAt(date);
					resource.setCreatedBy(user.getEmail());
					resource.setUpdatedBy(user.getEmail());
					resourceList.add(resource);
				}
			}

			if (!resourceList.isEmpty()) {
				resourceList = organizationResourceRepository.saveAll(resourceList);
			}

		}
		return resourceList;
	}// end of method

	/**
	 * @param organization
	 * @param user
	 * @param datasetCategoryList
	 * @param datasetType
	 * @return
	 * @throws Exception
	 */
	private List<OrganizationDataSet> createOrgDataSetByDataSetCategory(Organization organization, UserPayload user,
			List<Long> datasetCategoryList, String datasetType) throws Exception {
		List<OrganizationDataSet> datasetList = new ArrayList<OrganizationDataSet>();
		Date date = CommonUtils.getFormattedDate();
		if (null != datasetCategoryList) {
			for (Long categoryId : datasetCategoryList) {
				DataSetCategory datasetCategory = dataSetCategoryRepository.findDataSetCategoryById(categoryId);
				if (null != datasetCategory) {
					OrganizationDataSet dataset = null;
					/*
					 * List<OrganizationDataSet> datasets = organizationDataSetRepository
					 * .findAllOrgDataSetList(organization.getId()); if (null != datasets &&
					 * !datasets.isEmpty()) { for (OrganizationDataSet organizationDataset :
					 * datasets) { dataset = organizationDataset; if (null !=
					 * organizationDataset.getDataSetCategory() && organizationDataset
					 * .getDataSetCategory().getId().equals(datasetCategory. getId())) {
					 * dataset.setDataSetCategory(organizationDataset. getDataSetCategory()); } else
					 * { dataset.setDataSetCategory(datasetCategory); } } } else {
					 */
					dataset = new OrganizationDataSet();
					dataset.setDataSetCategory(datasetCategory);
					// }
					dataset.setOrganizationId(organization.getId());
					if (!StringUtils.isEmpty(datasetType)) {
						dataset.setType(datasetType);
					}
					dataset.setCreatedAt(date);
					dataset.setUpdatedAt(date);
					dataset.setCreatedBy(user.getEmail());
					dataset.setUpdatedBy(user.getEmail());
					datasetList.add(dataset);
				}
			}
			if (!datasetList.isEmpty()) {
				datasetList = organizationDataSetRepository.saveAll(datasetList);
			}
		}
		return datasetList;
	}// end of method

	/**
	 * @param programPayloadList
	 * @param response
	 * @param operationPerformed
	 * @param customMessage
	 * @return
	 */
	public List<Program> saveProgramOfflineForBulkUpload(List<DataMigrationCsvPayload> programPayloadList,
			ExceptionResponse response, String operationPerformed, String customMessage, UserPayload user) {
		List<Program> programList = new ArrayList<Program>();
		Map<Long, String> datasetsTypeMap = new HashMap<Long, String>();
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

			List<SpiData> spiDataList = spiDataRepository.findAllSpiData();
			Map<Long, SpiData> spiDataMap = spiDataList.stream()
					.collect(Collectors.toMap(SpiData::getId, SpiData -> SpiData));

			List<SdgData> sdgDataList = sdgDataRepository.findAllSdgData();
			Map<Long, SdgData> sdgDataMap = sdgDataList.stream()
					.collect(Collectors.toMap(SdgData::getId, SdgData -> SdgData));

			Map<Long, List<Long>> dataSetMapById = new HashMap<Long, List<Long>>();
			Map<Long, List<Long>> resourceMapById = new HashMap<Long, List<Long>>();
			if (null != programPayloadList) {
				for (DataMigrationCsvPayload programPayload : programPayloadList) {
					if (null != programPayload && null != programPayload.getId()) {
						if (!StringUtils.isEmpty(programPayload.getResourceIds())) {
							// split string with comma separated values with removing leading and trailing
							// whitespace
							String[] resourceIdsList = programPayload.getResourceIds().split("\\S*,\\S*");
							List<Long> resourceIds = new ArrayList<Long>();
							for (int j = 0; j < resourceIdsList.length; j++) {
								resourceIds.add(Long.parseLong(resourceIdsList[j]));
							}
							resourceMapById.put(programPayload.getId(), resourceIds);
						}
						if (!StringUtils.isEmpty(programPayload.getDatasetIds())) {
							// split string with comma separated values with removing leading and trailing
							// whitespace
							String[] datasetIdsList = programPayload.getDatasetIds().split("\\S*,\\S*");
							List<Long> datasetIds = new ArrayList<Long>();
							for (int j = 0; j < datasetIdsList.length; j++) {
								datasetIds.add(Long.parseLong(datasetIdsList[j]));
							}
							dataSetMapById.put(programPayload.getId(), datasetIds);
						}
						if (!StringUtils.isEmpty(programPayload.getDatasetType())) {
							if (null != programPayload.getName())
								datasetsTypeMap.put(programPayload.getId(), programPayload.getDatasetType());
						}
						if (operationPerformed.equals(OrganizationConstants.CREATE)) {
							programPayload.setPriority(OrganizationConstants.PRIORITY_NORMAL);
							programList.add(setProgramDataForBulkUpload(programPayload, user, operationPerformed,
									naicsMapForS3, nteeMapForS3, spiDataMap, sdgDataMap));
						}
					}
				}

			}

			programList = programRepository.saveAll(programList);

			for (Program program : programList) {
				if (null != program.getId()) {
					// To save list of resourceIds and datasetIds fetched from
					// .csv file
					saveProgramDatasetAndResources(program, user, resourceMapById.get(program.getId()),
							dataSetMapById.get(program.getId()), datasetsTypeMap.get(program.getId()));
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage(customMessage), e);
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return programList;
	}

	/**
	 * @param organizationPayload
	 * @param organization
	 * @param user
	 * @return
	 * @throws Exception
	 */
	private Program setProgramDataForBulkUpload(DataMigrationCsvPayload requestPayload, UserPayload user,
			String operationPerformed, Map<String, NaicsDataMappingPayload> naicsMapForS3,
			Map<String, NteeDataMappingPayload> nteeMapForS3, Map<Long, SpiData> spiDataMap,
			Map<Long, SdgData> sdgDataMap) throws Exception {

		Program program = new Program();
		BeanUtils.copyProperties(requestPayload, program);
		program.setAddress(saveAddressForBulkUpload(requestPayload, user));

		if (null != requestPayload.getNaicsCode()) {
			// set Naics-Ntee code map
			if (naicsMap == null || nteeMap == null)
				setNaicsNteeMap();
			program.setNaicsCode(naicsMap.get(requestPayload.getNaicsCode()));
		}

		if (null != requestPayload.getNteeCode()) {
			// set Naics-Ntee code map
			if (naicsMap == null || nteeMap == null)
				setNaicsNteeMap();
			program.setNteeCode(nteeMap.get(requestPayload.getNteeCode()));

		}

		program.setPriority(OrganizationConstants.PRIORITY_NORMAL);
		program.setIsActive(true);

		Date date = CommonUtils.getFormattedDate();
		program.setCreatedAt(date);
		program.setCreatedBy(user.getEmail());
		program.setUpdatedAt(date);
		program.setUpdatedBy(user.getEmail());
		program.setIsActive(true);

		if (requestPayload.getParentId() != null) {
			Organization organization = organizationRepository.findOrgById(requestPayload.getParentId());
			program.setOrganization(organization);
		}

		if (requestPayload.getNaicsCode() == null && requestPayload.getNteeCode() == null) {
			List<Long> spiTagIds = new ArrayList<>();
			List<Long> sdgTagIds = new ArrayList<>();

			if (!StringUtils.isEmpty(requestPayload.getSpiTagIds())) {
				// split string with comma separated values with removing leading and trailing
				// whitespace
				String[] spiIdsList = requestPayload.getSpiTagIds().split("\\S*,\\S*");
				for (int j = 0; j < spiIdsList.length; j++) {
					spiTagIds.add(Long.parseLong(spiIdsList[j]));
				}
			}
			if (!StringUtils.isEmpty(requestPayload.getSdgTagIds())) {
				// split string with comma separated values with removing leading and trailing
				// whitespace
				String[] sdgIdsList = requestPayload.getSdgTagIds().split("\\S*,\\S*");
				for (int j = 0; j < sdgIdsList.length; j++) {
					sdgTagIds.add(Long.parseLong(sdgIdsList[j]));
				}
			}
			if (operationPerformed.equals(OrganizationConstants.CREATE)) {
				// create program's spi tags mapping for Bulk Creation
				program.setProgramSpiData(saveProgramSpiMappingForBulkCreation(program, user, spiTagIds, spiDataMap));
				// create program's sdg tags mapping for Bulk Creation
				program.setProgramSdgData(saveProgramSdgMappingForBulkCreation(program, user, sdgTagIds, sdgDataMap));
			}
		}

		// To set spi and sdg tags by naics code and ntee code from s3
		if (null != program.getNaicsCode() || null != program.getNteeCode()) {
			if (operationPerformed.equals(OrganizationConstants.CREATE)) {
				program = setProgramSpiSdgMappingForBulkCreation(program, user, naicsMapForS3, nteeMapForS3, spiDataMap,
						sdgDataMap);
			}
		}

		return program;
	}

	/**
	 * @throws Exception
	 * 
	 */
	private Program setProgramSpiSdgMappingForBulkCreation(Program program, UserPayload user,
			Map<String, NaicsDataMappingPayload> naicsMapForS3, Map<String, NteeDataMappingPayload> nteeMapForS3,
			Map<Long, SpiData> spiDataMap, Map<Long, SdgData> sdgDataMap) throws Exception {
		List<Long> spiIdsByNaicsCode = new ArrayList<Long>();
		List<Long> sdgIdsByNaicsCode = new ArrayList<Long>();
		List<Long> spiIdsByNteeCode = new ArrayList<Long>();
		List<Long> sdgIdsByNteeCode = new ArrayList<Long>();
		List<Long> spiIds = new ArrayList<Long>();
		List<Long> sdgIds = new ArrayList<Long>();

		if (null != program.getNaicsCode()) {
			NaicsDataMappingPayload naicsMapPayload = naicsMapForS3.get(program.getNaicsCode().getCode());
			spiIdsByNaicsCode = naicsMapPayload.getSpiTagIds();
			sdgIdsByNaicsCode = naicsMapPayload.getSdgTagIds();
		}

		if (null != program.getNteeCode()) {
			NteeDataMappingPayload nteeMapPayload = nteeMapForS3.get(program.getNteeCode().getCode());
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
		List<ProgramSdgData> sdgDataMapList = new ArrayList<ProgramSdgData>();

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
					sdgDataMapObj.setCreatedBy(user.getEmail());
					sdgDataMapObj.setUpdatedBy(user.getEmail());
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
		List<ProgramSpiData> spiDataMapList = new ArrayList<ProgramSpiData>();
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
					spiDataMapObj.setCreatedBy(user.getEmail());
					spiDataMapObj.setUpdatedBy(user.getEmail());
					spiDataMapList.add(spiDataMapObj);
				}
			}
		}

		return spiDataMapList;
	}

	/**
	 * @param program
	 * @param user
	 * @param resourceIdsList
	 * @param datasetIdsList
	 * @throws Exception method saveProgramDatasetAndResources fetches list of
	 *                   resourceIdsList and datasetIdsList from .csv file and
	 *                   create entries for particular program
	 */
	private void saveProgramDatasetAndResources(Program program, UserPayload user, List<Long> resourceIdsList,
			List<Long> datasetIdsList, String datasetType) throws Exception {
		@SuppressWarnings("unused")
		List<ProgramResource> resourceList = createProgramResourcesByResourceCategory(program, user, resourceIdsList);

		@SuppressWarnings("unused")
		List<ProgramDataSet> datasetList = createProgramDataSetByDataSetCategory(program, user, datasetIdsList,
				datasetType);
	}

	/**
	 * @param program
	 * @param user
	 * @param resourceCategoryList
	 * @return
	 * @throws Exception
	 */
	private List<ProgramResource> createProgramResourcesByResourceCategory(Program program, UserPayload user,
			List<Long> resourceCategoryList) throws Exception {
		List<ProgramResource> resourceList = new ArrayList<ProgramResource>();
		Date date = CommonUtils.getFormattedDate();
		if (null != resourceCategoryList) {
			for (Long categoryId : resourceCategoryList) {
				ResourceCategory resourceCategory = resourceCategoryRepository.findResourceCategoryById(categoryId);
				if (null != resourceCategory) {
					ProgramResource resource = null;
					/*
					 * List<ProgramResource> resources = programResourceRepository
					 * .findAllProgramResourceByProgramId(program.getId()); if (null != resources &&
					 * !resources.isEmpty()) { for (ProgramResource programResource : resources) {
					 * resource = programResource; if (null != programResource.getResourceCategory()
					 * && programResource.getResourceCategory().getId().equals(
					 * resourceCategory.getId())) { resource.setResourceCategory(programResource.
					 * getResourceCategory()); } else {
					 * resource.setResourceCategory(resourceCategory); } } } else {
					 */
					resource = new ProgramResource();
					resource.setResourceCategory(resourceCategory);
					// }
					resource.setProgramId(program.getId());
					resource.setCreatedAt(date);
					resource.setUpdatedAt(date);
					resource.setCreatedBy(user.getEmail());
					resource.setUpdatedBy(user.getEmail());
					resourceList.add(resource);
				}
			}

			if (!resourceList.isEmpty()) {
				resourceList = programResourceRepository.saveAll(resourceList);
			}

		}
		return resourceList;
	}// end of method

	/**
	 * @param program
	 * @param user
	 * @param datasetCategoryList
	 * @param datasetType
	 * @return
	 * @throws Exception
	 */
	private List<ProgramDataSet> createProgramDataSetByDataSetCategory(Program program, UserPayload user,
			List<Long> datasetCategoryList, String datasetType) throws Exception {
		List<ProgramDataSet> datasetList = new ArrayList<ProgramDataSet>();
		Date date = CommonUtils.getFormattedDate();
		if (null != datasetCategoryList) {
			for (Long categoryId : datasetCategoryList) {
				DataSetCategory datasetCategory = dataSetCategoryRepository.findDataSetCategoryById(categoryId);
				if (null != datasetCategory) {
					ProgramDataSet dataset = null;
					/*
					 * List<ProgramDataSet> datasets = programDataSetRepository
					 * .findAllProgramDataSetListByProgramId(program.getId()); if (null != datasets
					 * && !datasets.isEmpty()) { for (ProgramDataSet programDataset : datasets) {
					 * dataset = programDataset; if (null != programDataset.getDataSetCategory() &&
					 * programDataset.getDataSetCategory().getId().equals( datasetCategory.getId()))
					 * { dataset.setDataSetCategory(programDataset. getDataSetCategory()); } else {
					 * dataset.setDataSetCategory(datasetCategory); } } } else {
					 */
					dataset = new ProgramDataSet();
					dataset.setDataSetCategory(datasetCategory);
					// }
					dataset.setProgramId(program.getId());
					if (!StringUtils.isEmpty(datasetType)) {
						dataset.setType(datasetType);
					}
					dataset.setCreatedAt(date);
					dataset.setUpdatedAt(date);
					dataset.setCreatedBy(user.getEmail());
					dataset.setUpdatedBy(user.getEmail());
					datasetList.add(dataset);
				}
			}
			if (!datasetList.isEmpty()) {
				datasetList = programDataSetRepository.saveAll(datasetList);
			}
		}
		return datasetList;
	}// end of method

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

	public Address saveAddressForBulkUpload(ProgramRequestPayload payload, UserPayload user) {
		Address address = null;
		try {
			if (null != payload) {
				address = new Address();
				Date date = CommonUtils.getFormattedDate();
				BeanUtils.copyProperties(payload, address);
				address.setId(null);
				address.setCreatedAt(date);
				address.setCreatedBy(user.getEmail());
				address.setUpdatedAt(date);
				address.setUpdatedBy(user.getEmail());
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.address.created"), e);
		}
		return address;
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

	public Address saveAddressForBulkUpload(DataMigrationCsvPayload payload, UserPayload user) {
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

}
