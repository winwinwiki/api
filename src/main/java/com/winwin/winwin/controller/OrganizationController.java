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

import com.winwin.winwin.entity.OrgRegionServed;
import com.winwin.winwin.entity.OrgSdgDataMapping;
import com.winwin.winwin.entity.OrgSpiDataMapping;
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
import com.winwin.winwin.repository.OrgRegionServedRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.service.OrgRegionServedService;
import com.winwin.winwin.service.OrgSdgDataService;
import com.winwin.winwin.service.OrgSpiDataService;
import com.winwin.winwin.service.OrganizationDataSetService;
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
	private OrgRegionServedRepository orgRegionServedRepository;

	@Autowired
	OrgSpiDataService orgSpiDataService;

	@Autowired
	OrgSdgDataService orgSdgDataService;

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity createOrganization(HttpServletResponse httpServletResponse,
			@RequestBody OrganizationPayload organizationPayload) {
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

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@Transactional
	public ResponseEntity deleteOrganization(HttpServletResponse httpServletResponse,
			@RequestBody OrganizationPayload organizationPayLoad) {
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
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "", method = RequestMethod.PUT)
	@Transactional
	public ResponseEntity updateOrgDetails(HttpServletResponse httpServletResponse,
			@RequestBody OrganizationPayload organizationPayload) {
		Organization organization = null;
		OrganizationPayload payload = null;
		try {
			organization = organizationRepository.findOrgById(organizationPayload.getId());
			if (organization == null) {
				throw new OrganizationException(customMessageSource.getMessage("org.error.not_found"));
			} else {
				organization = organizationService.updateOrgDetails(organizationPayload, organization);
				payload = setOrganizationPayload(organization, payload);
			}

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationList(HttpServletResponse httpServletResponse) throws OrganizationException {
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

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@Transactional
	public ResponseEntity getOrgDetails(HttpServletResponse httpServletResponse, @PathVariable("id") Long id) {
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
			payload.setDescription(organization.getDescription());
			// payload.setStatus(organization.getTagStatus());
			payload.setPriority(organization.getPriority());
			payload.setParentId(organization.getParentId());
			payload.setType(organization.getType());
			payload.setIsActive(organization.getIsActive());
			// payload.setIsTaggingReady(isTaggingReady);
			payload.setTagStatus(organization.getTagStatus());
			payload.setTotalAssets(organization.getAssets());
			payload.setWebsiteUrl(organization.getWebsiteUrl());
			payload.setSocialUrl(organization.getSocialUrl());
			// payload.setClassificationId(classificationId);

		}
		return payload;
	}

	// Code for organization data set start
	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.POST)
	public ResponseEntity createOrganizationDataSet(
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

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.PUT)
	public ResponseEntity updateOrganizationDataSet(
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

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.DELETE)
	public ResponseEntity deleteOrganizationDataSet(@RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
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
	public ResponseEntity<?> getOrganizationDataSetList(HttpServletResponse httpServletResponse,
			@PathVariable("id") Long id) throws OrganizationDataSetException {
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
	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.POST)
	public ResponseEntity createOrganizationResource(
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

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.PUT)
	public ResponseEntity updateOrganizationResource(
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

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.DELETE)
	public ResponseEntity deleteOrganizationResource(
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
	public ResponseEntity<?> getOrganizationResourceList(HttpServletResponse httpServletResponse,
			@PathVariable("id") Long id) throws OrganizationResourceException {
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
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/{id}/region", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity createOrgRegions(HttpServletResponse httpServletResponse,
			@RequestBody List<OrgRegionServedPayload> orgRegionServedPayloadList) throws OrgRegionServedException {
		List<OrgRegionServed> orgRegionServedList = null;
		List<OrgRegionServedPayload> payloadList = null;
		OrgRegionServedPayload payload = null;
		AddressPayload addressPayload = null;
		try {
			orgRegionServedList = orgRegionServedService.createOrgRegionServed(orgRegionServedPayloadList);
			if (null != orgRegionServedList) {
				payloadList = new ArrayList<OrgRegionServedPayload>();
				for (OrgRegionServed region : orgRegionServedList) {
					payload = new OrgRegionServedPayload();
					payload.setId(region.getId());
					payload.setOrganizationId(region.getOrgId());
					if (null != region.getAddress()) {
						addressPayload = new AddressPayload();
						addressPayload.setId(region.getAddress().getId());
						addressPayload.setCountry(region.getAddress().getCountry());
						addressPayload.setState(region.getAddress().getState());
						addressPayload.setCity(region.getAddress().getCity());
						addressPayload.setCounty(region.getAddress().getCounty());
						addressPayload.setZip(region.getAddress().getZip());
						addressPayload.setStreet(region.getAddress().getStreet());
						addressPayload.setPlaceId(region.getAddress().getPlaceId());
						payload.setAddress(addressPayload);
					}
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
	public ResponseEntity<?> getOrgRegionsList(HttpServletResponse httpServletResponse)
			throws OrgRegionServedException {
		List<OrgRegionServed> orgRegionList = null;
		OrgRegionServedPayload payload = null;
		AddressPayload addressPayload = null;
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
					payload.setOrganizationId(region.getOrgId());
					if (null != region.getAddress()) {
						addressPayload = new AddressPayload();
						addressPayload.setId(region.getAddress().getId());
						addressPayload.setCountry(region.getAddress().getCountry());
						addressPayload.setState(region.getAddress().getState());
						addressPayload.setCity(region.getAddress().getCity());
						addressPayload.setCounty(region.getAddress().getCounty());
						addressPayload.setZip(region.getAddress().getZip());
						addressPayload.setStreet(region.getAddress().getStreet());
						addressPayload.setPlaceId(region.getAddress().getPlaceId());
						payload.setAddress(addressPayload);
					}
					payload.setIsActive(region.getIsActive());
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
	public ResponseEntity<?> getOrgSpiDataList(HttpServletResponse httpServletResponse) throws OrgSpiDataException {
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

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/{id}/spidata", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity createOrgSpiDataMapping(HttpServletResponse httpServletResponse,
			@RequestBody List<OrgSpiDataMapPayload> payloadList, @PathVariable("id") Long orgId)
			throws OrgSpiDataException {
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
	public ResponseEntity<?> getSelectedOrgSpiData(HttpServletResponse httpServletResponse) throws OrgSpiDataException {
		List<OrgSpiDataMapping> selectedSpiDataList = null;
		OrgSpiDataMapPayload payload = null;
		List<OrgSpiDataMapPayload> payloadList = null;
		try {
			selectedSpiDataList = orgSpiDataService.getSelectedSpiData();

			if (selectedSpiDataList == null) {
				throw new OrgSpiDataException(customMessageSource.getMessage("org.spidata.error.not_found"));
			} else {
				payloadList = new ArrayList<OrgSpiDataMapPayload>();
				for (OrgSpiDataMapping spiDataMapObj : selectedSpiDataList) {
					payload = new OrgSpiDataMapPayload();
					payload.setId(spiDataMapObj.getId());
					payload.setOrganizationId(spiDataMapObj.getOrganizationId());
					payload.setDimensionId(spiDataMapObj.getDimensionId());
					payload.setDimensionName(spiDataMapObj.getDimensionName());
					payload.setComponentId(spiDataMapObj.getComponentId());
					payload.setComponentName(spiDataMapObj.getComponentName());
					payload.setIndicatorId(spiDataMapObj.getIndicatorId());
					payload.setIndicatorName(spiDataMapObj.getIndicatorName());
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			throw new OrgSpiDataException(customMessageSource.getMessage("org.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(payloadList);

	}// Code for organization SPI data end

	// Code for organization SDG data start
	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.GET)
	public ResponseEntity<?> getOrgSdgDataList(HttpServletResponse httpServletResponse) throws OrgSdgDataException {
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

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity createOrgSdgDataMapping(HttpServletResponse httpServletResponse,
			@RequestBody List<OrgSdgDataMapPayload> payloadList, @PathVariable("id") Long orgId)
			throws OrgSdgDataException {
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
	public ResponseEntity<?> getSelectedOrgSdgData(HttpServletResponse httpServletResponse) throws OrgSdgDataException {
		List<OrgSdgDataMapping> selectedSdgDataList = null;
		OrgSdgDataMapPayload payload = null;
		List<OrgSdgDataMapPayload> payloadList = null;
		try {
			selectedSdgDataList = orgSdgDataService.getSelectedSdgData();

			if (selectedSdgDataList == null) {
				throw new OrgSdgDataException(customMessageSource.getMessage("org.sdgdata.error.not_found"));
			} else {
			}
		} catch (Exception e) {
			throw new OrgSdgDataException(customMessageSource.getMessage("org.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(selectedSdgDataList);

	}// Code for organization SDG data end

}
