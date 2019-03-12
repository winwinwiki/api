package com.winwin.winwin.controller;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.payload.OrganizationPayload;
import com.winwin.winwin.service.OrganizationService;

@RestController
@RequestMapping(value = "/organization")
public class OrganizationController {

	@Autowired
	private OrganizationService organizationService;

	@GetMapping("/organization")
	public String getOrganization() {
		return "Hello World";
		// return orgRepo.getOne(id);
	}

	@PostMapping("/create")
	@Transactional
	public Object createOrganization(HttpServletResponse httpServletResponse, @Valid @RequestBody OrganizationPayload organizationPayload) {
		try {
		organizationService.createOrganization(organizationPayload);
		return true;
	}catch (Exception e) {
		throw new OrganizationException();
	}
	}
}
