/**
 * Created for Data Migration 
 */
package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.AddressPayload;
import com.winwin.winwin.payload.OrganizationCsvPayload;
import com.winwin.winwin.payload.OrganizationRequestPayload;
import com.winwin.winwin.payload.OrganizationResponsePayload;
import com.winwin.winwin.repository.NaicsDataRepository;
import com.winwin.winwin.repository.NteeDataRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationNoteRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.service.OrgNaicsDataService;
import com.winwin.winwin.service.OrgNteeDataService;
import com.winwin.winwin.service.OrgSdgDataService;
import com.winwin.winwin.service.OrgSpiDataService;
import com.winwin.winwin.service.OrganizationDataSetService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.OrganizationRegionServedService;
import com.winwin.winwin.service.OrganizationResourceService;
import com.winwin.winwin.service.OrganizationService;
import com.winwin.winwin.service.ProgramService;
import com.winwin.winwin.service.SdgDataService;
import com.winwin.winwin.service.SpiDataService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CsvUtils;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 *
 */


@RestController
@RequestMapping( value = "/winwin-offline" )
public class WinWinController extends BaseController {
	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationDataSetService organizationDataSetService;

	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;

	@Autowired
	private OrganizationResourceService organizationResourceService;

	@Autowired
	private OrganizationResourceRepository organizationResourceRepository;

	@Autowired
	private OrganizationRegionServedService orgRegionServedService;

	@Autowired
	OrgSpiDataService orgSpiDataService;

	@Autowired
	OrgSdgDataService orgSdgDataService;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationNoteService organizationNoteService;

	@Autowired
	OrganizationNoteRepository organizationNoteRepository;

	@Autowired
	OrgNaicsDataService naicsDataService;
	@Autowired
	OrgNteeDataService nteeDataService;

	@Autowired
	ProgramService programService;
	@Autowired
	ProgramRepository programRepository;

	@Autowired
	SpiDataService spiDataService;
	@Autowired
	SdgDataService sdgDataService;

	@Autowired
	NteeDataRepository nteeDataRepository;

	@Autowired
	NaicsDataRepository naicsDataRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinController.class);

	/**
	 * 
	 */
	//for offline bulk organization creation
	@RequestMapping(value = "/addAll", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrganizationsOffline(@RequestParam("file") MultipartFile file) {
		List<OrganizationRequestPayload> organizationPayloadList = new ArrayList<>();
		List<Organization> organizationList = null;
		List<OrganizationResponsePayload> payloadList = null;
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != file) {
			List<OrganizationCsvPayload> organizationCsvPayload = CsvUtils.read(OrganizationCsvPayload.class, file);
			organizationPayloadList = organizationCsvPayload.stream().map(this::setOrganizationPayload)
					.collect(Collectors.toList());
			organizationList = organizationService.createOrganizations(organizationPayloadList, exceptionResponse);
			payloadList = setOrganizationPayload(organizationList);

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		} else {
			return sendErrorResponse("org.file.null");
		}
		return sendSuccessResponse(payloadList);
	}
	
	/**
	 * @param organization
	 * @param payload
	 * @return
	 */
	private List<OrganizationResponsePayload> setOrganizationPayload(List<Organization> organizationList) {
		List<OrganizationResponsePayload> payload = new ArrayList<>();
		for (int i = 0; i < organizationList.size(); i++)
			payload.add(setOrganizationPayload(organizationList.get(i)));
		return payload;
	}

	private OrganizationResponsePayload setOrganizationPayload(Organization organization) {
		AddressPayload addressPayload;
		OrganizationResponsePayload payload = null;
		if (null != organization) {
			payload = new OrganizationResponsePayload();
			BeanUtils.copyProperties(organization, payload);
			if (null != organization.getAddress()) {
				addressPayload = new AddressPayload();
				BeanUtils.copyProperties(organization.getAddress(), addressPayload);
				payload.setAddress(addressPayload);
			}
		}
		return payload;
	}
	
	private OrganizationRequestPayload setOrganizationPayload(OrganizationCsvPayload csv) {
		OrganizationRequestPayload payload = new OrganizationRequestPayload();
		AddressPayload address = new AddressPayload();
		BeanUtils.copyProperties(csv, address);
		payload.setAddress(address);
		BeanUtils.copyProperties(csv, payload);

		// Get Id from naics_code master data table and assign the id of it
		NaicsData naicsData = naicsDataRepository.findByCode(csv.getNaicsCode());
		payload.setNaicsCode(naicsData.getId());

		// Get Id from ntee_code master data table and assign the id of it
		NteeData nteeData = nteeDataRepository.findByCode(csv.getNteeCode());
		payload.setNteeCode(nteeData.getId());

		return payload;
	}

}
