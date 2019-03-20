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

import com.winwin.winwin.payload.OrganizationResourceCategoryPayLoad;
import com.winwin.winwin.payload.OrganizationResourcePayLoad;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.service.OrganizationResourceService;
import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.OrganizationResourceCategory;
import com.winwin.winwin.exception.OrganizationResourceCategoryException;
import com.winwin.winwin.exception.OrganizationResourceException;

/**
 * @author ArvindK
 *
 */
@RestController
@RequestMapping(value = "/orgresources")
public class OrganizationResourceController extends BaseController {

	@Autowired
	private OrganizationResourceService organizationResourceService;

	@Autowired
	private OrganizationResourceRepository organizationResourceRepository;

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity createOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayLoad organizationResourcePayLoad)
			throws OrganizationResourceException {
		OrganizationResource organizationResource = null;
		OrganizationResourcePayLoad payload = null;
		if (null != organizationResourcePayLoad) {
			try {
				organizationResource = organizationResourceService
						.createOrUpdateOrganizationResource(organizationResourcePayLoad);
				if (null != organizationResource) {
					payload = new OrganizationResourcePayLoad();
					payload.setId(organizationResource.getId());
					payload.setOrganizationResourceCategory(organizationResource.getOrganizationResourceCategory());
					payload.setOrganizationId(organizationResource.getOrganizationId());
					payload.setCount(organizationResource.getCount());
					payload.setDescription(organizationResource.getDescription());
					payload.setType(organizationResource.getType());
					payload.setUrl(organizationResource.getUrl());
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
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	public ResponseEntity updateOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayLoad organizationResourcePayLoad)
			throws OrganizationResourceException {
		OrganizationResource organizationResource = null;
		OrganizationResourcePayLoad payload = new OrganizationResourcePayLoad();
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
					payload.setId(organizationResource.getId());
					payload.setOrganizationResourceCategory(organizationResource.getOrganizationResourceCategory());
					payload.setOrganizationId(organizationResource.getOrganizationId());
					payload.setCount(organizationResource.getCount());
					payload.setDescription(organizationResource.getDescription());
					payload.setType(organizationResource.getType());
					payload.setUrl(organizationResource.getUrl());
					payload.setIsActive(organizationResource.getIsActive());
				}

			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationResourceException(
					customMessageSource.getMessage("org.resource.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(organizationResource);
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
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

	@RequestMapping(value = "/list/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationResourceList(HttpServletResponse httpServletResponse,
			@PathVariable("id") Long id) throws OrganizationResourceException {
		List<OrganizationResource> orgResourceList = null;
		OrganizationResourcePayLoad payload = null;
		List<OrganizationResourcePayLoad> payloadList = new ArrayList<OrganizationResourcePayLoad>();
		try {
			orgResourceList = organizationResourceService.getOrganizationResourceList(id);
			if (orgResourceList == null) {
				throw new OrganizationResourceException(customMessageSource.getMessage("org.resource.error.not_found"));
			} else {
				for (OrganizationResource resource : orgResourceList) {
					payload = new OrganizationResourcePayLoad();
					payload.setId(resource.getId());
					payload.setOrganizationResourceCategory(resource.getOrganizationResourceCategory());
					payload.setOrganizationId(resource.getOrganizationId());
					payload.setCount(resource.getCount());
					payload.setDescription(resource.getDescription());
					payload.setType(resource.getType());
					payload.setUrl(resource.getUrl());
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

	@RequestMapping(value = "/categorylist", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationResourceCategoryList(HttpServletResponse httpServletResponce)
			throws OrganizationResourceCategoryException {
		List<OrganizationResourceCategory> orgResourceCategoryList = null;
		OrganizationResourceCategoryPayLoad payload = null;
		List<OrganizationResourceCategoryPayLoad> payloadList = new ArrayList<OrganizationResourceCategoryPayLoad>();

		try {
			orgResourceCategoryList = organizationResourceService.getOrganizationResourceCategoryList();
			if (orgResourceCategoryList == null) {
				throw new OrganizationResourceCategoryException(
						customMessageSource.getMessage("org.resource.category.error.not_found"));
			} else {
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

}
