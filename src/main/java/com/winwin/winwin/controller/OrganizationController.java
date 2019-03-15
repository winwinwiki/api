package com.winwin.winwin.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

@RestController
@RequestMapping(value = "/organization")
public class OrganizationController extends BaseController {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@GetMapping("/organization")
	public String getOrganization() {
		return "Hello World";
		// return orgRepo.getOne(id);
	}

	// @PostMapping("/create")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@Transactional
	public Object createOrganization(HttpServletResponse httpServletResponse,
			@RequestBody OrganizationPayload organizationPayload) {
		try {
			organizationService.createOrganization(organizationPayload);
			return true;
		} catch (Exception e) {
			throw new OrganizationException("There is some error while creating the org");
		}
	}

	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	@Transactional
	public Object deleteOrganization(HttpServletResponse httpServletResponse, @PathVariable("id") Long id) {
		try {
			Organization organization = organizationRepository.findOrgById(id);
			if (organization == null) {
				throw new OrganizationException("Org is not found");
			} else {
				organizationService.deleteOrganization(id);
				return true;
			}

		} catch (Exception e) {
			throw new OrganizationException("Org is not found");
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@Transactional
	public Object updateOrgDetails(HttpServletResponse httpServletResponse,
			@RequestBody OrganizationPayload organizationPayload) {
		try {
			Organization organization = organizationRepository.findOrgById(organizationPayload.getId());
			if (organization == null) {
				throw new OrganizationException("Org is not found");
			} else {
				organizationService.updateOrgDetails(organizationPayload, organization);
				return true;
			}

		} catch (Exception e) {
			throw new OrganizationException(e.getMessage());
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationList(HttpServletResponse httpServletResponse) throws OrganizationException {
		List<Organization> orgList = null;
		try {
			orgList = organizationService.getOrganizationList();
			if (orgList == null) {
				throw new OrganizationException("org.error.list.not_found");
			}
		} catch (Exception e) {
			throw new OrganizationException("org.error.list");
		}
		return sendSuccessResponse(orgList);

	}
}
