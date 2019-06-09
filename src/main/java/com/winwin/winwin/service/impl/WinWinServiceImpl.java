/**
 * 
 */
package com.winwin.winwin.service.impl;

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
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.entity.SdgData;
import com.winwin.winwin.entity.SpiData;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrganizationNotePayload;
import com.winwin.winwin.payload.OrganizationRequestPayload;
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
import com.winwin.winwin.repository.ResourceCategoryRepository;
import com.winwin.winwin.repository.SdgDataRepository;
import com.winwin.winwin.repository.SpiDataRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.service.WinWinService;
import com.winwin.winwin.util.CommonUtils;

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
	OrganizationResourceRepository organizationResourceRepository;

	@Autowired
	ResourceCategoryRepository resourceCategoryRepository;

	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;

	@Autowired
	DataSetCategoryRepository dataSetCategoryRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinServiceImpl.class);

	@Override
	@Transactional
	public List<Organization> createOrganizationsOffline(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response) {
		List<Organization> organizationList = saveOrganizationsOfflineForBulkUpload(organizationPayloadList, response,
				OrganizationConstants.CREATE, "org.exception.created");

		return organizationList;
	}

	public List<Organization> saveOrganizationsOfflineForBulkUpload(
			List<OrganizationRequestPayload> organizationPayloadList, ExceptionResponse response,
			String operationPerformed, String customMessage) {
		OrganizationSpiData spiDataMapObj = null;
		OrganizationSdgData sdgDataMapObj = null;
		List<Organization> organizationList = new ArrayList<Organization>();
		List<Long> spiTagIds = new ArrayList<Long>();
		List<Long> sdgTagIds = new ArrayList<Long>();
		List<Long> resourceIds = new ArrayList<Long>();
		List<Long> datasetIds = new ArrayList<Long>();
		Map<String, String> notesMap = new HashMap<String, String>();
		Map<String, String> datasetsTypeMap = new HashMap<String, String>();

		try {
			UserPayload user = userService.getCurrentUserDetails();
			for (OrganizationRequestPayload organizationPayload : organizationPayloadList) {
				if (null != organizationPayload && null != user) {
					if (!StringUtils.isEmpty(organizationPayload.getNotes())) {
						if (null != organizationPayload.getName())
							notesMap.put(organizationPayload.getName(), organizationPayload.getNotes());
					}
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
					if (!StringUtils.isEmpty(organizationPayload.getResourceIds())) {
						String[] resourceIdsList = organizationPayload.getResourceIds().split(",");
						for (int j = 0; j < resourceIdsList.length; j++) {
							resourceIds.add(Long.parseLong(resourceIdsList[j]));
						}
					}
					if (!StringUtils.isEmpty(organizationPayload.getDatasetIds())) {
						String[] datasetIdsList = organizationPayload.getDatasetIds().split(",");
						for (int j = 0; j < datasetIdsList.length; j++) {
							datasetIds.add(Long.parseLong(datasetIdsList[j]));
						}
					}
					if (!StringUtils.isEmpty(organizationPayload.getDatasetType())) {
						if (null != organizationPayload.getName())
							datasetsTypeMap.put(organizationPayload.getName(), organizationPayload.getDatasetType());
					}
					if (operationPerformed.equals(OrganizationConstants.CREATE)) {
						organizationList.add(setOrganizationData(organizationPayload, user));
					}
				}
			}
			organizationList = organizationRepository.saveAll(organizationList);

			for (Organization organization : organizationList) {
				if (null != organization.getName()) {
					String notes = notesMap.get(organization.getName());
					OrganizationNotePayload organizationNotePayload = new OrganizationNotePayload();
					Date date = CommonUtils.getFormattedDate();
					organizationNotePayload.setName(notes);
					organizationNotePayload.setOrganizationId(organization.getId());
					organizationNotePayload.setCreatedAt(date);
					organizationNotePayload.setCreatedBy(user.getEmail());
					OrganizationNote note = organizationNoteService.createOrganizationNote(organizationNotePayload);

					// To save list of resourceIds and datasetIds from fetched from .csv file
					String datasetType = datasetsTypeMap.get(organization.getName());
					saveOrgDatasetAndResources(organization, user, resourceIds, datasetIds, datasetType);
				}
				// To save list of spiTagIds and sdgTagIds from fetched from .csv file
				saveOrgSpiSdgMappingOffline(organization, user, spiDataMapObj, sdgDataMapObj, spiTagIds, sdgTagIds);

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

		Date date = CommonUtils.getFormattedDate();
		organization.setCreatedAt(date);
		organization.setCreatedBy(user.getEmail());
		organization.setIsActive(true);
		organization.setUpdatedAt(date);
		organization.setUpdatedBy(user.getEmail());

		return organization;
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

	/**
	 * @param organization
	 * @param user
	 * @param spiDataMapObj
	 * @param sdgDataMapObj
	 * @param spiIdsList
	 * @param sdgIdsList
	 * @throws Exception Method saveOrgSpiSdgMappingOffline fetches list of
	 *                   spiTagIds and sdgTagIds from .csv file and create entries
	 *                   for particular organization
	 */
	private void saveOrgSpiSdgMappingOffline(Organization organization, UserPayload user,
			OrganizationSpiData spiDataMapObj, OrganizationSdgData sdgDataMapObj, List<Long> spiIdsList,
			List<Long> sdgIdsList) throws Exception {
		@SuppressWarnings("unused")
		List<OrganizationSpiData> spiDataMapList = saveOrgSpiMapping(organization, user, spiDataMapObj, spiIdsList);

		@SuppressWarnings("unused")
		List<OrganizationSdgData> sdgDataMapList = saveOrgSdgMapping(organization, user, sdgDataMapObj, sdgIdsList);
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
					OrganizationResource resource = new OrganizationResource();
					resource.setResourceCategory(resourceCategory);
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

				for (OrganizationResource orgResource : resourceList) {
					orgHistoryService.createOrganizationHistory(user, orgResource.getOrganizationId(),
							OrganizationConstants.CREATE, OrganizationConstants.RESOURCE, orgResource.getId(),
							orgResource.getDescription());
				}
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
					OrganizationDataSet dataset = new OrganizationDataSet();
					dataset.setDataSetCategory(datasetCategory);
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
				for (OrganizationDataSet organizationDataSet : datasetList) {
					orgHistoryService.createOrganizationHistory(user, organizationDataSet.getOrganizationId(),
							OrganizationConstants.CREATE, OrganizationConstants.DATASET, organizationDataSet.getId(),
							organizationDataSet.getDescription());
				}
			}
		}
		return datasetList;
	}// end of method

}
