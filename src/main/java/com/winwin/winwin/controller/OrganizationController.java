package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
import com.winwin.winwin.exception.DataSetCategoryException;
import com.winwin.winwin.exception.DataSetException;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.exception.RegionServedException;
import com.winwin.winwin.exception.ResourceCategoryException;
import com.winwin.winwin.exception.ResourceException;
import com.winwin.winwin.exception.SdgDataException;
import com.winwin.winwin.exception.SpiDataException;
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
import com.winwin.winwin.payload.ProgramRequestPayload;
import com.winwin.winwin.payload.ProgramResponsePayload;
import com.winwin.winwin.payload.RegionMasterFilterPayload;
import com.winwin.winwin.payload.RegionMasterPayload;
import com.winwin.winwin.payload.ResourceCategoryPayLoad;
import com.winwin.winwin.payload.SdgGoalPayload;
import com.winwin.winwin.payload.SpiDataDimensionsPayload;
import com.winwin.winwin.payload.SubOrganizationPayload;
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
import com.winwin.winwin.service.SpiDataService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CsvUtils;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 *
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
	OrganizationDataSetRepository organizationDataSetRepository;

	@Autowired
	private OrganizationResourceService organizationResourceService;

	@Autowired
	private OrganizationResourceRepository organizationResourceRepository;

	@Autowired
	private OrganizationRegionServedService orgRegionServedService;

	@Autowired
	OrgSpiDataService orgSpiDataService;

	@Autowired
	OrgSdgDataService orgSdgDataService;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationNoteService organizationNoteService;

	@Autowired
	OrganizationNoteRepository organizationNoteRepository;

	@Autowired
	OrgNaicsDataService naicsDataService;
	@Autowired
	OrgNteeDataService nteeDataService;

	@Autowired
	ProgramService programService;
	@Autowired
	ProgramRepository programRepository;

	@Autowired
	SpiDataService spiDataService;
	@Autowired
	SdgDataService sdgDataService;

	@Autowired
	NteeDataRepository nteeDataRepository;

	@Autowired
	NaicsDataRepository naicsDataRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationController.class);

	// Code for organization start
	@RequestMapping(value = "", method = RequestMethod.POST)
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

	@RequestMapping(value = "/addAll", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrganizations(@RequestParam("file") MultipartFile file) {
		List<OrganizationRequestPayload> organizationPayloadList = new ArrayList<>();
		List<Organization> organizationList = null;
		List<OrganizationResponsePayload> payloadList = null;
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != file) {
			List<OrganizationCsvPayload> organizationCsvPayload = CsvUtils.read(OrganizationCsvPayload.class, file);
			organizationPayloadList = organizationCsvPayload.stream().map(this::setOrganizationPayload)
					.collect(Collectors.toList());
			organizationList = organizationService.createOrganizations(organizationPayloadList, exceptionResponse);
			payloadList = setOrganizationPayload(organizationList);

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		} else {
			return sendErrorResponse("org.file.null");
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/updateAll", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateOrganizations(@RequestParam("file") MultipartFile file) {
		List<OrganizationRequestPayload> organizationPayloadList = new ArrayList<>();
		List<Organization> organizationList = null;
		List<OrganizationResponsePayload> payloadList = new ArrayList<>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != file) {
			List<OrganizationCsvPayload> organizationCsvPayload = CsvUtils.read(OrganizationCsvPayload.class, file);
			organizationPayloadList = organizationCsvPayload.stream().map(this::setOrganizationPayload)
					.collect(Collectors.toList());
			organizationList = organizationService.updateOrganizations(organizationPayloadList, exceptionResponse);
			payloadList = setOrganizationPayload(organizationList);

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

		} else {
			return sendErrorResponse("org.file.null");
		}

		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> deleteOrganization(@RequestBody OrganizationRequestPayload organizationPayLoad) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (null != organizationPayLoad && null != organizationPayLoad.getId()) {
			try {
				Long id = organizationPayLoad.getId();
				Organization organization = organizationRepository.findOrgById(id);
				if (organization == null) {
					return sendMsgResponse(customMessageSource.getMessage("org.error.not_found"),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
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
	 * @param httpServletResponse
	 * @param organizationPayload
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.PUT)
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
					return sendMsgResponse(customMessageSource.getMessage("org.error.not_found"),
							HttpStatus.INTERNAL_SERVER_ERROR);
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

	@RequestMapping(value = "", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrganizationList(OrganizationFilterPayload filterPayload) throws OrganizationException {
		List<OrganizationResponsePayload> payloadList = new ArrayList<>();
		OrganizationResponsePayload payload = null;
		List<Organization> orgList = null;
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		try {
			if (null != filterPayload) {
				LOGGER.info("getting organization list");
				orgList = organizationService.getOrganizationList(filterPayload, exceptionResponse);
				filterPayload.setOrgCount(organizationService.getOrgCounts(filterPayload, exceptionResponse));
			}

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			if (orgList == null) {
				return sendMsgResponse(customMessageSource.getMessage("org.error.not_found"),
						HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				for (Organization organization : orgList) {
					payload = setOrganizationPayload(organization);
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

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgDetails(@PathVariable("id") Long id) {
		Organization organization = null;
		OrganizationResponsePayload payload = null;
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		try {
			organization = organizationRepository.findOrgById(id);

			if (organization == null) {
				return sendMsgResponse(customMessageSource.getMessage("org.error.not_found"),
						HttpStatus.INTERNAL_SERVER_ERROR);
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
		for (int i = 0; i < organizationList.size(); i++)
			payload.add(setOrganizationPayload(organizationList.get(i)));
		return payload;
	}

	private OrganizationResponsePayload setOrganizationPayload(Organization organization) {
		AddressPayload addressPayload;
		OrganizationResponsePayload payload = null;
		if (null != organization) {
			payload = new OrganizationResponsePayload();
			BeanUtils.copyProperties(organization, payload);
			if (null != organization.getAddress()) {
				addressPayload = new AddressPayload();
				BeanUtils.copyProperties(organization.getAddress(), addressPayload);
				payload.setAddress(addressPayload);
			}
		}
		return payload;
	}
	// Code for organization end

	// Code for organization data set start
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrganizationDataSet(@RequestBody DataSetPayload orgDataSetPayLoad)
			throws DataSetException {
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
			throw new DataSetException(
					customMessageSource.getMessage("org.dataset.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateOrganizationDataSet(@RequestBody DataSetPayload organizationDataSetPayLoad)
			throws DataSetException {
		OrganizationDataSet dataSet = null;
		DataSetCategory category = null;
		DataSetCategoryPayload payloadCategory = null;
		DataSetPayload payload = null;
		if (null != organizationDataSetPayLoad && null != organizationDataSetPayLoad.getId()) {
			Long id = organizationDataSetPayLoad.getId();
			dataSet = organizationDataSetRepository.findOrgDataSetById(id);
			if (dataSet == null) {
				throw new DataSetException(customMessageSource.getMessage("org.dataset.error.not_found"));
			} else {
				payload = new DataSetPayload();
				dataSet = organizationDataSetService.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
				BeanUtils.copyProperties(dataSet, payload);

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

	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> deleteOrganizationDataSet(@RequestBody DataSetPayload orgDataSetPayLoad)
			throws DataSetException {
		try {
			if (null != orgDataSetPayLoad && null != orgDataSetPayLoad.getId()) {
				Long id = orgDataSetPayLoad.getId();
				OrganizationDataSet dataSet = organizationDataSetRepository.findOrgDataSetById(id);

				if (dataSet == null) {
					throw new DataSetException(customMessageSource.getMessage("org.dataset.error.not_found"));
				}
				organizationDataSetService.removeOrganizationDataSet(id);
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			throw new DataSetException(
					customMessageSource.getMessage("org.dataset.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("org.dataset.success.deleted");
	}

	@RequestMapping(value = "{id}/datasets", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrganizationDataSetList(@PathVariable("id") Long id) throws DataSetException {
		List<OrganizationDataSet> orgDataSetList = null;
		DataSetPayload payload = null;
		DataSetCategory category = null;
		DataSetCategoryPayload payloadCategory = null;
		List<DataSetPayload> payloadList = null;
		try {
			orgDataSetList = organizationDataSetService.getOrganizationDataSetList(id);
			if (orgDataSetList == null) {
				throw new DataSetException(customMessageSource.getMessage("org.dataset.error.not_found"));
			} else {
				payloadList = new ArrayList<DataSetPayload>();
				for (OrganizationDataSet dataSet : orgDataSetList) {
					payload = new DataSetPayload();
					payload.setId(dataSet.getId());
					category = dataSet.getDataSetCategory();
					if (null != category) {
						payloadCategory = new DataSetCategoryPayload();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
						payloadCategory.setAdminUrl(category.getAdminUrl());
					}
					payload.setDataSetCategory(payloadCategory);
					payload.setOrganizationId(dataSet.getOrganizationId());
					payload.setDescription(dataSet.getDescription());
					payload.setType(dataSet.getType());
					payload.setUrl(dataSet.getUrl());
					payload.setAdminUrl(dataSet.getAdminUrl());
					payload.setIsActive(dataSet.getIsActive());
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			throw new DataSetException(
					customMessageSource.getMessage("org.dataset.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/{id}/dataset/categorylist", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> getOrganizationDataSetCategoryList(HttpServletResponse httpServletResponce)
			throws DataSetCategoryException {
		List<DataSetCategory> orgDataSetCategoryList = null;
		List<DataSetCategoryPayload> payloadList = null;
		DataSetCategoryPayload payload = null;
		try {
			orgDataSetCategoryList = organizationDataSetService.getDataSetCategoryList();
			if (orgDataSetCategoryList == null) {
				throw new DataSetCategoryException(
						customMessageSource.getMessage("org.dataset.category.error.not_found"));
			} else {
				payloadList = new ArrayList<>();
				for (DataSetCategory category : orgDataSetCategoryList) {
					payload = new DataSetCategoryPayload();
					payload.setId(category.getId());
					payload.setCategoryName(category.getCategoryName());
					payload.setAdminUrl(category.getAdminUrl());
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			throw new DataSetCategoryException(
					customMessageSource.getMessage("org.dataset.category.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);
	}

	// Code for organization data set end

	// Code for organization resource start
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> createOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayload organizationResourcePayLoad) throws ResourceException {
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
						payloadCategory.setAdminUrl(category.getAdminUrl());
					}
					payload.setAdminUrl(organizationResource.getAdminUrl());
					payload.setResourceCategory(payloadCategory);
					payload.setOrganizationId(organizationResource.getOrganizationId());
					payload.setCount(organizationResource.getCount());
					payload.setDescription(organizationResource.getDescription());
					payload.setIsActive(organizationResource.getIsActive());
				}
			} catch (Exception e) {
				throw new ResourceException(
						customMessageSource.getMessage("org.resource.error.created") + ": " + e.getMessage());
			}
		} else {
			return sendErrorResponse("org.bad.request");
		}
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "/{id}/resource", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> updateOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayload organizationResourcePayLoad) throws ResourceException {
		OrganizationResource organizationResource = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		OrganizationResourcePayload payload = null;
		try {
			if (null != organizationResourcePayLoad && null != organizationResourcePayLoad.getId()) {
				Long id = organizationResourcePayLoad.getId();
				organizationResource = organizationResourceRepository.findOrgResourceById(id);
				if (organizationResource == null) {
					throw new ResourceException(customMessageSource.getMessage("org.resource.error.not_found"));
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
						payloadCategory.setAdminUrl(category.getAdminUrl());
					}
					payload.setResourceCategory(payloadCategory);
					payload.setAdminUrl(organizationResource.getAdminUrl());
					payload.setOrganizationId(organizationResource.getOrganizationId());
					payload.setCount(organizationResource.getCount());
					payload.setDescription(organizationResource.getDescription());
					payload.setIsActive(organizationResource.getIsActive());
				}
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			throw new ResourceException(
					customMessageSource.getMessage("org.resource.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "/{id}/resource", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> deleteOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayload organizationResourcePayLoad) throws ResourceException {
		try {
			if (null != organizationResourcePayLoad && null != organizationResourcePayLoad.getId()) {
				Long id = organizationResourcePayLoad.getId();
				OrganizationResource organizationResource = organizationResourceRepository.findOrgResourceById(id);
				if (organizationResource == null) {
					throw new ResourceException(customMessageSource.getMessage("org.resource.error.not_found"));
				}
				organizationResourceService.removeOrganizationResource(id);
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			throw new ResourceException(
					customMessageSource.getMessage("org.resource.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("org.resource.success.deleted");
	}

	@RequestMapping(value = "/{id}/resources", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrganizationResourceList(@PathVariable("id") Long id) throws ResourceException {
		List<OrganizationResource> orgResourceList = null;
		OrganizationResourcePayload payload = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		List<OrganizationResourcePayload> payloadList = null;
		try {
			orgResourceList = organizationResourceService.getOrganizationResourceList(id);
			if (orgResourceList == null) {
				throw new ResourceException(customMessageSource.getMessage("org.resource.error.not_found"));
			} else {
				payloadList = new ArrayList<OrganizationResourcePayload>();
				for (OrganizationResource resource : orgResourceList) {
					payload = new OrganizationResourcePayload();
					BeanUtils.copyProperties(resource, payload);
					category = resource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						BeanUtils.copyProperties(category, payloadCategory);
					}
					payload.setResourceCategory(payloadCategory);
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			throw new ResourceException(
					customMessageSource.getMessage("org.resource.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/{id}/resource/categorylist", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> getOrganizationResourceCategoryList(HttpServletResponse httpServletResponce)
			throws ResourceCategoryException {
		List<ResourceCategory> orgResourceCategoryList = null;
		ResourceCategoryPayLoad payload = null;
		List<ResourceCategoryPayLoad> payloadList = null;
		try {
			orgResourceCategoryList = organizationResourceService.getResourceCategoryList();
			if (orgResourceCategoryList == null) {
				throw new ResourceCategoryException(
						customMessageSource.getMessage("org.resource.category.error.not_found"));
			} else {
				payloadList = new ArrayList<>();
				for (ResourceCategory category : orgResourceCategoryList) {
					payload = new ResourceCategoryPayLoad();
					BeanUtils.copyProperties(category, payload);
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			throw new ResourceCategoryException(
					customMessageSource.getMessage("org.resource.category.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for organization resource end

	// Code for organization region served start
	@RequestMapping(value = "/{id}/region", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgRegions(
			@RequestBody List<OrganizationRegionServedPayload> orgRegionServedPayloadList)
			throws RegionServedException {
		List<OrganizationRegionServed> orgRegionServedList = null;
		List<OrganizationRegionServedPayload> payloadList = null;
		OrganizationRegionServedPayload payload = null;
		try {
			orgRegionServedList = orgRegionServedService.createOrgRegionServed(orgRegionServedPayloadList);
			if (null != orgRegionServedList) {
				payloadList = new ArrayList<OrganizationRegionServedPayload>();
				for (OrganizationRegionServed region : orgRegionServedList) {
					payload = new OrganizationRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						RegionMasterPayload regionMasterPayload = new RegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());
						regionMasterPayload.setAdminUrl(region.getRegionMaster().getAdminUrl());
						payload.setRegion(regionMasterPayload);
					}
					payload.setOrganizationId(region.getOrgId());
					payload.setIsActive(region.getIsActive());
					payload.setAdminUrl(region.getAdminUrl());
					payloadList.add(payload);

				}
			}
		} catch (Exception e) {
			throw new RegionServedException(
					customMessageSource.getMessage("org.region.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/{id}/regions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgRegionsList(@PathVariable Long id) throws RegionServedException {
		List<OrganizationRegionServed> orgRegionList = null;
		OrganizationRegionServedPayload payload = null;
		List<OrganizationRegionServedPayload> payloadList = null;
		try {
			orgRegionList = orgRegionServedService.getOrgRegionServedList(id);
			if (orgRegionList == null) {
				throw new RegionServedException(customMessageSource.getMessage("org.region.error.not_found"));
			} else {
				payloadList = new ArrayList<OrganizationRegionServedPayload>();
				for (OrganizationRegionServed region : orgRegionList) {
					payload = new OrganizationRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						RegionMasterPayload regionMasterPayload = new RegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());
						regionMasterPayload.setAdminUrl(region.getRegionMaster().getAdminUrl());

						payload.setRegion(regionMasterPayload);
					}
					payload.setOrganizationId(region.getOrgId());
					payload.setIsActive(region.getIsActive());
					payload.setAdminUrl(region.getAdminUrl());

					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			throw new RegionServedException(customMessageSource.getMessage("org.region.error.list"));
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/{id}/regionmasters", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgRegionsMasterList(RegionMasterFilterPayload filterPayload)
			throws RegionServedException {
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

				if (orgRegionMasterList == null) {
					return sendMsgResponse(customMessageSource.getMessage("org.region.error.not_found"),
							HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					payloadList = new ArrayList<RegionMasterPayload>();
					for (RegionMaster region : orgRegionMasterList) {
						payload = new RegionMasterPayload();
						payload.setRegionId(region.getId());
						payload.setRegionName(region.getRegionName());
						payload.setAdminUrl(region.getAdminUrl());
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
		return sendSuccessResponse(payloadList);
	}

	// Code for organization region served end

	// Code for organization SPI data start
	@RequestMapping(value = "/{id}/spidata", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgSpiDataList() throws SpiDataException {
		List<SpiDataDimensionsPayload> payloadList = new ArrayList<SpiDataDimensionsPayload>();
		try {
			payloadList = spiDataService.getSpiDataForResponse();
			if (payloadList == null) {
				throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.not_found"));
			}
		} catch (Exception e) {
			throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.list"));
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/{id}/spidata", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgSpiDataMapping(@RequestBody List<OrganizationSpiDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) throws SpiDataException {
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		if (null != payloadList) {
			try {
				orgSpiDataService.createSpiDataMapping(payloadList, orgId);
			} catch (Exception e) {
				throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.created"));
			}
			return sendSuccessResponse("org.spidata.success.created");
		} else {
			return sendErrorResponse("org.bad.request");
		}
	}

	@RequestMapping(value = "/{id}/spidata/selected", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSpiData(@PathVariable("id") Long orgId) throws SpiDataException {
		List<OrganizationSpiDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			payloadList = orgSpiDataService.getSelectedSpiData(orgId);

			if (payloadList == null) {
				throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.not_found"));
			}
		} catch (Exception e) {
			throw new SpiDataException(customMessageSource.getMessage("org.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(payloadList);

	}// Code for organization SPI data end

	// Code for organization SDG data start
	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgSdgDataList() throws SdgDataException {
		List<SdgGoalPayload> payloadList = new ArrayList<SdgGoalPayload>();
		try {
			payloadList = sdgDataService.getSdgDataForResponse();
			if (payloadList == null) {
				throw new SdgDataException(customMessageSource.getMessage("org.sdgdata.error.not_found"));
			}
		} catch (Exception e) {
			throw new SdgDataException(customMessageSource.getMessage("org.sdgdata.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgSdgDataMapping(@RequestBody List<OrganizationSdgDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) throws SdgDataException {
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		if (null != payloadList) {
			try {
				orgSdgDataService.createSdgDataMapping(payloadList, orgId);
			} catch (Exception e) {
				throw new SdgDataException(customMessageSource.getMessage("org.sdgdata.error.created"));
			}
			return sendSuccessResponse("org.sdgdata.success.created");

		} else {
			return sendErrorResponse("org.bad.request");
		}
	}

	@RequestMapping(value = "/{id}/sdgdata/selected", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSdgData(@PathVariable("id") Long orgId) throws SdgDataException {
		List<OrganizationSdgDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			payloadList = orgSdgDataService.getSelectedSdgData(orgId);

			if (payloadList == null) {
				throw new SdgDataException(customMessageSource.getMessage("org.sdgdata.error.not_found"));
			}
		} catch (Exception e) {
			throw new SdgDataException(customMessageSource.getMessage("org.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(payloadList);

	}// Code for organization SDG data end

	// Code for organization Program Details start
	@RequestMapping(value = "/{id}/program", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createProgram(@RequestBody ProgramRequestPayload programPayload,
			@PathVariable("id") Long orgId) {
		Program program = null;
		ProgramResponsePayload payload = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.program.null"));
		try {
			program = programService.createProgram(programPayload);
			payload = programService.getProgramResponseFromProgram(program);

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("prg.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "/{id}/program", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getProgramList(@PathVariable("id") Long orgId, OrganizationFilterPayload filterPayload)
			throws OrganizationException {
		List<Program> prgList = null;
		List<ProgramResponsePayload> payloadList = new ArrayList<>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			prgList = programService.getProgramList(filterPayload, orgId, exceptionResponse);
			if (prgList == null) {
				return sendMsgResponse(customMessageSource.getMessage("prg.error.not_found"),
						HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
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

	@RequestMapping(value = "/{id}/program", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> deleteProgram(@RequestBody ProgramRequestPayload programPayLoad) {
		try {
			if (null != programPayLoad && null != programPayLoad.getId()) {
				Long id = programPayLoad.getId();
				Program program = programRepository.findProgramById(id);
				if (program == null) {
					throw new OrganizationException(customMessageSource.getMessage("prg.error.not_found"));
				}
				programService.deleteProgram(id);
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("prg.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("prg.success.deleted");
	}

	/**
	 * @param httpServletResponse
	 * @param organizationPayload
	 * @return
	 */
	@RequestMapping(value = "/{id}/program", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateProgramDetails(@RequestBody List<ProgramRequestPayload> prgPayloadList) {
		Program program = null;
		List<ProgramResponsePayload> payloadList = new ArrayList<>();
		try {
			for (ProgramRequestPayload payload : prgPayloadList) {
				program = programRepository.findProgramById(payload.getId());
				if (program == null) {
					throw new OrganizationException(customMessageSource.getMessage("prg.error.not_found"));
				} else {
					program = programService.updateProgram(payload);
					ProgramResponsePayload responsePayload = programService.getProgramResponseFromProgram(program);
					payloadList.add(responsePayload);
				}
			}
		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("prg.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}
	// Code for organization Program Details end

	// Code for organization Chart start
	@RequestMapping(value = "/{id}/suborganization", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSuborganizationList(@PathVariable("id") Long orgId) throws OrganizationException {
		Organization organization = null;
		OrganizationChartPayload payload = new OrganizationChartPayload();
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			organization = organizationRepository.findOrgById(orgId);
			if (organization == null) {
				return sendErrorResponse("org.bad.request");
			} else {
				payload = organizationService.getOrgCharts(organization, orgId);
			}
		} catch (Exception e) {
			throw new OrganizationException(customMessageSource.getMessage("org.chart.error"));
		}
		return sendSuccessResponse(payload);

	}

	@RequestMapping(value = "/{id}/suborganization", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createSuborganization(@RequestBody SubOrganizationPayload subOrganizationPayload) {
		OrganizationDivisionPayload payload = null;
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
			throw new OrganizationException(
					customMessageSource.getMessage("org.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}
	// Code for organization Chart end

	// Code for organization notes start
	@RequestMapping(value = "/{id}/notes", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> createOrgNote(@RequestBody OrganizationNotePayload organizationNotePayload,
			@PathVariable("id") Long orgId) {
		OrganizationNote note = null;
		OrganizationNotePayload payload = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			note = organizationNoteService.createOrganizationNote(organizationNotePayload);
			payload = setOrganizationNotePayload(note, payload);
		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.note.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "/{id}/notes", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getorgNotesList(@PathVariable("id") Long orgId) throws OrganizationException {
		List<OrganizationNote> orgNoteList = null;
		OrganizationNotePayload payload = new OrganizationNotePayload();
		List<OrganizationNotePayload> payloadList = new ArrayList<OrganizationNotePayload>();
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			orgNoteList = organizationNoteRepository.findAllOrgNotesList(orgId);
			if (orgNoteList == null) {
				throw new OrganizationException(customMessageSource.getMessage("org.note.error.not_found"));
			} else {
				for (OrganizationNote orgNote : orgNoteList) {
					payload = setOrganizationNotePayload(orgNote, payload);
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			throw new OrganizationException(customMessageSource.getMessage("org.note.error.list"));
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * @param httpServletResponse
	 * @param organizationPayload
	 * @return
	 */
	@RequestMapping(value = "/{id}/notes", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateOrgNote(@RequestBody OrganizationNotePayload organizationNotePayload,
			@PathVariable("id") Long orgId) {
		OrganizationNote note = null;
		OrganizationNotePayload payload = null;
		if (orgId == null || organizationNotePayload == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			note = organizationNoteService.updateOrganizationNote(organizationNotePayload);
			payload = setOrganizationNotePayload(note, payload);
		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.note.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "/{id}/notes", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> deleteOrgNote(@RequestBody OrganizationNotePayload organizationNotePayLoad) {
		try {
			if (null != organizationNotePayLoad && null != organizationNotePayLoad.getId()) {
				Long noteId = organizationNotePayLoad.getId();
				OrganizationNote note = organizationNoteRepository.findOrgNoteById(noteId);
				if (note == null) {
					throw new OrganizationException(customMessageSource.getMessage("org.note.error.not_found"));
				}
				organizationNoteService.removeOrganizationNote(noteId, organizationNotePayLoad.getOrganizationId());
			} else {
				return sendErrorResponse("org.bad.request");
			}

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.note.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("org.note.success.deleted");
	}

	@RequestMapping(value = "/{id}/history", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgHistoy(@PathVariable("id") Long orgId) {
		List<OrganizationHistoryPayload> payloadList = null;
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

	@RequestMapping(value = "/naics_data", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgNaicsData(@RequestParam(name = "search", required = false) String search) {
		List<NaicsData> payloadList = null;
		try {
			payloadList = naicsDataService.getAllOrgNaicsData();
		} catch (Exception e) {
			return sendExceptionResponse(e, "org.history.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/ntee_data", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgNteeData(@RequestParam(name = "search", required = false) String search) {
		List<NteeData> payloadList = null;
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
		}
		return payload;
	}

	private OrganizationRequestPayload setOrganizationPayload(OrganizationCsvPayload csv) {
		OrganizationRequestPayload payload = new OrganizationRequestPayload();
		AddressPayload address = new AddressPayload();
		BeanUtils.copyProperties(csv, address);
		payload.setAddress(address);
		BeanUtils.copyProperties(csv, payload);

		// Get Id from naics_code master data table and assign the id of it
		NaicsData naicsData = naicsDataRepository.findByCode(csv.getNaicsCode());
		payload.setNaicsCode(naicsData.getId());

		// Get Id from ntee_code master data table and assign the id of it
		NteeData nteeData = nteeDataRepository.findByCode(csv.getNteeCode());
		payload.setNteeCode(nteeData.getId());

		return payload;
	}
}
