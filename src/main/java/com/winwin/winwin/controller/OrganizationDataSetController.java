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

import com.winwin.winwin.payload.OrganizationDataSetCategoryPayLoad;
import com.winwin.winwin.payload.OrganizationDataSetPayLoad;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.service.OrganizationDataSetService;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationDataSetCategory;
import com.winwin.winwin.exception.OrganizationDataSetCategoryException;
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
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	public ResponseEntity updateOrganizationDataSet(
			@Valid @RequestBody OrganizationDataSetPayLoad organizationDataSetPayLoad)
			throws OrganizationDataSetException {
		OrganizationDataSet dataSet = null;
		OrganizationDataSetCategory category = null;
		OrganizationDataSetCategoryPayLoad payloadCategory = null;
		OrganizationDataSetPayLoad payload = new OrganizationDataSetPayLoad();
		if (null != organizationDataSetPayLoad && null != organizationDataSetPayLoad.getId()) {
			Long id = organizationDataSetPayLoad.getId();
			dataSet = organizationDataSetRepository.findOrgDataSetById(id);
			if (dataSet == null) {
				throw new OrganizationDataSetException(customMessageSource.getMessage("org.dataset.error.not_found"));
			} else {
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
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
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

	@RequestMapping(value = "/list/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationDataSetList(HttpServletResponse httpServletResponse,
			@PathVariable("id") Long id) throws OrganizationDataSetException {
		List<OrganizationDataSet> orgDataSetList = null;
		OrganizationDataSetPayLoad payload = null;
		OrganizationDataSetCategory category = null;
		OrganizationDataSetCategoryPayLoad payloadCategory = null;
		List<OrganizationDataSetPayLoad> payloadList = new ArrayList<OrganizationDataSetPayLoad>();
		try {
			orgDataSetList = organizationDataSetService.getOrganizationDataSetList(id);
			if (orgDataSetList == null) {
				throw new OrganizationDataSetException(customMessageSource.getMessage("org.dataset.error.not_found"));
			} else {
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

	@RequestMapping(value = "/categorylist", method = RequestMethod.GET)
	public ResponseEntity<?> getOrganizationDataSetCategoryList(HttpServletResponse httpServletResponce)
			throws OrganizationDataSetCategoryException {
		List<OrganizationDataSetCategory> orgDataSetCategoryList = null;
		List<OrganizationDataSetCategoryPayLoad> payloadList = new ArrayList<OrganizationDataSetCategoryPayLoad>();
		OrganizationDataSetCategoryPayLoad payload = null;
		try {
			orgDataSetCategoryList = organizationDataSetService.getOrganizationDataSetCategoryList();
			if (orgDataSetCategoryList == null) {
				throw new OrganizationDataSetCategoryException(
						customMessageSource.getMessage("org.dataset.category.error.not_found"));
			} else {
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

}
