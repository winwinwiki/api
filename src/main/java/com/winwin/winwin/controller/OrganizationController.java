/**
 * The class OrganizationController is mapped to all incoming request comes at '/organization' for creating new organizations and it's associated entities.
 * i.e.  programs,SPI tags mapping,SDG tags mapping, address, Note, Resources, DataSets.
 * 
 */
package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.entity.OrganizationRegionServed;
import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.entity.SlackMessage;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.DataSetCategoryPayload;
import com.winwin.winwin.payload.DataSetPayload;
import com.winwin.winwin.payload.OrganizationChartPayload;
import com.winwin.winwin.payload.OrganizationCsvPayload;
import com.winwin.winwin.payload.OrganizationDivisionPayload;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.payload.OrganizationFilterResponse;
import com.winwin.winwin.payload.OrganizationHistoryPayload;
import com.winwin.winwin.payload.OrganizationNotePayload;
import com.winwin.winwin.payload.OrganizationRegionServedPayload;
import com.winwin.winwin.payload.OrganizationRequestPayload;
import com.winwin.winwin.payload.OrganizationResourcePayload;
import com.winwin.winwin.payload.OrganizationResponsePayload;
import com.winwin.winwin.payload.OrganizationSdgDataMapPayload;
import com.winwin.winwin.payload.OrganizationSpiDataMapPayload;
import com.winwin.winwin.payload.ProgramFilterPayloadData;
import com.winwin.winwin.payload.ProgramRequestPayload;
import com.winwin.winwin.payload.ProgramResponsePayload;
import com.winwin.winwin.payload.RegionMasterFilterPayload;
import com.winwin.winwin.payload.RegionMasterPayload;
import com.winwin.winwin.payload.ResourceCategoryPayLoad;
import com.winwin.winwin.payload.SdgGoalPayload;
import com.winwin.winwin.payload.SpiDataDimensionsPayload;
import com.winwin.winwin.payload.SubOrganizationPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.NaicsDataRepository;
import com.winwin.winwin.repository.NteeDataRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationNoteRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.service.OrgNaicsDataService;
import com.winwin.winwin.service.OrgNteeDataService;
import com.winwin.winwin.service.OrgSdgDataService;
import com.winwin.winwin.service.OrgSpiDataService;
import com.winwin.winwin.service.OrganizationDataSetService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.OrganizationRegionServedService;
import com.winwin.winwin.service.OrganizationResourceService;
import com.winwin.winwin.service.OrganizationService;
import com.winwin.winwin.service.ProgramService;
import com.winwin.winwin.service.SdgDataService;
import com.winwin.winwin.service.SlackNotificationSenderService;
import com.winwin.winwin.service.SpiDataService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;
import com.winwin.winwin.util.CsvUtils;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@RestController
@RequestMapping(value = "/organization")
public class OrganizationController extends BaseController {

	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private OrganizationDataSetService organizationDataSetService;
	@Autowired
	private OrganizationDataSetRepository organizationDataSetRepository;
	@Autowired
	private OrganizationResourceService organizationResourceService;
	@Autowired
	private OrganizationResourceRepository organizationResourceRepository;
	@Autowired
	private OrganizationRegionServedService orgRegionServedService;
	@Autowired
	private OrgSpiDataService orgSpiDataService;
	@Autowired
	private OrgSdgDataService orgSdgDataService;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationNoteService organizationNoteService;
	@Autowired
	private OrganizationNoteRepository organizationNoteRepository;
	@Autowired
	private OrgNaicsDataService naicsDataService;
	@Autowired
	private OrgNteeDataService nteeDataService;
	@Autowired
	private ProgramService programService;
	@Autowired
	private ProgramRepository programRepository;
	@Autowired
	private SpiDataService spiDataService;
	@Autowired
	private SdgDataService sdgDataService;
	@Autowired
	private NteeDataRepository nteeDataRepository;
	@Autowired
	private NaicsDataRepository naicsDataRepository;
	@Autowired
	private SlackNotificationSenderService slackNotificationSenderService;
	@Autowired
	private CsvUtils csvUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationController.class);

	private Map<String, Long> naicsMap = null;
	private Map<String, Long> nteeMap = null;

	@Value("${slack.channel}")
	String SLACK_CHANNEL;

	// Code for organization start
	/**
	 * The below method creates new organization
	 * 
	 * @param organizationPayload
	 * @return
	 */
	@PostMapping(path = "")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrganization(@RequestBody OrganizationRequestPayload organizationPayload) {
		Organization organization = null;
		OrganizationResponsePayload payload = null;
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != organizationPayload) {
			organization = organizationService.createOrganization(organizationPayload, exceptionResponse);
			payload = setOrganizationPayload(organization);

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		} else {
			return sendErrorResponse("org.bad.request");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * The below method accepts CSV file and creates records in bulk for
	 * organizations and it's associated entities.
	 * 
	 * @param file
	 * @return
	 */
	@PostMapping(path = "/addAll")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> createOrganizations(@RequestParam("file") MultipartFile file) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (null != file) {
			LOGGER.info("csv read started - " + CommonUtils.getFormattedDate());
			List<OrganizationCsvPayload> organizationCsvPayload = csvUtils.read(OrganizationCsvPayload.class, file,
					exceptionResponse);
			LOGGER.info("csv read ended - " + CommonUtils.getFormattedDate());
			if (null != exceptionResponse.getException())
				return sendMsgResponse(exceptionResponse.getException().getMessage(),
						exceptionResponse.getStatusCode());

			if (null != organizationCsvPayload) {
				UserPayload user = userService.getCurrentUserDetails();
				if (null != user) {
					LOGGER.info("org service createOrganizations() started  with number of organizations - "
							+ organizationCsvPayload.size() + " created by: " + user.getUserDisplayName());
					// for Slack Notification
					Date date = CommonUtils.getFormattedDate();
					SlackMessage slackMessage = SlackMessage.builder().username("WinWinMessageNotifier")
							.text("WinWinWiki Bulk Upload Process has been started successfully for app env: "
									+ System.getenv("WINWIN_ENV") + " , initiated by user: " + user.getUserDisplayName()
									+ " at " + date)
							.channel(SLACK_CHANNEL).as_user("true").build();
					slackNotificationSenderService.sendSlackMessageNotification(slackMessage);
					organizationService.createOrganizations(organizationCsvPayload, exceptionResponse, user);
				}
			}
			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		} else {
			return sendErrorResponse("org.file.null");
		}
		return sendSuccessResponse("org.file.upload.success");
	}

	/**
	 * The below method accepts CSV file and update records in bulk for
	 * organizations and it's associated entities.
	 * 
	 * @param file
	 * @return
	 */
	@PutMapping(path = "/updateAll")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> updateOrganizations(@RequestParam("file") MultipartFile file) {
		List<Organization> organizationList = null;
		List<OrganizationResponsePayload> payloadList = new ArrayList<>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != file) {
			List<OrganizationCsvPayload> organizationCsvPayload = csvUtils.read(OrganizationCsvPayload.class, file,
					exceptionResponse);
			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			if (null != organizationCsvPayload) {
				// set Naics-Ntee code map
				setNaicsNteeMap();
				List<OrganizationRequestPayload> organizationPayloadList = organizationCsvPayload.stream()
						.map(this::setOrganizationPayload).collect(Collectors.toList());
				organizationService.updateOrganizations(organizationPayloadList, exceptionResponse);
				payloadList = setOrganizationPayload(organizationList);
			}
			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

		} else {
			return sendErrorResponse("org.file.null");
		}

		return sendSuccessResponse(payloadList);
	}

	/**
	 * Set a map for NaicsData and NteeData by their code and id.
	 */
	private void setNaicsNteeMap() {
		List<NaicsData> naicsCodeList = naicsDataRepository.findAll();
		if (null != naicsCodeList) {
			naicsMap = naicsCodeList.stream().collect(Collectors.toMap(NaicsData::getCode, NaicsData::getId));
		}
		List<NteeData> nteeCodeList = nteeDataRepository.findAll();
		if (null != nteeCodeList) {
			nteeMap = nteeCodeList.stream().collect(Collectors.toMap(NteeData::getCode, NteeData::getId));
		}
	}

	/**
	 * Delete Organization by Id
	 * 
	 * @param organizationPayLoad
	 * @return
	 */
	@DeleteMapping(path = "")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> deleteOrganization(@RequestBody OrganizationRequestPayload organizationPayLoad) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (null != organizationPayLoad && null != organizationPayLoad.getId()) {
			try {
				Long id = organizationPayLoad.getId();
				Organization organization = organizationRepository.findOrgById(id);
				if (organization == null)
					return sendErrorResponse("org.error.not_found");

				organizationService.deleteOrganization(id, OrganizationConstants.ORGANIZATION, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			} catch (Exception e) {
				exceptionResponse.setErrorMessage(e.getMessage());
				exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
				LOGGER.error(customMessageSource.getMessage("org.error.deleted"), e);
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
			}

		} else {
			return sendErrorResponse("org.bad.request");
		}

		return sendSuccessResponse("org.success.deleted");
	}

	/**
	 * Update Organization By Id
	 * 
	 * @param httpServletResponse
	 * @param organizationPayload
	 * @return
	 */
	@PutMapping(path = "")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateOrgDetails(@RequestBody List<OrganizationRequestPayload> orgPayloadList) {
		List<OrganizationResponsePayload> payloadList = new ArrayList<>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		Organization organization = null;
		try {
			for (OrganizationRequestPayload payload : orgPayloadList) {
				organization = organizationRepository.findOrgById(payload.getId());
				if (organization == null) {
					return sendErrorResponse("org.error.not_found");
				} else {
					organization = organizationService.updateOrgDetails(payload, organization,
							OrganizationConstants.ORGANIZATION, exceptionResponse);

					if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
							&& exceptionResponse.getStatusCode() != null)
						return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

					OrganizationResponsePayload responsePayload = setOrganizationPayload(organization);
					payloadList.add(responsePayload);
				}
			}
		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("org.error.updated"), e);
			return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		}
		return sendSuccessResponse(payloadList);

	}

	/**
	 * Returns an list of Organization with every organization's last updated
	 * history
	 * 
	 * @param filterPayload
	 * @return @
	 */
	@GetMapping(path = "")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrganizationList(OrganizationFilterPayload filterPayload) {
		List<OrganizationResponsePayload> payloadList = new ArrayList<>();
		List<Organization> orgList = new ArrayList<>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		try {
			if (null != filterPayload) {
				orgList = organizationService.getOrganizationList(filterPayload, exceptionResponse);
				filterPayload.setOrgCount(organizationService.getOrgCounts(filterPayload, exceptionResponse));
			}

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			if (orgList != null) {
				OrganizationResponsePayload payload = null;
				for (Organization organization : orgList) {
					payload = setOrganizationPayload(organization);
					if (null != payload) {
						payload.setLastEditedAt(organization.getUpdatedAt());
						payload.setLastEditedBy(organization.getUpdatedBy());
						payload.setLastEditedByEmail(organization.getUpdatedByEmail());
					}
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("org.error.list"), e);
			return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		}

		return sendSuccessResponse(new OrganizationFilterResponse(filterPayload, payloadList));

	}

	/**
	 * Returns an Organization Details by Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(path = "/{id}")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgDetails(@PathVariable("id") Long id) {
		Organization organization = null;
		OrganizationResponsePayload payload = new OrganizationResponsePayload();
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		try {
			organization = organizationRepository.findOrgById(id);
			if (organization == null) {
				return sendErrorResponse("org.error.not_found");
			} else {
				payload = setOrganizationPayload(organization);
			}
		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("org.error.fetch"), e);
			return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		}
		return sendSuccessResponse(payload);

	}

	/**
	 * @param organization
	 * @param payload
	 * @return
	 */
	private List<OrganizationResponsePayload> setOrganizationPayload(List<Organization> organizationList) {
		List<OrganizationResponsePayload> payload = new ArrayList<>();
		if (null != organizationList) {
			for (int i = 0; i < organizationList.size(); i++)
				payload.add(setOrganizationPayload(organizationList.get(i)));
		}
		return payload;
	}

	private OrganizationResponsePayload setOrganizationPayload(Organization organization) {
		OrganizationResponsePayload payload = null;
		if (null != organization) {
			payload = new OrganizationResponsePayload();
			BeanUtils.copyProperties(organization, payload);
			if (null != organization.getAddress()) {
				AddressPayload addressPayload = new AddressPayload();
				BeanUtils.copyProperties(organization.getAddress(), addressPayload);
				payload.setAddress(addressPayload);
			}
			if (null != organization.getParentId()) {
				Organization parentOrganization = organizationRepository.findOrgById(organization.getParentId());
				if (null != parentOrganization)
					payload.setParentName(parentOrganization.getName());
			}

			if (null != organization.getRootParentId()) {
				Organization rootParentOrganization = organizationRepository
						.findOrgById(organization.getRootParentId());
				if (null != rootParentOrganization) {
					payload.setRootParentName(rootParentOrganization.getName());
				}
			}
		}
		return payload;
	}
	// Code for organization end

	// Code for organization data set start
	/**
	 * Creates DataSet for an Organization by Id
	 * 
	 * @param orgDataSetPayLoad
	 * @return
	 */
	@PostMapping(path = "/{id}/dataset")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrganizationDataSet(@RequestBody DataSetPayload orgDataSetPayLoad) {
		OrganizationDataSet organizationDataSet = null;
		DataSetPayload payload = null;
		DataSetCategory category = null;
		DataSetCategoryPayload payloadCategory = null;
		try {
			if (null != orgDataSetPayLoad) {
				organizationDataSet = organizationDataSetService.createOrUpdateOrganizationDataSet(orgDataSetPayLoad);
				if (null != organizationDataSet) {
					payload = new DataSetPayload();
					BeanUtils.copyProperties(organizationDataSet, payload);

					if (null != organizationDataSet.getOrganization())
						payload.setOrganizationId(organizationDataSet.getOrganization().getId());

					category = organizationDataSet.getDataSetCategory();
					if (null != category) {
						payloadCategory = new DataSetCategoryPayload();
						BeanUtils.copyProperties(category, payloadCategory);
					}
					payload.setDataSetCategory(payloadCategory);
				}
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.dataset.error.created");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Update DataSet for an Organization by Id
	 * 
	 * @param organizationDataSetPayLoad
	 * @return
	 */
	@PutMapping(path = "/{id}/dataset")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateOrganizationDataSet(@RequestBody DataSetPayload organizationDataSetPayLoad) {
		OrganizationDataSet dataSet = null;
		DataSetCategory category = null;
		DataSetCategoryPayload payloadCategory = null;
		DataSetPayload payload = null;
		if (null != organizationDataSetPayLoad && null != organizationDataSetPayLoad.getId()) {
			Long id = organizationDataSetPayLoad.getId();
			dataSet = organizationDataSetRepository.findOrgDataSetById(id);
			if (dataSet == null) {
				return sendErrorResponse("org.dataset.error.not_found");
			} else {
				payload = new DataSetPayload();
				dataSet = organizationDataSetService.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
				BeanUtils.copyProperties(dataSet, payload);

				if (null != dataSet.getOrganization())
					payload.setOrganizationId(dataSet.getOrganization().getId());

				category = dataSet.getDataSetCategory();
				if (null != category) {
					payloadCategory = new DataSetCategoryPayload();
					BeanUtils.copyProperties(category, payloadCategory);
				}
				payload.setDataSetCategory(payloadCategory);
			}
		} else {
			return sendErrorResponse("org.bad.request");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Delete a DataSet for an Organization by Id
	 * 
	 * @param orgDataSetPayLoad
	 * @return
	 */
	@DeleteMapping(path = "/{id}/dataset")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> deleteOrganizationDataSet(@RequestBody DataSetPayload orgDataSetPayLoad) {
		try {
			if (null != orgDataSetPayLoad && null != orgDataSetPayLoad.getId()) {
				Long id = orgDataSetPayLoad.getId();
				OrganizationDataSet dataSet = organizationDataSetRepository.findOrgDataSetById(id);

				if (dataSet == null)
					return sendErrorResponse("org.dataset.error.not_found");

				organizationDataSetService.removeOrganizationDataSet(id);
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.dataset.error.deleted");
		}
		return sendSuccessResponse("org.dataset.success.deleted");
	}

	/**
	 * Returns a DataSet List associated with Organization by Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(path = "{id}/datasets")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrganizationDataSetList(@PathVariable("id") Long id) {
		List<OrganizationDataSet> orgDataSetList = null;
		DataSetPayload payload = null;
		DataSetCategory category = null;
		DataSetCategoryPayload payloadCategory = null;
		List<DataSetPayload> payloadList = new ArrayList<>();
		try {
			orgDataSetList = organizationDataSetService.getOrganizationDataSetList(id);
			if (orgDataSetList != null) {
				for (OrganizationDataSet dataSet : orgDataSetList) {
					payload = new DataSetPayload();
					payload.setId(dataSet.getId());
					category = dataSet.getDataSetCategory();
					if (null != category) {
						payloadCategory = new DataSetCategoryPayload();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
					}
					payload.setDataSetCategory(payloadCategory);

					if (null != dataSet.getOrganization())
						payload.setOrganizationId(dataSet.getOrganization().getId());

					payload.setDescription(dataSet.getDescription());
					payload.setType(dataSet.getType());
					payload.setUrl(dataSet.getUrl());
					payload.setIsActive(dataSet.getIsActive());
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.dataset.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Returns a DataSet Category Master List
	 * 
	 * @return
	 */
	@GetMapping(path = "/{id}/dataset/categorylist")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> getOrganizationDataSetCategoryList() {
		List<DataSetCategory> orgDataSetCategoryList = null;
		List<DataSetCategoryPayload> payloadList = new ArrayList<>();
		DataSetCategoryPayload payload = null;
		try {
			orgDataSetCategoryList = organizationDataSetService.getDataSetCategoryList();
			if (orgDataSetCategoryList != null) {
				for (DataSetCategory category : orgDataSetCategoryList) {
					payload = new DataSetCategoryPayload();
					payload.setId(category.getId());
					payload.setCategoryName(category.getCategoryName());
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.dataset.category.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	// Code for organization data set end

	// Code for organization resource start
	/**
	 * Creates Resources for an Organization by Id
	 * 
	 * @param organizationResourcePayLoad
	 * @return
	 */
	@PostMapping(path = "/{id}/resource")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> createOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayload organizationResourcePayLoad) {
		OrganizationResource organizationResource = null;
		OrganizationResourcePayload payload = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		if (null != organizationResourcePayLoad) {
			try {
				organizationResource = organizationResourceService
						.createOrUpdateOrganizationResource(organizationResourcePayLoad);
				if (null != organizationResource) {
					payload = new OrganizationResourcePayload();
					payload.setId(organizationResource.getId());
					category = organizationResource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
					}
					payload.setResourceCategory(payloadCategory);

					if (null != organizationResource.getOrganization())
						payload.setOrganizationId(organizationResource.getOrganization().getId());

					payload.setCount(organizationResource.getCount());
					payload.setDescription(organizationResource.getDescription());
					payload.setIsActive(organizationResource.getIsActive());
				}
			} catch (Exception e) {
				return sendExceptionResponse(e, "org.resource.error.created");
			}
		} else {
			return sendErrorResponse("org.bad.request");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Update Resource for an Organization by Id
	 * 
	 * @param organizationResourcePayLoad
	 * @return
	 */
	@PutMapping(path = "/{id}/resource")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> updateOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayload organizationResourcePayLoad) {
		OrganizationResource organizationResource = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		OrganizationResourcePayload payload = null;
		try {
			if (null != organizationResourcePayLoad && null != organizationResourcePayLoad.getId()) {
				Long id = organizationResourcePayLoad.getId();
				organizationResource = organizationResourceRepository.findOrgResourceById(id);
				if (organizationResource == null) {
					return sendErrorResponse("org.resource.error.not_found");
				} else {
					organizationResource = organizationResourceService
							.createOrUpdateOrganizationResource(organizationResourcePayLoad);
					payload = new OrganizationResourcePayload();
					payload.setId(organizationResource.getId());
					category = organizationResource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
					}
					payload.setResourceCategory(payloadCategory);

					if (null != organizationResource.getOrganization())
						payload.setOrganizationId(organizationResource.getOrganization().getId());

					payload.setCount(organizationResource.getCount());
					payload.setDescription(organizationResource.getDescription());
					payload.setIsActive(organizationResource.getIsActive());
				}
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.resource.error.updated");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Delete Resource associated with Organization by Id
	 * 
	 * @param organizationResourcePayLoad
	 * @return @
	 */
	@DeleteMapping(path = "/{id}/resource")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> deleteOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayload organizationResourcePayLoad) {
		try {
			if (null != organizationResourcePayLoad && null != organizationResourcePayLoad.getId()) {
				Long id = organizationResourcePayLoad.getId();
				OrganizationResource organizationResource = organizationResourceRepository.findOrgResourceById(id);
				if (organizationResource == null) {
					return sendErrorResponse("org.resource.error.not_found");
				}
				organizationResourceService.removeOrganizationResource(id);
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.resource.error.delete");
		}
		return sendSuccessResponse("org.resource.success.deleted");
	}

	/**
	 * Returns a List of Resources for an Organization by Id
	 * 
	 * @param id
	 * @return @
	 */
	@GetMapping(path = "/{id}/resources")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrganizationResourceList(@PathVariable("id") Long id) {
		List<OrganizationResource> orgResourceList = null;
		OrganizationResourcePayload payload = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		List<OrganizationResourcePayload> payloadList = new ArrayList<>();
		try {
			orgResourceList = organizationResourceService.getOrganizationResourceList(id);
			if (orgResourceList != null) {
				for (OrganizationResource resource : orgResourceList) {
					payload = new OrganizationResourcePayload();
					BeanUtils.copyProperties(resource, payload);
					category = resource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						BeanUtils.copyProperties(category, payloadCategory);
					}

					if (null != resource.getOrganization())
						payload.setOrganizationId(resource.getOrganization().getId());

					payload.setResourceCategory(payloadCategory);
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.resource.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Returns an Resource Category Master List
	 * 
	 * @return
	 */
	@GetMapping(path = "/{id}/resource/categorylist")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> getOrganizationResourceCategoryList() {
		List<ResourceCategory> orgResourceCategoryList = null;
		ResourceCategoryPayLoad payload = null;
		List<ResourceCategoryPayLoad> payloadList = new ArrayList<>();
		try {
			orgResourceCategoryList = organizationResourceService.getResourceCategoryList();
			if (orgResourceCategoryList != null) {
				for (ResourceCategory category : orgResourceCategoryList) {
					payload = new ResourceCategoryPayLoad();
					BeanUtils.copyProperties(category, payload);
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.resource.category.error.list");
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for organization resource end

	// Code for organization region served start
	/**
	 * Creates OrganizationRegionServed for an Organization by Id
	 * 
	 * @param orgRegionServedPayloadList
	 * @return @
	 */
	@PutMapping(path = "/{id}/region")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgRegions(
			@RequestBody List<OrganizationRegionServedPayload> orgRegionServedPayloadList) {
		List<OrganizationRegionServed> orgRegionServedList = null;
		List<OrganizationRegionServedPayload> payloadList = new ArrayList<>();
		OrganizationRegionServedPayload payload = null;
		try {
			orgRegionServedList = orgRegionServedService.createOrgRegionServed(orgRegionServedPayloadList);
			if (null != orgRegionServedList) {
				for (OrganizationRegionServed region : orgRegionServedList) {
					payload = new OrganizationRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						RegionMasterPayload regionMasterPayload = new RegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());
						payload.setRegion(regionMasterPayload);
					}
					if (null != region.getOrganization())
						payload.setOrganizationId(region.getOrganization().getId());

					payload.setIsActive(region.getIsActive());
					payloadList.add(payload);

				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.region.error.created");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Returns OrganizationRegionServed List
	 * 
	 * @param id
	 * @return @
	 */
	@GetMapping(path = "/{id}/regions")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgRegionsList(@PathVariable Long id) {
		List<OrganizationRegionServed> orgRegionList = null;
		OrganizationRegionServedPayload payload = null;
		List<OrganizationRegionServedPayload> payloadList = new ArrayList<>();
		try {
			orgRegionList = orgRegionServedService.getOrgRegionServedList(id);
			if (orgRegionList != null) {
				for (OrganizationRegionServed region : orgRegionList) {
					payload = new OrganizationRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						RegionMasterPayload regionMasterPayload = new RegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());

						payload.setRegion(regionMasterPayload);
					}
					if (null != region.getOrganization())
						payload.setOrganizationId(region.getOrganization().getId());

					payload.setIsActive(region.getIsActive());
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.region.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Returns a Region Master List
	 * 
	 * @param filterPayload
	 * @return @
	 */
	@GetMapping(path = "/{id}/regionmasters")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgRegionsMasterList(RegionMasterFilterPayload filterPayload) {
		List<RegionMaster> orgRegionMasterList = null;
		RegionMasterPayload payload = null;
		List<RegionMasterPayload> payloadList = null;
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		try {
			if (null != filterPayload) {
				orgRegionMasterList = orgRegionServedService.getOrgRegionMasterList(filterPayload, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

				payloadList = new ArrayList<>();
				if (orgRegionMasterList != null) {
					for (RegionMaster region : orgRegionMasterList) {
						payload = new RegionMasterPayload();
						payload.setRegionId(region.getId());
						payload.setRegionName(region.getRegionName());
						payloadList.add(payload);
					}
				}
			}
		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("org.region.error.list"), e);
			return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		}

		if (payloadList == null)
			return sendSuccessResponse(new ArrayList<>());

		return sendSuccessResponse(payloadList);
	}

	// Code for organization region served end

	// Code for organization SPI data start
	/**
	 * Returns a SpiData Master List
	 * 
	 * @return @
	 */
	@GetMapping(path = "/spidata")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getSpiDataList() {
		List<SpiDataDimensionsPayload> payloadList = null;
		try {
			payloadList = spiDataService.getSpiDataForResponse();
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.spidata.error.list");
		}

		if (payloadList == null)
			return sendSuccessResponse(new ArrayList<>());

		return sendSuccessResponse(payloadList);
	}

	/**
	 * Creates SpiData Mapping for an Organization by Id
	 * 
	 * @param payloadList
	 * @param orgId
	 * @return @
	 */
	@PutMapping(path = "/{id}/spidata")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgSpiDataMapping(@RequestBody List<OrganizationSpiDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) {
		Organization organization = organizationRepository.findOrgById(orgId);
		if (organization == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		if (null != payloadList) {
			try {
				orgSpiDataService.createSpiDataMapping(payloadList, organization);
			} catch (Exception e) {
				return sendExceptionResponse(e, "org.spidata.error.created");
			}
			return sendSuccessResponse("org.spidata.success.created");
		} else {
			return sendErrorResponse("org.bad.request");
		}
	}

	/**
	 * Returns a SpiData selected list for an Organization by Id
	 * 
	 * @param orgId
	 * @return @
	 */
	@GetMapping(path = "/{id}/spidata/selected")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSpiData(@PathVariable("id") Long orgId) {
		List<OrganizationSpiDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			payloadList = orgSpiDataService.getSelectedSpiData(orgId);
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.spidata.error.selectedlist");
		}

		if (payloadList == null)
			return sendSuccessResponse(new ArrayList<>());

		return sendSuccessResponse(payloadList);

	}// Code for organization SPI data end

	// Code for organization SDG data start
	/**
	 * Returns a SdgData Master List
	 * 
	 * @return @
	 */
	@GetMapping(path = "/sdgdata")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgSdgDataList() {
		List<SdgGoalPayload> payloadList = new ArrayList<>();
		try {
			payloadList = sdgDataService.getSdgDataForResponse();
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.sdgdata.error.list");
		}
		return sendSuccessResponse(payloadList);

	}

	/**
	 * Creates SdgData Mapping for an Organization by Id
	 * 
	 * @param payloadList
	 * @param orgId
	 * @return @
	 */
	@PutMapping(path = "/{id}/sdgdata")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgSdgDataMapping(@RequestBody List<OrganizationSdgDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) {
		Organization organization = organizationRepository.findOrgById(orgId);
		if (organization == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		if (null != payloadList) {
			try {
				orgSdgDataService.createSdgDataMapping(payloadList, organization);
			} catch (Exception e) {
				return sendExceptionResponse(e, "org.sdgdata.error.created");
			}
			return sendSuccessResponse("org.sdgdata.success.created");

		} else {
			return sendErrorResponse("org.bad.request");
		}
	}

	/**
	 * Returns a SdgData selected list for an Organization by Id
	 * 
	 * @param orgId
	 * @return @
	 */
	@GetMapping(path = "/{id}/sdgdata/selected")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSdgData(@PathVariable("id") Long orgId) {
		List<OrganizationSdgDataMapPayload> payloadList = new ArrayList<OrganizationSdgDataMapPayload>();
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			payloadList = orgSdgDataService.getSelectedSdgData(orgId);

			if (payloadList == null) {
				return sendErrorResponse("org.sdgdata.error.not_found");
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.spidata.error.selectedlist");
		}
		return sendSuccessResponse(payloadList);

	}// Code for organization SDG data end

	// Code for organization Program Details start
	/**
	 * Creates a Program for an Organization by Id
	 * 
	 * @param programPayload
	 * @param orgId
	 * @return
	 */
	@PostMapping(path = "/{id}/program")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createProgram(@RequestBody ProgramRequestPayload programPayload,
			@PathVariable("id") Long orgId) {
		Program program = null;
		ProgramResponsePayload payload = null;
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.program.null"));
		try {
			program = programService.createProgram(programPayload, exceptionResponse);

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			payload = programService.getProgramResponseFromProgram(program);

		} catch (Exception e) {
			return sendExceptionResponse(e, "prg.error.created");
		}

		if (payload == null) {
			return sendSuccessResponse(new ProgramResponsePayload());
		}

		return sendSuccessResponse(payload);
	}

	/**
	 * Returns a Program List for an Organization by Id
	 * 
	 * @param orgId
	 * @param filterPayload
	 * @return @
	 */
	@GetMapping(path = "/{id}/program")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getProgramList(@PathVariable("id") Long orgId, ProgramFilterPayloadData filterPayload) {
		List<Program> prgList = null;
		List<ProgramResponsePayload> payloadList = new ArrayList<>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			prgList = programService.getProgramList(filterPayload, orgId, exceptionResponse);
			if (prgList != null) {
				for (Program program : prgList) {
					ProgramResponsePayload responsePayload = programService.getProgramResponseFromProgram(program);
					payloadList.add(responsePayload);
				}
			}
		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("prg.error.list"), e);
			return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		}
		return sendSuccessResponse(payloadList);

	}

	/**
	 * Delete a Program by Id
	 * 
	 * @param programPayLoad
	 * @return
	 */
	@DeleteMapping(path = "/{id}/program")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> deleteProgram(@RequestBody ProgramRequestPayload programPayLoad) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		try {
			if (null != programPayLoad && null != programPayLoad.getId()) {
				Long id = programPayLoad.getId();
				Program program = programRepository.findProgramById(id);
				if (program == null)
					return sendErrorResponse("prg.error.not_found");

				programService.deleteProgram(program, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("prg.error.deleted"), e);
			return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		}
		return sendSuccessResponse("prg.success.deleted");
	}

	/**
	 * Update Program Details by Id
	 * 
	 * @param httpServletResponse
	 * @param organizationPayload
	 * @return
	 */
	@PutMapping(path = "/{id}/program")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateProgramDetails(@RequestBody List<ProgramRequestPayload> prgPayloadList) {
		Program program = null;
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		List<ProgramResponsePayload> payloadList = new ArrayList<>();
		try {
			for (ProgramRequestPayload payload : prgPayloadList) {
				program = programRepository.findProgramById(payload.getId());
				if (program == null) {
					return sendErrorResponse("prg.error.not_found");
				} else {
					program = programService.updateProgram(payload, exceptionResponse);

					if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
							&& exceptionResponse.getStatusCode() != null)
						return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

					ProgramResponsePayload responsePayload = programService.getProgramResponseFromProgram(program);
					payloadList.add(responsePayload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prg.error.updated");
		}
		return sendSuccessResponse(payloadList);

	}
	// Code for organization Program Details end

	// Code for organization Chart start
	/**
	 * Returns a Organization Chart for an Organization by Id
	 * 
	 * @param orgId
	 * @return @
	 */
	@GetMapping(path = "/{id}/suborganization")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSuborganizationList(@PathVariable("id") Long orgId) {
		Organization organization;
		OrganizationChartPayload payload;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			organization = organizationRepository.findOrgById(orgId);
			if (organization == null) {
				return sendErrorResponse("org.bad.request");
			} else {
				payload = organizationService.getOrgCharts(organization);
				if (null != organization.getParentId()) {
					Organization parentOrganization = organizationRepository.findOrgById(organization.getParentId());
					if (null != parentOrganization) {
						payload.setParentId(parentOrganization.getId());
						payload.setParentName(parentOrganization.getName());
					}
				}

				if (null != organization.getRootParentId()) {
					Organization rootParentOrganization = organizationRepository
							.findOrgById(organization.getRootParentId());
					if (null != rootParentOrganization) {
						payload.setRootParentId(rootParentOrganization.getId());
						payload.setRootParentName(rootParentOrganization.getName());
					}
				}

			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.chart.error");
		}

		if (payload == null) {
			return sendSuccessResponse(new OrganizationChartPayload());
		}

		return sendSuccessResponse(payload);

	}

	/**
	 * Create Child Organization for an Organization by Id
	 * 
	 * @param subOrganizationPayload
	 * @return
	 */
	@PostMapping(path = "/{id}/suborganization")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createSuborganization(@RequestBody SubOrganizationPayload subOrganizationPayload) {
		OrganizationDivisionPayload payload = new OrganizationDivisionPayload();
		Organization organization = null;
		try {
			organization = organizationService.createSubOrganization(subOrganizationPayload);
			if (null != organization) {
				payload = new OrganizationDivisionPayload();
				payload.setId(organization.getId());
				payload.setName(organization.getName());
				payload.setChildrenType(organization.getType());

				if (null != organization.getAddress()) {
					AddressPayload addressPayload = new AddressPayload();
					BeanUtils.copyProperties(organization.getAddress(), addressPayload);
					payload.setLocation(addressPayload);
				}
			}

		} catch (Exception e) {
			return sendExceptionResponse(e, "org.error.created");
		}
		return sendSuccessResponse(payload);
	}
	// Code for organization Chart end

	// Code for organization notes start
	/**
	 * Create Note for an Organization by Id
	 * 
	 * @param organizationNotePayload
	 * @param orgId
	 * @return
	 */
	@PostMapping(path = "/{id}/notes")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> createOrgNote(@RequestBody OrganizationNotePayload organizationNotePayload,
			@PathVariable("id") Long orgId) {
		OrganizationNote note = null;
		OrganizationNotePayload payload = new OrganizationNotePayload();
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			note = organizationNoteService.createOrganizationNote(organizationNotePayload);
			payload = setOrganizationNotePayload(note, payload);
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.note.error.created");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Returns Notes List of an Organization by Id
	 * 
	 * @param orgId
	 * @return @
	 */
	@GetMapping(path = "/{id}/notes")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgNotesList(@PathVariable("id") Long orgId) {
		List<OrganizationNote> orgNoteList = null;
		OrganizationNotePayload payload = new OrganizationNotePayload();
		List<OrganizationNotePayload> payloadList = new ArrayList<>();
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			orgNoteList = organizationNoteRepository.findAllOrgNotesList(orgId);
			if (orgNoteList == null) {
				return sendErrorResponse("org.note.error.not_found");
			} else {
				for (OrganizationNote orgNote : orgNoteList) {
					payload = setOrganizationNotePayload(orgNote, payload);
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.note.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Update Note for an Organization by Id
	 * 
	 * @param httpServletResponse
	 * @param organizationPayload
	 * @return
	 */
	@PutMapping(path = "/{id}/notes")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateOrgNote(@RequestBody OrganizationNotePayload organizationNotePayload,
			@PathVariable("id") Long orgId) {
		OrganizationNote note = null;
		OrganizationNotePayload payload = new OrganizationNotePayload();
		if (orgId == null || organizationNotePayload == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			note = organizationNoteService.updateOrganizationNote(organizationNotePayload);
			payload = setOrganizationNotePayload(note, payload);
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.note.error.updated");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Delete Note of an Organization by Id
	 * 
	 * @param organizationNotePayLoad
	 * @return
	 */
	@DeleteMapping(path = "/{id}/notes")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> deleteOrgNote(@RequestBody OrganizationNotePayload organizationNotePayLoad) {
		try {
			if (null != organizationNotePayLoad && null != organizationNotePayLoad.getId()) {
				Long noteId = organizationNotePayLoad.getId();
				OrganizationNote note = organizationNoteRepository.findOrgNoteById(noteId);
				if (note == null) {
					return sendErrorResponse("org.note.error.not_found");
				}
				organizationNoteService.removeOrganizationNote(noteId, organizationNotePayLoad.getOrganizationId());
			} else {
				return sendErrorResponse("org.bad.request");
			}

		} catch (Exception e) {
			return sendExceptionResponse(e, "org.note.error.deleted");
		}
		return sendSuccessResponse("org.note.success.deleted");
	}

	/**
	 * Return List OrganizationHistory of an Organization by Id
	 * 
	 * @param orgId
	 * @return
	 */
	@GetMapping(path = "/{id}/history")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgHistoy(@PathVariable("id") Long orgId) {
		List<OrganizationHistoryPayload> payloadList = new ArrayList<>();
		;
		try {
			if (null != orgId) {
				payloadList = organizationService.getOrgHistoryDetails(orgId);
			} else {
				return sendErrorResponse("org.bad.request");
			}

		} catch (Exception e) {
			return sendExceptionResponse(e, "org.history.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Returns a NaicsData Master List
	 * 
	 * @param search
	 * @return
	 */
	@GetMapping(path = "/naics_data")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgNaicsData(@RequestParam(name = "search", required = false) String search) {
		List<NaicsData> payloadList = new ArrayList<>();
		try {
			payloadList = naicsDataService.getAllOrgNaicsData();
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.history.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Returns a NteeData Master List
	 * 
	 * @param search
	 * @return
	 */
	@GetMapping(path = "/ntee_data")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgNteeData(@RequestParam(name = "search", required = false) String search) {
		List<NteeData> payloadList = new ArrayList<>();
		try {
			payloadList = nteeDataService.getAllOrgNteeData();
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.history.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * @param organizationNote
	 * @param payload
	 * @return
	 */
	private OrganizationNotePayload setOrganizationNotePayload(OrganizationNote organizationNote,
			OrganizationNotePayload payload) {
		if (null != organizationNote) {
			payload = new OrganizationNotePayload();
			BeanUtils.copyProperties(organizationNote, payload);

			if (null != organizationNote.getOrganization())
				payload.setOrganizationId(organizationNote.getOrganization().getId());
		}
		return payload;
	}

	/**
	 * Set Organization Pay load
	 * 
	 * @param csv
	 * @return
	 */
	private OrganizationRequestPayload setOrganizationPayload(OrganizationCsvPayload csv) {
		OrganizationRequestPayload payload = new OrganizationRequestPayload();
		AddressPayload address = new AddressPayload();
		BeanUtils.copyProperties(csv, address);
		address.setId(csv.getAddressId());
		payload.setAddress(address);
		BeanUtils.copyProperties(csv, payload);

		// Get Id from naics_code master data Map and assign the id of it
		if (!StringUtils.isEmpty(csv.getNaicsCode()))
			payload.setNaicsCode(naicsMap.get(csv.getNaicsCode()));

		// Get Id from ntee_code master data Map and assign the id of it
		if (!StringUtils.isEmpty(csv.getNteeCode()))
			payload.setNteeCode(nteeMap.get(csv.getNteeCode()));

		return payload;
	}
}
