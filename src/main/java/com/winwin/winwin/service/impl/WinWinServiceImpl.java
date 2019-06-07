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
import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.entity.OrganizationSdgData;
import com.winwin.winwin.entity.OrganizationSpiData;
import com.winwin.winwin.entity.SdgData;
import com.winwin.winwin.entity.SpiData;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.AddressPayload;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinServiceImpl.class);

	@Override
	public List<Organization> createOrganizationsOffline(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response) {
		List<Organization> organizationList = saveOrganizationsOfflineForBulkUpload(organizationPayloadList, response,
				OrganizationConstants.CREATE, "org.exception.created");

		return organizationList;
	}

	public List<Organization> saveOrganizationsOfflineForBulkUpload(
			List<OrganizationRequestPayload> organizationPayloadList, ExceptionResponse response,
			String operationPerformed, String customMessage) {
		ExceptionResponse errorResForNaics = new ExceptionResponse();
		ExceptionResponse errorResForNtee = new ExceptionResponse();
		List<Organization> organizationList = new ArrayList<Organization>();

		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (!StringUtils.isEmpty(errorResForNaics.getErrorMessage())) {
				throw new Exception(errorResForNaics.getErrorMessage());
			}
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
		organization.setIsActive(true);
		;
		organization.setUpdatedAt(date);
		organization.setUpdatedBy(user.getEmail());

		// To save list of spiTagIds and sdgTagIds from fetched from .csv file
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
		saveOrgSpiSdgMappingOffline(organization, user, spiDataMapObj, sdgDataMapObj, spiTagIds, sdgTagIds);

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
	 * @throws Exception
	 *             Method saveOrgSpiSdgMappingOffline fetches list of spiTagIds
	 *             and sdgTagIds from csv file and create entries for particular
	 *             organization
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

}
