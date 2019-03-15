package com.winwin.winwin.controller;

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

import com.winwin.winwin.payload.OrganizationResourcePayLoad;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.service.OrganizationResourceService;
import com.winwin.winwin.entity.OrganizationResource;
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
		if (null != organizationResourcePayLoad) {
			try {
				organizationResource = organizationResourceService
						.createOrUpdateOrganizationResource(organizationResourcePayLoad);
			} catch (Exception e) {
				throw new OrganizationResourceException("org.resource.error.created");
			}

		} else {
			return sendErrorResponse("org.bad.request");

		}
		return sendSuccessResponse(organizationResource);
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	public ResponseEntity updateOrganizationResource(
			@Valid @RequestBody OrganizationResourcePayLoad organizationResourcePayLoad)
			throws OrganizationResourceException {
		try {
			if (null != organizationResourcePayLoad && null != organizationResourcePayLoad.getId()) {
				Long id = organizationResourcePayLoad.getId();
				OrganizationResource organizationResource = organizationResourceRepository.findOrgResourceById(id);
				if (organizationResource == null) {
					throw new OrganizationResourceException("org.resource.error.update.not_found");
				}
				organizationResourceService.createOrUpdateOrganizationResource(organizationResourcePayLoad);
			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationResourceException("org.resource.error.updated");
		}
		return sendSuccessResponse("org.resource.success.updated");
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
					throw new OrganizationResourceException("org.resource.error.delete.not_found");
				}
				organizationResourceService.removeOrganizationResource(id);
			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationResourceException("org.resource.error.deleted");
		}
		return sendSuccessResponse("org.resource.success.deleted");
	}

	@RequestMapping(value = "/list/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationResourceList(HttpServletResponse httpServletResponse,
			@PathVariable("id") Long id) throws OrganizationResourceException {
		List<OrganizationResource> orgResourceList = null;
		try {
			orgResourceList = organizationResourceService.getOrganizationResourceList(id);
			if (orgResourceList == null) {
				throw new OrganizationResourceException("org.resource.error.list.not_found");
			}
		} catch (Exception e) {
			throw new OrganizationResourceException("org.resource.error.list");
		}
		return sendSuccessResponse(orgResourceList);

	}

}
