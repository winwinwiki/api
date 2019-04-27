package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.entity.OrgRegionMaster;
import com.winwin.winwin.entity.OrgRegionServed;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationDataSetCategory;
import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.OrganizationResourceCategory;
import com.winwin.winwin.exception.OrgRegionServedException;
import com.winwin.winwin.exception.OrgSdgDataException;
import com.winwin.winwin.exception.OrgSpiDataException;
import com.winwin.winwin.exception.OrganizationDataSetCategoryException;
import com.winwin.winwin.exception.OrganizationDataSetException;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.exception.OrganizationResourceCategoryException;
import com.winwin.winwin.exception.OrganizationResourceException;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrgRegionMasterPayload;
import com.winwin.winwin.payload.OrgRegionServedPayload;
import com.winwin.winwin.payload.OrgSdgDataMapPayload;
import com.winwin.winwin.payload.OrgSdgGoalPayload;
import com.winwin.winwin.payload.OrgSpiDataDimensionsPayload;
import com.winwin.winwin.payload.OrgSpiDataMapPayload;
import com.winwin.winwin.payload.OrganizationDataSetCategoryPayLoad;
import com.winwin.winwin.payload.OrganizationDataSetPayLoad;
import com.winwin.winwin.payload.OrganizationPayload;
import com.winwin.winwin.payload.OrganizationResourceCategoryPayLoad;
import com.winwin.winwin.payload.OrganizationResourcePayLoad;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.service.OrgRegionServedService;
import com.winwin.winwin.service.OrgSdgDataService;
import com.winwin.winwin.service.OrgSpiDataService;
import com.winwin.winwin.service.OrganizationDataSetService;
import com.winwin.winwin.service.OrganizationResourceService;

/**
 * @author ArvindKhatik
 *
 */
@RestController
@RequestMapping(value = "/program")

public class ProgramController extends BaseController {

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
	private OrgRegionServedService orgRegionServedService;

	@Autowired
	OrgSpiDataService orgSpiDataService;

	@Autowired
	OrgSdgDataService orgSdgDataService;

