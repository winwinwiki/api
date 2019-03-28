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
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrgRegionServedPayload;
import com.winwin.winwin.repository.OrgRegionServedRepository;
import com.winwin.winwin.service.OrgRegionServedService;

/**
 * @author ArvindKhatik
 *
 */
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
		List<OrgRegionServedPayload> payloadList = new ArrayList<OrgRegionServedPayload>();
		OrgRegionServedPayload payload = null;
		AddressPayload addressPayload = null;
		try {
			orgRegionServedList = orgRegionServedService.createOrgRegionServed(orgRegionServedPayloadList);
			if (null != orgRegionServedList) {
				for (OrgRegionServed region : orgRegionServedList) {
					payload = new OrgRegionServedPayload();
					payload.setId(region.getId());
					payload.setOrganizationId(region.getOrgId());
					if (null != region.getAddress()) {
						addressPayload = new AddressPayload();
						addressPayload.setId(region.getAddress().getId());
						addressPayload.setCountry(region.getAddress().getCountry());
						addressPayload.setState(region.getAddress().getState());
						addressPayload.setCity(region.getAddress().getCity());
						addressPayload.setCounty(region.getAddress().getCounty());
						addressPayload.setZip(region.getAddress().getZip());
						addressPayload.setStreet(region.getAddress().getStreet());
						payload.setAddress(addressPayload);
					}
					payload.setIsActive(region.getIsActive());
					payloadList.add(payload);

				}
			}
		} catch (Exception e) {
			throw new OrgRegionServedException(
					customMessageSource.getMessage("org.region.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);
	}



	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<?> getOrgRegionsList(HttpServletResponse httpServletResponse)
			throws OrgRegionServedException {
		List<OrgRegionServed> orgRegionList = null;
		OrgRegionServedPayload payload = null;
		AddressPayload addressPayload = null;
		List<OrgRegionServedPayload> payloadList = new ArrayList<OrgRegionServedPayload>();
		try {
			orgRegionList = orgRegionServedService.getOrgRegionServedList();
			if (orgRegionList == null) {
				throw new OrgRegionServedException(customMessageSource.getMessage("org.region.error.not_found"));
			} else {
				for (OrgRegionServed region : orgRegionList) {
					payload = new OrgRegionServedPayload();
					payload.setId(region.getId());
					payload.setOrganizationId(region.getOrgId());
					if (null != region.getAddress()) {
						addressPayload = new AddressPayload();
						addressPayload.setId(region.getAddress().getId());
						addressPayload.setCountry(region.getAddress().getCountry());
						addressPayload.setState(region.getAddress().getState());
						addressPayload.setCity(region.getAddress().getCity());
						addressPayload.setCounty(region.getAddress().getCounty());
						addressPayload.setZip(region.getAddress().getZip());
						addressPayload.setStreet(region.getAddress().getStreet());
						payload.setAddress(addressPayload);
					}
					payload.setIsActive(region.getIsActive());
					payloadList.add(payload);

				}

			}
		} catch (Exception e) {
			throw new OrgRegionServedException(customMessageSource.getMessage("org.region.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

}
