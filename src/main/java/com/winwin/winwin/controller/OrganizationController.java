package com.winwin.winwin.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.payload.OrganizationPayload;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.service.OrganizationService;

/**
 * @author ArvindK
 *
 */
@RestController
@RequestMapping(value = "/organization")
public class OrganizationController extends BaseController {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity createOrganization(HttpServletResponse httpServletResponse,
			@RequestBody OrganizationPayload organizationPayload) {
		Organization organization = null;
		try {
			organization = organizationService.createOrganization(organizationPayload);
		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(organization);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	@Transactional
	public ResponseEntity deleteOrganization(HttpServletResponse httpServletResponse, @PathVariable("id") Long id) {
		try {
			if (null != id) {
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
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@Transactional
	public ResponseEntity updateOrgDetails(HttpServletResponse httpServletResponse,
			@RequestBody OrganizationPayload organizationPayload) {
		Organization organization = null;
		try {
			organization = organizationRepository.findOrgById(organizationPayload.getId());
			if (organization == null) {
				throw new OrganizationException(customMessageSource.getMessage("org.error.not_found"));
			} else {
				organization = organizationService.updateOrgDetails(organizationPayload, organization);
			}

		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(organization);

	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationList(HttpServletResponse httpServletResponse) throws OrganizationException {
		List<Organization> orgList = null;
		try {
			orgList = organizationService.getOrganizationList();
			if (orgList == null) {
				throw new OrganizationException(customMessageSource.getMessage("org.error.not_found"));
			}
		} catch (Exception e) {
			throw new OrganizationException(customMessageSource.getMessage("org.error.list"));
		}
		return sendSuccessResponse(orgList);

	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	@Transactional
	public ResponseEntity getOrgDetails(HttpServletResponse httpServletResponse, @PathVariable("id") Long id) {
		Organization organization = null;
		try {
			organization = organizationRepository.findOrgById(id);
			if (organization == null) {
				throw new OrganizationException(customMessageSource.getMessage("org.error.not_found"));
			}

		} catch (Exception e) {
			throw new OrganizationException(customMessageSource.getMessage("org.error.fetch") + ": " + e.getMessage());
		}
		return sendSuccessResponse(organization);

	}
}
