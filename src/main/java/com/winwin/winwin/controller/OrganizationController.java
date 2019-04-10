package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.winwin.entity.OrgRegionMaster;
import com.winwin.winwin.entity.OrgRegionServed;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationDataSetCategory;
import com.winwin.winwin.entity.OrganizationNote;
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
import com.winwin.winwin.payload.OrgChartPayload;
import com.winwin.winwin.payload.OrgRegionMasterPayload;
import com.winwin.winwin.payload.OrgRegionServedPayload;
import com.winwin.winwin.payload.OrgSdgDataMapPayload;
import com.winwin.winwin.payload.OrgSdgGoalPayload;
import com.winwin.winwin.payload.OrgSpiDataDimensionsPayload;
import com.winwin.winwin.payload.OrgSpiDataMapPayload;
import com.winwin.winwin.payload.OrganizationDataSetCategoryPayLoad;
import com.winwin.winwin.payload.OrganizationDataSetPayLoad;
import com.winwin.winwin.payload.OrganizationNotePayload;
import com.winwin.winwin.payload.OrganizationPayload;
import com.winwin.winwin.payload.OrganizationResourceCategoryPayLoad;
import com.winwin.winwin.payload.OrganizationResourcePayLoad;
import com.winwin.winwin.payload.SubOrganizationPayload;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationNoteRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.service.OrgRegionServedService;
import com.winwin.winwin.service.OrgSdgDataService;
import com.winwin.winwin.service.OrgSpiDataService;
import com.winwin.winwin.service.OrganizationDataSetService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.OrganizationResourceService;
import com.winwin.winwin.service.OrganizationService;

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
	private OrgRegionServedService orgRegionServedService;

	@Autowired
	OrgSpiDataService orgSpiDataService;

	@Autowired
	OrgSdgDataService orgSdgDataService;

	@Autowired
	OrganizationNoteService organizationNoteService;

	@Autowired
	OrganizationNoteRepository organizationNoteRepository;

	// Code for organization start
	@RequestMapping(value = "", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> createOrganization(@RequestBody OrganizationPayload organizationPayload) {
		Organization organization = null;
		OrganizationPayload payload = null;
		try {
			organization = organizationService.createOrganization(organizationPayload);
			payload = setOrganizationPayload(organization, payload);

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@Transactional
	public ResponseEntity<?> deleteOrganization(@RequestBody OrganizationPayload organizationPayLoad) {
		try {
			if (null != organizationPayLoad && null != organizationPayLoad.getId()) {
				Long id = organizationPayLoad.getId();
				Organization organization = organizationRepository.findOrgById(id);
				if (organization == null) {
					throw new OrganizationException(customMessageSource.getMessage("org.error.not_found"));
				}
				organizationService.deleteOrganization(id);

			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("org.success.deleted");
	}

	/**
	 * @param httpServletResponse
	 * @param organizationPayload
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.PUT)
	@Transactional
	public ResponseEntity<?> updateOrgDetails(@RequestBody List<OrganizationPayload> orgPayloadList) {
		Organization organization = null;
		List<OrganizationPayload> payloadList = new ArrayList<OrganizationPayload>();
		try {
			for (OrganizationPayload payload : orgPayloadList) {
				organization = organizationRepository.findOrgById(payload.getId());
				if (organization == null) {
					throw new OrganizationException(customMessageSource.getMessage("org.error.not_found"));
				} else {
					organization = organizationService.updateOrgDetails(payload, organization);
					payload = setOrganizationPayload(organization, payload);
					payloadList.add(payload);
				}

			}

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationList() throws OrganizationException {
		List<Organization> orgList = null;
		List<OrganizationPayload> payloadList = new ArrayList<OrganizationPayload>();
		OrganizationPayload payload = null;
		try {
			orgList = organizationService.getOrganizationList();
			if (orgList == null) {
				throw new OrganizationException(customMessageSource.getMessage("org.error.not_found"));
			} else {
				for (Organization organization : orgList) {
					payload = setOrganizationPayload(organization, payload);
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new OrganizationException(customMessageSource.getMessage("org.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@Transactional
	public ResponseEntity<?> getOrgDetails(@PathVariable("id") Long id) {
		Organization organization = null;
		OrganizationPayload payload = null;
		try {
			organization = organizationRepository.findOrgById(id);
			if (organization == null) {
				throw new OrganizationException(customMessageSource.getMessage("org.error.not_found"));
			} else {
				payload = setOrganizationPayload(organization, payload);

			}

		} catch (Exception e) {
			throw new OrganizationException(customMessageSource.getMessage("org.error.fetch") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);

	}

	/**
	 * @param organization
	 * @param payload
	 * @return
	 */
	private OrganizationPayload setOrganizationPayload(Organization organization, OrganizationPayload payload) {
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

		}
		return payload;
	}
	// Code for organization end

	// Code for organization data set start
	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.POST)
	public ResponseEntity<?> createOrganizationDataSet(
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
						customMessageSource.getMessage("org.dataset.error.created") + ": " + e.getMessage());
			}

		} else {
			return sendErrorResponse("org.bad.request");

		}

		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.PUT)
	public ResponseEntity<?> updateOrganizationDataSet(
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
				throw new OrganizationDataSetException(customMessageSource.getMessage("org.dataset.error.not_found"));
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
	public ResponseEntity<?> deleteOrganizationDataSet(
			@RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad) throws OrganizationDataSetException {
		try {
			if (null != organizationDataSetPayLoad && null != organizationDataSetPayLoad.getId()) {
				Long id = organizationDataSetPayLoad.getId();
				OrganizationDataSet dataSet = organizationDataSetRepository.findOrgDataSetById(id);
				if (dataSet == null) {
					throw new OrganizationDataSetException(
							customMessageSource.getMessage("org.dataset.error.not_found"));
				}
				organizationDataSetService.removeOrganizationDataSet(id);
			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationDataSetException(
					customMessageSource.getMessage("org.dataset.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("org.dataset.success.deleted");
	}

	@RequestMapping(value = "{id}/datasets", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationDataSetList(@PathVariable("id") Long id)
			throws OrganizationDataSetException {
		List<OrganizationDataSet> orgDataSetList = null;
		OrganizationDataSetPayLoad payload = null;
		OrganizationDataSetCategory category = null;
		OrganizationDataSetCategoryPayLoad payloadCategory = null;
		List<OrganizationDataSetPayLoad> payloadList = null;
		try {
			orgDataSetList = organizationDataSetService.getOrganizationDataSetList(id);
			if (orgDataSetList == null) {
				throw new OrganizationDataSetException(customMessageSource.getMessage("org.dataset.error.not_found"));
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
					customMessageSource.getMessage("org.dataset.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/dataset/categorylist", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationDataSetCategoryList(HttpServletResponse httpServletResponce)
			throws OrganizationDataSetCategoryException {
		List<OrganizationDataSetCategory> orgDataSetCategoryList = null;
		List<OrganizationDataSetCategoryPayLoad> payloadList = null;
		OrganizationDataSetCategoryPayLoad payload = null;
		try {
			orgDataSetCategoryList = organizationDataSetService.getOrganizationDataSetCategoryList();
			if (orgDataSetCategoryList == null) {
				throw new OrganizationDataSetCategoryException(
						customMessageSource.getMessage("org.dataset.category.error.not_found"));
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
					customMessageSource.getMessage("org.dataset.category.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for organization data set end

	// Code for organization resource start
	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.POST)
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
						customMessageSource.getMessage("org.resource.error.created") + ": " + e.getMessage());
			}

		} else {
			return sendErrorResponse("org.bad.request");

		}
		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.PUT)
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
							customMessageSource.getMessage("org.resource.error.not_found"));
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
					customMessageSource.getMessage("org.resource.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayLoad organizationResourcePayLoad)
			throws OrganizationResourceException {
		try {
			if (null != organizationResourcePayLoad && null != organizationResourcePayLoad.getId()) {
				Long id = organizationResourcePayLoad.getId();
				OrganizationResource organizationResource = organizationResourceRepository.findOrgResourceById(id);
				if (organizationResource == null) {
					throw new OrganizationResourceException(
							customMessageSource.getMessage("org.resource.error.not_found"));
				}
				organizationResourceService.removeOrganizationResource(id);
			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationResourceException(
					customMessageSource.getMessage("org.resource.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("org.resource.success.deleted");
	}

	@RequestMapping(value = "/{id}/resources", method = RequestMethod.GET)
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
				throw new OrganizationResourceException(customMessageSource.getMessage("org.resource.error.not_found"));
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
					customMessageSource.getMessage("org.resource.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/resource/categorylist", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationResourceCategoryList(HttpServletResponse httpServletResponce)
			throws OrganizationResourceCategoryException {
		List<OrganizationResourceCategory> orgResourceCategoryList = null;
		OrganizationResourceCategoryPayLoad payload = null;
		List<OrganizationResourceCategoryPayLoad> payloadList = null;

		try {
			orgResourceCategoryList = organizationResourceService.getOrganizationResourceCategoryList();
			if (orgResourceCategoryList == null) {
				throw new OrganizationResourceCategoryException(
						customMessageSource.getMessage("org.resource.category.error.not_found"));
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
					customMessageSource.getMessage("org.resource.category.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for organization resource end

	// Code for organization region served start
	@RequestMapping(value = "/{id}/region", method = RequestMethod.POST)
	@Transactional
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
					customMessageSource.getMessage("org.region.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/{id}/regions", method = RequestMethod.GET)
	public ResponseEntity<?> getOrgRegionsList() throws OrgRegionServedException {
		List<OrgRegionServed> orgRegionList = null;
		OrgRegionServedPayload payload = null;
		List<OrgRegionServedPayload> payloadList = null;
		try {
			orgRegionList = orgRegionServedService.getOrgRegionServedList();
			if (orgRegionList == null) {
				throw new OrgRegionServedException(customMessageSource.getMessage("org.region.error.not_found"));
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
			throw new OrgRegionServedException(customMessageSource.getMessage("org.region.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/regionmasters", method = RequestMethod.GET)
	public ResponseEntity<?> getOrgRegionsMasterList() throws OrgRegionServedException {
		List<OrgRegionMaster> orgRegionMasterList = null;
		OrgRegionMasterPayload payload = null;
		List<OrgRegionMasterPayload> payloadList = null;
		try {
			orgRegionMasterList = orgRegionServedService.getOrgRegionMasterList();
			if (orgRegionMasterList == null) {
				throw new OrgRegionServedException(customMessageSource.getMessage("org.region.error.not_found"));
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
			throw new OrgRegionServedException(customMessageSource.getMessage("org.region.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for organization region served end

	// Code for organization SPI data start
	@RequestMapping(value = "/{id}/spidata", method = RequestMethod.GET)
	public ResponseEntity<?> getOrgSpiDataList() throws OrgSpiDataException {
		List<OrgSpiDataDimensionsPayload> payloadList = new ArrayList<OrgSpiDataDimensionsPayload>();
		try {
			payloadList = orgSpiDataService.getSpiDataForResponse();
			if (payloadList == null) {
				throw new OrgSpiDataException(customMessageSource.getMessage("org.spidata.error.not_found"));
			}

		} catch (Exception e) {
			throw new OrgSpiDataException(customMessageSource.getMessage("org.spidata.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/spidata", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> createOrgSpiDataMapping(@RequestBody List<OrgSpiDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) throws OrgSpiDataException {
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		if (null != payloadList) {
			try {
				orgSpiDataService.createSpiDataMapping(payloadList, orgId);
			} catch (Exception e) {
				throw new OrgSpiDataException(customMessageSource.getMessage("org.spidata.error.created"));
			}
			return sendSuccessResponse("org.spidata.success.created");

		} else {
			return sendErrorResponse("org.bad.request");
		}

	}

	@RequestMapping(value = "/{id}/spidata/selected", method = RequestMethod.GET)
	public ResponseEntity<?> getSelectedOrgSpiData(@PathVariable("id") Long orgId) throws OrgSpiDataException {
		List<OrgSpiDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));

		try {
			payloadList = orgSpiDataService.getSelectedSpiData(orgId);

			if (payloadList == null) {
				throw new OrgSpiDataException(customMessageSource.getMessage("org.spidata.error.not_found"));
			}

		} catch (Exception e) {
			throw new OrgSpiDataException(customMessageSource.getMessage("org.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(payloadList);

	}// Code for organization SPI data end

	// Code for organization SDG data start
	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.GET)
	public ResponseEntity<?> getOrgSdgDataList() throws OrgSdgDataException {
		List<OrgSdgGoalPayload> payloadList = new ArrayList<OrgSdgGoalPayload>();
		try {
			payloadList = orgSdgDataService.getSdgDataForResponse();
			if (payloadList == null) {
				throw new OrgSdgDataException(customMessageSource.getMessage("org.sdgdata.error.not_found"));
			}

		} catch (Exception e) {
			throw new OrgSdgDataException(customMessageSource.getMessage("org.sdgdata.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> createOrgSdgDataMapping(@RequestBody List<OrgSdgDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) throws OrgSdgDataException {
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		if (null != payloadList) {
			try {
				orgSdgDataService.createSdgDataMapping(payloadList, orgId);
			} catch (Exception e) {
				throw new OrgSdgDataException(customMessageSource.getMessage("org.sdgdata.error.created"));
			}
			return sendSuccessResponse("org.sdgdata.success.created");

		} else {
			return sendErrorResponse("org.bad.request");
		}

	}

	@RequestMapping(value = "/{id}/sdgdata/selected", method = RequestMethod.GET)
	public ResponseEntity<?> getSelectedOrgSdgData(@PathVariable("id") Long orgId) throws OrgSdgDataException {
		List<OrgSdgDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			payloadList = orgSdgDataService.getSelectedSdgData(orgId);

			if (payloadList == null) {
				throw new OrgSdgDataException(customMessageSource.getMessage("org.sdgdata.error.not_found"));
			}
		} catch (Exception e) {
			throw new OrgSdgDataException(customMessageSource.getMessage("org.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(payloadList);

	}// Code for organization SDG data end

	// Code for organization Program Details start
	@RequestMapping(value = "/{id}/program", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> createProgram(@RequestBody OrganizationPayload organizationPayload,
			@PathVariable("id") Long orgId) {
		Organization organization = null;
		OrganizationPayload payload = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			organization = organizationService.createProgram(organizationPayload);
			payload = setOrganizationPayload(organization, payload);

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("prg.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "/{id}/program", method = RequestMethod.GET)
	public ResponseEntity<?> getProgramList(@PathVariable("id") Long orgId) throws OrganizationException {
		List<Organization> prgList = null;
		List<OrganizationPayload> payloadList = new ArrayList<OrganizationPayload>();
		OrganizationPayload payload = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("org.error.organization.null"));
		try {
			prgList = organizationService.getProgramList(orgId);
			if (prgList == null) {
				throw new OrganizationException(customMessageSource.getMessage("prg.error.not_found"));
			} else {
				for (Organization organization : prgList) {
					payload = setOrganizationPayload(organization, payload);
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new OrganizationException(customMessageSource.getMessage("prg.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/program", method = RequestMethod.DELETE)
	@Transactional
	public ResponseEntity<?> deleteProgram(@RequestBody OrganizationPayload organizationPayLoad) {
		try {
			if (null != organizationPayLoad && null != organizationPayLoad.getId()) {
				Long id = organizationPayLoad.getId();
				Organization organization = organizationRepository.findOrgById(id);
				if (organization == null) {
					throw new OrganizationException(customMessageSource.getMessage("prg.error.not_found"));
				}
				organizationService.deleteOrganization(id);

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
	@Transactional
	public ResponseEntity<?> updateProgramDetails(@RequestBody List<OrganizationPayload> orgPayloadList) {
		Organization organization = null;
		List<OrganizationPayload> payloadList = new ArrayList<OrganizationPayload>();
		try {
			for (OrganizationPayload payload : orgPayloadList) {
				organization = organizationRepository.findOrgById(payload.getId());
				if (organization == null) {
					throw new OrganizationException(customMessageSource.getMessage("prg.error.not_found"));
				} else {
					organization = organizationService.updateOrgDetails(payload, organization);
					payload = setOrganizationPayload(organization, payload);
					payloadList.add(payload);
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
	public ResponseEntity<?> getSuborganizationList(@PathVariable("id") Long orgId) throws OrganizationException {
		Organization organization = null;
		OrgChartPayload payload = new OrgChartPayload();
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
	@Transactional
	public ResponseEntity<?> createSuborganization(@RequestBody SubOrganizationPayload subOrganizationPayload) {
		OrganizationPayload payload = null;
		Organization organization = null;
		try {
			organization = organizationService.createSubOrganization(subOrganizationPayload);
			payload = setOrganizationPayload(organization, payload);

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}
	// Code for organization Chart end

	// Code for organization notes start
	@RequestMapping(value = "/{id}/notes", method = RequestMethod.POST)
	@Transactional
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

	@RequestMapping(value = "/{id}/notes", method = RequestMethod.DELETE)
	@Transactional
	public ResponseEntity<?> deleteOrgNote(@RequestBody OrganizationNotePayload organizationNotePayLoad) {
		try {
			if (null != organizationNotePayLoad && null != organizationNotePayLoad.getNoteId()) {
				Long noteId = organizationNotePayLoad.getNoteId();
				OrganizationNote note = organizationNoteRepository.findOrgNoteById(noteId);
				if (note == null) {
					throw new OrganizationException(customMessageSource.getMessage("org.note.error.not_found"));
				}
				organizationNoteService.removeOrganizationNote(noteId);

			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.note.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("org.note.success.deleted");
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
			payload.setNoteId(organizationNote.getId());
			payload.setNote(organizationNote.getName());
			payload.setOrganizationId(organizationNote.getOrganizationId());
			payload.setCreatedBy(organizationNote.getCreatedBy());
			payload.setCreatedAt(organizationNote.getCreatedAt());

		}
		return payload;
	}
}