	// Code for program data set start
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@Transactional
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getProgramDetails(@PathVariable("id") Long id) {
		Organization organization = null;
		OrganizationPayload payload = null;
		try {
			organization = organizationRepository.findOrgById(id);
			if (organization == null) {
				throw new OrganizationException(customMessageSource.getMessage("prg.error.not_found"));
			} else {
				payload = setProgramPayload(organization, payload);

			}

		} catch (Exception e) {
			throw new OrganizationException(customMessageSource.getMessage("prg.error.fetch") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);

	}

	/**
	 * @param organization
	 * @param payload
	 * @return
	 */
	private OrganizationPayload setProgramPayload(Organization organization, OrganizationPayload payload) {
		AddressPayload addressPayload;
		if (null != organization) {
			payload = new OrganizationPayload();
			payload.setId(organization.getId());
			if (null != organization.getAddress()) {
				addressPayload = new AddressPayload();
				addressPayload.setId(organization.getAddress().getId());
				addressPayload.setCountry(organization.getAddress().getCountry());
				addressPayload.setState(organization.getAddress().getState());
				addressPayload.setCity(organization.getAddress().getCity());
				addressPayload.setCounty(organization.getAddress().getCounty());
				addressPayload.setZip(organization.getAddress().getZip());
				addressPayload.setStreet(organization.getAddress().getStreet());
				addressPayload.setPlaceId(organization.getAddress().getPlaceId());
				payload.setAddress(addressPayload);
			}
			payload.setName(organization.getName());
			payload.setRevenue(organization.getRevenue());
			payload.setAssets(organization.getAssets());
			payload.setSector(organization.getSector());
			payload.setSectorLevel(organization.getSectorLevel());
			payload.setSectorLevelName(organization.getSectorLevelName());
			payload.setDescription(organization.getDescription());
			payload.setNaicsCode(organization.getNaicsCode());
			payload.setNteeCode(organization.getNteeCode());
			payload.setPriority(organization.getPriority());
			payload.setParentId(organization.getParentId());
			payload.setIsActive(organization.getIsActive());
			payload.setTagStatus(organization.getTagStatus());
			payload.setTotalAssets(organization.getAssets());
			payload.setWebsiteUrl(organization.getWebsiteUrl());
			payload.setFacebookUrl(organization.getFacebookUrl());
			payload.setLinkedinUrl(organization.getLinkedinUrl());
			payload.setTwitterUrl(organization.getTwitterUrl());
			payload.setValues(organization.getValues());
			payload.setPurpose(organization.getPurpose());
			payload.setSelfInterest(organization.getSelfInterest());
			payload.setBusinessModel(organization.getBusinessModel());
			payload.setMissionStatement(organization.getMissionStatement());
			payload.setContactInfo(organization.getContactInfo());
			payload.setPopulationServed(organization.getPopulationServed());
			payload.setTagStatus(organization.getTagStatus());

		}
		return payload;
	}

	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createProgrmDataSet(
			@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
		OrganizationDataSet organizationDataSet = null;
		OrganizationDataSetPayLoad payload = null;
		OrganizationDataSetCategory category = null;
		OrganizationDataSetCategoryPayLoad payloadCategory = null;
		if (null != organizationDataSetPayLoad) {
			try {
				organizationDataSet = organizationDataSetService
						.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
				if (null != organizationDataSet) {
					payload = new OrganizationDataSetPayLoad();
					payload.setId(organizationDataSet.getId());
					category = organizationDataSet.getOrganizationDataSetCategory();
					if (null != category) {
						payloadCategory = new OrganizationDataSetCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
					}
					payload.setOrganizationDataSetCategory(payloadCategory);
					payload.setOrganizationId(organizationDataSet.getOrganizationId());
					payload.setDescription(organizationDataSet.getDescription());
					payload.setType(organizationDataSet.getType());
					payload.setUrl(organizationDataSet.getUrl());
					payload.setIsActive(organizationDataSet.getIsActive());
				}

			} catch (Exception e) {
				throw new OrganizationDataSetException(
						customMessageSource.getMessage("prog.dataset.error.created") + ": " + e.getMessage());
			}

		} else {
			return sendErrorResponse("org.bad.request");

		}

		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateProgramDataSet(
			@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
		OrganizationDataSet dataSet = null;
		OrganizationDataSetCategory category = null;
		OrganizationDataSetCategoryPayLoad payloadCategory = null;
		OrganizationDataSetPayLoad payload = null;
		if (null != organizationDataSetPayLoad && null != organizationDataSetPayLoad.getId()) {
			Long id = organizationDataSetPayLoad.getId();
			dataSet = organizationDataSetRepository.findOrgDataSetById(id);
			if (dataSet == null) {
				throw new OrganizationDataSetException(customMessageSource.getMessage("prog.dataset.error.not_found"));
			} else {
				payload = new OrganizationDataSetPayLoad();
				dataSet = organizationDataSetService.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
				payload.setId(dataSet.getId());
				category = dataSet.getOrganizationDataSetCategory();
				if (null != category) {
					payloadCategory = new OrganizationDataSetCategoryPayLoad();
					payloadCategory.setId(category.getId());
					payloadCategory.setCategoryName(category.getCategoryName());
				}
				payload.setOrganizationDataSetCategory(payloadCategory);
				payload.setOrganizationId(dataSet.getOrganizationId());
				payload.setDescription(dataSet.getDescription());
				payload.setType(dataSet.getType());
				payload.setUrl(dataSet.getUrl());
				payload.setIsActive(dataSet.getIsActive());
			}
		} else {
			return sendErrorResponse("org.bad.request");

		}

		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> deleteProgramDataSet(@RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
		try {
			if (null != organizationDataSetPayLoad && null != organizationDataSetPayLoad.getId()) {
				Long id = organizationDataSetPayLoad.getId();
				OrganizationDataSet dataSet = organizationDataSetRepository.findOrgDataSetById(id);
				if (dataSet == null) {
					throw new OrganizationDataSetException(
							customMessageSource.getMessage("prog.dataset.error.not_found"));
				}
				// organizationDataSetService.removeOrganizationDataSet(id);
			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationDataSetException(
					customMessageSource.getMessage("prog.dataset.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("prog.dataset.success.deleted");
	}

	@RequestMapping(value = "{id}/datasets", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getProgramDataSetList(@PathVariable("id") Long id) throws OrganizationDataSetException {
		List<OrganizationDataSet> orgDataSetList = null;
		OrganizationDataSetPayLoad payload = null;
		OrganizationDataSetCategory category = null;
		OrganizationDataSetCategoryPayLoad payloadCategory = null;
		List<OrganizationDataSetPayLoad> payloadList = null;
		try {
			orgDataSetList = organizationDataSetService.getOrganizationDataSetList(id);
			if (orgDataSetList == null) {
				throw new OrganizationDataSetException(customMessageSource.getMessage("prog.dataset.error.not_found"));
			} else {
				payloadList = new ArrayList<OrganizationDataSetPayLoad>();
				for (OrganizationDataSet dataSet : orgDataSetList) {
					payload = new OrganizationDataSetPayLoad();
					payload.setId(dataSet.getId());
					category = dataSet.getOrganizationDataSetCategory();
					if (null != category) {
						payloadCategory = new OrganizationDataSetCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
					}
					payload.setOrganizationDataSetCategory(payloadCategory);
					payload.setOrganizationId(dataSet.getOrganizationId());
					payload.setDescription(dataSet.getDescription());
					payload.setType(dataSet.getType());
					payload.setUrl(dataSet.getUrl());
					payload.setIsActive(dataSet.getIsActive());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new OrganizationDataSetException(
					customMessageSource.getMessage("prog.dataset.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/dataset/categorylist", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrganizationDataSetCategoryList(HttpServletResponse httpServletResponce)
			throws OrganizationDataSetCategoryException {
		List<OrganizationDataSetCategory> orgDataSetCategoryList = null;
		List<OrganizationDataSetCategoryPayLoad> payloadList = null;
		OrganizationDataSetCategoryPayLoad payload = null;
		try {
			orgDataSetCategoryList = organizationDataSetService.getOrganizationDataSetCategoryList();
			if (orgDataSetCategoryList == null) {
				throw new OrganizationDataSetCategoryException(
						customMessageSource.getMessage("prog.dataset.category.error.not_found"));
			} else {
				payloadList = new ArrayList<OrganizationDataSetCategoryPayLoad>();
				for (OrganizationDataSetCategory category : orgDataSetCategoryList) {
					payload = new OrganizationDataSetCategoryPayLoad();
					payload.setId(category.getId());
					payload.setCategoryName(category.getCategoryName());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new OrganizationDataSetCategoryException(
					customMessageSource.getMessage("prog.dataset.category.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for program data set end

	// Code for program resource start
	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayLoad organizationResourcePayLoad)
			throws OrganizationResourceException {
		OrganizationResource organizationResource = null;
		OrganizationResourcePayLoad payload = null;
		OrganizationResourceCategory category = null;
		OrganizationResourceCategoryPayLoad payloadCategory = null;
		if (null != organizationResourcePayLoad) {
			try {
				organizationResource = organizationResourceService
						.createOrUpdateOrganizationResource(organizationResourcePayLoad);
				if (null != organizationResource) {
					payload = new OrganizationResourcePayLoad();
					payload.setId(organizationResource.getId());
					category = organizationResource.getOrganizationResourceCategory();
					if (null != category) {
						payloadCategory = new OrganizationResourceCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
					}
					payload.setOrganizationResourceCategory(payloadCategory);
					payload.setOrganizationId(organizationResource.getOrganizationId());
					payload.setCount(organizationResource.getCount());
					payload.setDescription(organizationResource.getDescription());
					payload.setIsActive(organizationResource.getIsActive());
				}
			} catch (Exception e) {
				throw new OrganizationResourceException(
						customMessageSource.getMessage("prog.resource.error.created") + ": " + e.getMessage());
			}

		} else {
			return sendErrorResponse("org.bad.request");

		}
		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayLoad organizationResourcePayLoad)
			throws OrganizationResourceException {
		OrganizationResource organizationResource = null;
		OrganizationResourceCategory category = null;
		OrganizationResourceCategoryPayLoad payloadCategory = null;
		OrganizationResourcePayLoad payload = null;
		try {
			if (null != organizationResourcePayLoad && null != organizationResourcePayLoad.getId()) {
				Long id = organizationResourcePayLoad.getId();
				organizationResource = organizationResourceRepository.findOrgResourceById(id);
				if (organizationResource == null) {
					throw new OrganizationResourceException(
							customMessageSource.getMessage("prog.resource.error.not_found"));
				} else {
					organizationResource = organizationResourceService
							.createOrUpdateOrganizationResource(organizationResourcePayLoad);
					payload = new OrganizationResourcePayLoad();
					payload.setId(organizationResource.getId());
					category = organizationResource.getOrganizationResourceCategory();
					if (null != category) {
						payloadCategory = new OrganizationResourceCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
					}
					payload.setOrganizationResourceCategory(payloadCategory);
					payload.setOrganizationId(organizationResource.getOrganizationId());
					payload.setCount(organizationResource.getCount());
					payload.setDescription(organizationResource.getDescription());
					payload.setIsActive(organizationResource.getIsActive());
				}

			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationResourceException(
					customMessageSource.getMessage("prog.resource.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> deleteOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayLoad organizationResourcePayLoad)
			throws OrganizationResourceException {
		try {
			if (null != organizationResourcePayLoad && null != organizationResourcePayLoad.getId()) {
				Long id = organizationResourcePayLoad.getId();
				OrganizationResource organizationResource = organizationResourceRepository.findOrgResourceById(id);
				if (organizationResource == null) {
					throw new OrganizationResourceException(
							customMessageSource.getMessage("prog.resource.error.not_found"));
				}
				organizationResourceService.removeOrganizationResource(id);
			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationResourceException(
					customMessageSource.getMessage("prog.resource.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("prog.resource.success.deleted");
	}

	@RequestMapping(value = "/{id}/resources", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrganizationResourceList(@PathVariable("id") Long id)
			throws OrganizationResourceException {
		List<OrganizationResource> orgResourceList = null;
		OrganizationResourcePayLoad payload = null;
		OrganizationResourceCategory category = null;
		OrganizationResourceCategoryPayLoad payloadCategory = null;
		List<OrganizationResourcePayLoad> payloadList = null;
		try {
			orgResourceList = organizationResourceService.getOrganizationResourceList(id);
			if (orgResourceList == null) {
				throw new OrganizationResourceException(
						customMessageSource.getMessage("prog.resource.error.not_found"));
			} else {
				payloadList = new ArrayList<OrganizationResourcePayLoad>();
				for (OrganizationResource resource : orgResourceList) {
					payload = new OrganizationResourcePayLoad();
					payload.setId(resource.getId());
					category = resource.getOrganizationResourceCategory();
					if (null != category) {
						payloadCategory = new OrganizationResourceCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
					}
					payload.setOrganizationResourceCategory(payloadCategory);
					payload.setOrganizationId(resource.getOrganizationId());
					payload.setCount(resource.getCount());
					payload.setDescription(resource.getDescription());
					payload.setIsActive(resource.getIsActive());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new OrganizationResourceException(
					customMessageSource.getMessage("prog.resource.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/resource/categorylist", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> getOrganizationResourceCategoryList(HttpServletResponse httpServletResponce)
			throws OrganizationResourceCategoryException {
		List<OrganizationResourceCategory> orgResourceCategoryList = null;
		OrganizationResourceCategoryPayLoad payload = null;
		List<OrganizationResourceCategoryPayLoad> payloadList = null;

		try {
			orgResourceCategoryList = organizationResourceService.getOrganizationResourceCategoryList();
			if (orgResourceCategoryList == null) {
				throw new OrganizationResourceCategoryException(
						customMessageSource.getMessage("prog.resource.category.error.not_found"));
			} else {
				payloadList = new ArrayList<OrganizationResourceCategoryPayLoad>();
				for (OrganizationResourceCategory category : orgResourceCategoryList) {
					payload = new OrganizationResourceCategoryPayLoad();
					payload.setId(category.getId());
					payload.setCategoryName(category.getCategoryName());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new OrganizationResourceCategoryException(
					customMessageSource.getMessage("prog.resource.category.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for program resource end

	// Code for program region served start
	@RequestMapping(value = "/{id}/region", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> createOrgRegions(@RequestBody List<OrgRegionServedPayload> orgRegionServedPayloadList)
			throws OrgRegionServedException {
		List<OrgRegionServed> orgRegionServedList = null;
		List<OrgRegionServedPayload> payloadList = null;
		OrgRegionServedPayload payload = null;
		try {
			orgRegionServedList = orgRegionServedService.createOrgRegionServed(orgRegionServedPayloadList);
			if (null != orgRegionServedList) {
				payloadList = new ArrayList<OrgRegionServedPayload>();
				for (OrgRegionServed region : orgRegionServedList) {
					payload = new OrgRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						OrgRegionMasterPayload regionMasterPayload = new OrgRegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());
						payload.setRegion(regionMasterPayload);
					}
					payload.setOrganizationId(region.getOrgId());
					payload.setIsActive(region.getIsActive());
					payloadList.add(payload);

				}
			}
		} catch (Exception e) {
			throw new OrgRegionServedException(
					customMessageSource.getMessage("prog.region.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/{id}/regions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgRegionsList(@PathVariable Long id) throws OrgRegionServedException {
		List<OrgRegionServed> orgRegionList = null;
		OrgRegionServedPayload payload = null;
		List<OrgRegionServedPayload> payloadList = null;
		try {
			orgRegionList = orgRegionServedService.getOrgRegionServedList(id);
			if (orgRegionList == null) {
				throw new OrgRegionServedException(customMessageSource.getMessage("prog.region.error.not_found"));
			} else {
				payloadList = new ArrayList<OrgRegionServedPayload>();
				for (OrgRegionServed region : orgRegionList) {
					payload = new OrgRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						OrgRegionMasterPayload regionMasterPayload = new OrgRegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());
						payload.setRegion(regionMasterPayload);
					}
					payload.setOrganizationId(region.getOrgId());
					payload.setIsActive(region.getIsActive());
					payloadList.add(payload);

				}

			}
		} catch (Exception e) {
			throw new OrgRegionServedException(customMessageSource.getMessage("prog.region.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/regionmasters", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> getOrgRegionsMasterList() throws OrgRegionServedException {
		List<OrgRegionMaster> orgRegionMasterList = null;
		OrgRegionMasterPayload payload = null;
		List<OrgRegionMasterPayload> payloadList = null;
		try {
			orgRegionMasterList = orgRegionServedService.getOrgRegionMasterList();
			if (orgRegionMasterList == null) {
				throw new OrgRegionServedException(customMessageSource.getMessage("prog.region.error.not_found"));
			} else {
				payloadList = new ArrayList<OrgRegionMasterPayload>();
				for (OrgRegionMaster region : orgRegionMasterList) {
					payload = new OrgRegionMasterPayload();
					payload.setRegionId(region.getId());
					payload.setRegionName(region.getRegionName());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new OrgRegionServedException(customMessageSource.getMessage("prog.region.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}
	// Code for program region served end

	// Code for program SPI data start
	@RequestMapping(value = "/{id}/spidata", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgSpiDataList() throws OrgSpiDataException {
		List<OrgSpiDataDimensionsPayload> payloadList = new ArrayList<OrgSpiDataDimensionsPayload>();
		try {
			payloadList = orgSpiDataService.getSpiDataForResponse();
			if (payloadList == null) {
				throw new OrgSpiDataException(customMessageSource.getMessage("prog.spidata.error.not_found"));
			}

		} catch (Exception e) {
			throw new OrgSpiDataException(customMessageSource.getMessage("prog.spidata.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/spidata", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgSpiDataMapping(@RequestBody List<OrgSpiDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) throws OrgSpiDataException {
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));
		if (null != payloadList) {
			try {
				orgSpiDataService.createSpiDataMapping(payloadList, orgId);
			} catch (Exception e) {
				throw new OrgSpiDataException(customMessageSource.getMessage("prog.spidata.error.created"));
			}
			return sendSuccessResponse("prog.spidata.success.created");

		} else {
			return sendErrorResponse("org.bad.request");
		}

	}

	@RequestMapping(value = "/{id}/spidata/selected", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSpiData(@PathVariable("id") Long orgId) throws OrgSpiDataException {
		List<OrgSpiDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));

		try {
			payloadList = orgSpiDataService.getSelectedSpiData(orgId);

			if (payloadList == null) {
				throw new OrgSpiDataException(customMessageSource.getMessage("prog.spidata.error.not_found"));
			}

		} catch (Exception e) {
			throw new OrgSpiDataException(customMessageSource.getMessage("prog.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(payloadList);

	}// Code for program SPI data end

	// Code for program SDG data start
	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgSdgDataList() throws OrgSdgDataException {
		List<OrgSdgGoalPayload> payloadList = new ArrayList<OrgSdgGoalPayload>();
		try {
			payloadList = orgSdgDataService.getSdgDataForResponse();
			if (payloadList == null) {
				throw new OrgSdgDataException(customMessageSource.getMessage("prog.sdgdata.error.not_found"));
			}

		} catch (Exception e) {
			throw new OrgSdgDataException(customMessageSource.getMessage("prog.sdgdata.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgSdgDataMapping(@RequestBody List<OrgSdgDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) throws OrgSdgDataException {
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));
		if (null != payloadList) {
			try {
				orgSdgDataService.createSdgDataMapping(payloadList, orgId);
			} catch (Exception e) {
				throw new OrgSdgDataException(customMessageSource.getMessage("prog.sdgdata.error.created"));
			}
			return sendSuccessResponse("prog.sdgdata.success.created");

		} else {
			return sendErrorResponse("org.bad.request");
		}

	}

	@RequestMapping(value = "/{id}/sdgdata/selected", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSdgData(@PathVariable("id") Long orgId) throws OrgSdgDataException {
		List<OrgSdgDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));
		try {
			payloadList = orgSdgDataService.getSelectedSdgData(orgId);

			if (payloadList == null) {
				throw new OrgSdgDataException(customMessageSource.getMessage("prog.sdgdata.error.not_found"));
			}
		} catch (Exception e) {
			throw new OrgSdgDataException(customMessageSource.getMessage("prog.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(payloadList);

	}// Code for program SDG data end

}
