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

import com.winwin.winwin.payload.OrganizationDataSetPayLoad;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
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

	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity createOrganizationDataSet(
			@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
		OrganizationDataSet organizationDataSet = null;
		if (null != organizationDataSetPayLoad) {
			try {
				organizationDataSet = organizationDataSetService
						.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
			} catch (Exception e) {
				throw new OrganizationDataSetException("org.dataset.success.updated");
			}

		} else {
			return sendErrorResponse("org.bad.request");

		}

		return sendSuccessResponse(organizationDataSet);
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	public ResponseEntity updateOrganizationDataSet(
			@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {

		if (null != organizationDataSetPayLoad && null != organizationDataSetPayLoad.getId()) {
			Long id = organizationDataSetPayLoad.getId();
			OrganizationDataSet dataSet = organizationDataSetRepository.findOrgDataSetById(id);
			if (dataSet == null) {
				throw new OrganizationDataSetException("org.dataset.error.update.not_found");
			}
			organizationDataSetService.createOrUpdateOrganizationDataSet(organizationDataSetPayLoad);
		} else {
			return sendErrorResponse("org.bad.request");

		}

		return sendSuccessResponse("org.dataset.success.updated");
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity deleteOrganizationDataSet(@RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
		try {
			if (null != organizationDataSetPayLoad && null != organizationDataSetPayLoad.getId()) {
				Long id = organizationDataSetPayLoad.getId();
				OrganizationDataSet dataSet = organizationDataSetRepository.findOrgDataSetById(id);
				if (dataSet == null) {
					throw new OrganizationDataSetException("org.dataset.error.delete.not_found");
				}
				organizationDataSetService.removeOrganizationDataSet(id);
			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new OrganizationDataSetException("org.dataset.error.deleted");
		}
		return sendSuccessResponse("org.dataset.success.deleted");
	}

	@RequestMapping(value = "/list/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationDataSetList(HttpServletResponse httpServletResponse,
			@PathVariable("id") Long id) throws OrganizationDataSetException {
		List<OrganizationDataSet> orgDataSetList = null;
		try {
			orgDataSetList = organizationDataSetService.getOrganizationDataSetList(id);
			if (orgDataSetList == null) {
				throw new OrganizationDataSetException("org.dataset.error.list.not_found");
			}
		} catch (Exception e) {
			throw new OrganizationDataSetException("org.dataset.error.list");
		}
		return sendSuccessResponse(orgDataSetList);

	}

}
