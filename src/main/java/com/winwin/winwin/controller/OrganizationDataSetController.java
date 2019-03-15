package com.winwin.winwin.controller;

import java.util.List;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.winwin.payload.OrganizationDataSetPayLoad;
import com.winwin.winwin.service.OrganizationDataSetService;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.exception.OrganizationDataSetException;

/**
 * @author ArvindK
 *
 */
@RestController
@RequestMapping(value = "/orgdataset")
public class OrganizationDataSetController extends BaseController {

	@Autowired
	private OrganizationDataSetService organizationDataSetService;

	@SuppressWarnings("rawtypes")
	@PostMapping("/create")
	@Transactional
	public ResponseEntity createOrganizationDataSet(
			@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
		try {
			organizationDataSetService.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
		} catch (Exception e) {
			throw new OrganizationDataSetException("error occoured while creating organization dataset");
		}
		return sendSuccessResponse("org.success.created");
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/update")
	@Transactional
	public ResponseEntity updateOrganizationDataSet(
			@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
		try {
			organizationDataSetService.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
		} catch (Exception e) {
			throw new OrganizationDataSetException("error occoured while updating organization dataset");
		}
		return sendSuccessResponse("org.success.updated");
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/delete")
	@Transactional
	public ResponseEntity deleteOrganizationDataSet(
			@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
		try {
			if (null != organizationDataSetPayLoad) {
				Long id = organizationDataSetPayLoad.getId();
				organizationDataSetService.removeOrganizationDataSet(id);
			}

		} catch (Exception e) {
			throw new OrganizationDataSetException("error occoured while deleting organization dataset");
		}
		return sendSuccessResponse("org.success.deleted");
	}

	@GetMapping("/list")
	public ResponseEntity<?> getOrganizationDataSetList() throws OrganizationDataSetException {
		List<OrganizationDataSet> orgDataSetList = null;
		try {
			orgDataSetList = organizationDataSetService.getOrganizationDataSetList();
		} catch (Exception e) {
			throw new OrganizationDataSetException("error occoured while gettting organization dataset list");
		}
		return sendSuccessResponse(orgDataSetList);

	}

}
