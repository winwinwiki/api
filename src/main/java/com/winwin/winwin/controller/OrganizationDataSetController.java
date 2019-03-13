package com.winwin.winwin.controller;

import java.util.List;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.winwin.payload.OrganizationDataSetPayLoad;
import com.winwin.winwin.service.OrganizationDataSetService;
import com.winwin.winwin.entity.OrganizationDataSet;

/**
 * @author ArvindK
 *
 */
@RestController
@RequestMapping(value = "/organizationdataset")
public class OrganizationDataSetController {

	@Autowired
	private OrganizationDataSetService organizationDataSetService;

	@GetMapping("/organizationdataset")
	public String getOrganizationDataSet() {
		return "Hello World  For Organization Data Set";
		// return orgRepo.getOne(id);
	}

	@PostMapping("/create")
	@Transactional
	public void createOrganizationDataSet(@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad) {
		organizationDataSetService.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
	}
	
	@PostMapping("/update")
	@Transactional
	public void updateOrganizationDataSet(@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad) {
		organizationDataSetService.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
	}
	
	@PostMapping("/delete")
	@Transactional
	public void deleteOrganizationDataSet(@Valid @RequestBody Long id) {
		organizationDataSetService.removeOrganizationDataSet(id);
	}

	@GetMapping("/organizationdatasetlist")
	public List<OrganizationDataSet> getOrganizationDataSetList() {
		return organizationDataSetService.getOrganizationDataSetList();
	}// end of method getOrganizationDataSetList

}
