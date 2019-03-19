package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.winwin.winwin.entity.OrgRegionServed;
import com.winwin.winwin.exception.OrgRegionServedException;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.payload.OrgRegionServedPayload;
import com.winwin.winwin.repository.OrgRegionServedRepository;
import com.winwin.winwin.service.OrgRegionServedService;

@RestController
@RequestMapping(value = "/orgregions")
public class OrgRegionServedController extends BaseController {

	@Autowired
	private OrgRegionServedService orgRegionServedService;

	@Autowired
	private OrgRegionServedRepository orgRegionServedRepository;

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity createOrgRegions(HttpServletResponse httpServletResponse,
			@RequestBody List<OrgRegionServedPayload> orgRegionServedPayloadList) throws OrgRegionServedException {
		List<OrgRegionServed> orgRegionServedList = null;
		try {
			orgRegionServedList = orgRegionServedService.createOrgRegionServed(orgRegionServedPayloadList);
		} catch (Exception e) {
			throw new OrgRegionServedException(
					customMessageSource.getMessage("org.region.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(orgRegionServedList);
	}

	/**
	 * @param httpServletResponse
	 * @param orgRegionServedPayloadList
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@Transactional
	public ResponseEntity updateOrgRegions(HttpServletResponse httpServletResponse,
			@RequestBody List<OrgRegionServedPayload> orgRegionServedPayloadList) {
		OrgRegionServed orgRegionServed = null;
		List<OrgRegionServed> orgRegionServedList = new ArrayList<OrgRegionServed>();
		try {
			for (OrgRegionServedPayload payload : orgRegionServedPayloadList) {
				orgRegionServed = orgRegionServedRepository.findOrgRegionById(payload.getId());
				if (orgRegionServed == null) {
					throw new OrgRegionServedException(customMessageSource.getMessage("org.region.error.not_found"));
				} else {
					orgRegionServed = orgRegionServedService.updateOrgRegionServed(payload, orgRegionServed);
					orgRegionServedList.add(orgRegionServed);
				}
			}
		} catch (Exception e) {
			throw new OrganizationException(
					customMessageSource.getMessage("org.region.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(orgRegionServedList);

	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<?> getOrgRegionsList(HttpServletResponse httpServletResponse)
			throws OrgRegionServedException {
		List<OrgRegionServed> orgRegionList = null;
		try {
			orgRegionList = orgRegionServedService.getOrgRegionServedList();
			if (orgRegionList == null) {
				throw new OrgRegionServedException(customMessageSource.getMessage("org.region.error.not_found"));
			}
		} catch (Exception e) {
			throw new OrgRegionServedException(customMessageSource.getMessage("org.region.error.list"));
		}
		return sendSuccessResponse(orgRegionList);

	}

}
