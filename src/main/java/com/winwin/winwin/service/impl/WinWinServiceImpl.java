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
import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.entity.Organization;
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
import com.winwin.winwin.payload.OrganizationNotePayload;
import com.winwin.winwin.payload.OrganizationRequestPayload;
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
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.service.WinWinService;
import com.winwin.winwin.util.CommonUtils;
import com.winwin.winwin.util.CsvUtils;

/**
 * @author ArvindK
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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinServiceImpl.class);

	@Override
	public List<Organization> createOrganizationsOffline(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response) {
		List<Organization> organizationList = saveOrganizationsForBulkUpload(organizationPayloadList, response,
				OrganizationConstants.CREATE, "org.exception.created");

		return organizationList;
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

		Date date = CommonUtils.getFormattedDate();
		if (organization.getId() == null) {
			organization.setCreatedAt(date);
			organization.setCreatedBy(user.getEmail());
		}
		organization.setIsActive(true);;
		organization.setUpdatedAt(date);
		organization.setUpdatedBy(user.getEmail());

		if (organizationPayload.getNaicsCode() == null && organizationPayload.getNteeCode() == null) {
			saveOrgSpiSdgMapping(organization, user, spiDataMapObj, sdgDataMapObj, organizationPayload.getSpiTagIds(),
					organizationPayload.getSdgTagIds());

		}
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
		InputStream input = s3Object.getObjectContent();
		String csv = IOUtils.toString(input);
		List<NaicsMappingCsvPayload> naicsMappingCsvPayloadList = CsvUtils.read(NaicsMappingCsvPayload.class, csv);
		Integer rowNumber = null;
		try {
			if (null != s3Object) {
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
			errorResForNaics.setErrorMessage("error occurred while fetching details of row: " + rowNumber
					+ " from the file " + awsS3ObjectServiceImpl.getNaicsAwsKey() + ", error: " + e.toString());
			errorResForNaics.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return naicsMap;
	}

	/**
	 * @throws Exception
	 * 
	 */
	private Map<String, NteeDataMappingPayload> getNteeSpiSdgMap(ExceptionResponse errorResForNtee) throws Exception {
		Map<String, NteeDataMappingPayload> nteeMap = null;
		Integer rowNumber = null;
		try {
			nteeMap = new HashMap<>();
			S3Object s3Object = awsS3ObjectServiceImpl.getS3Object(awsS3ObjectServiceImpl.getNteeAwsKey());
			if (null != s3Object) {
				InputStream input = s3Object.getObjectContent();
				String csv = IOUtils.toString(input);
				List<NteeMappingCsvPayload> nteeMappingCsvPayloadList = CsvUtils.read(NteeMappingCsvPayload.class, csv);

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
			LOGGER.error("", e);
			errorResForNtee.setErrorMessage("error occurred while fetching details of row: " + rowNumber
					+ " from the file " + awsS3ObjectServiceImpl.getNteeAwsKey() + ", error: " + e.toString());
			errorResForNtee.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
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

}
