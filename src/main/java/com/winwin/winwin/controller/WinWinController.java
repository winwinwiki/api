/**
 * Created for Data Migration 
 */
package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.DataMigrationCsvPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.NaicsDataRepository;
import com.winwin.winwin.repository.NteeDataRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationNoteRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.service.OrgNaicsDataService;
import com.winwin.winwin.service.OrgNteeDataService;
import com.winwin.winwin.service.OrgSdgDataService;
import com.winwin.winwin.service.OrgSpiDataService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.ProgramService;
import com.winwin.winwin.service.SdgDataService;
import com.winwin.winwin.service.SpiDataService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.service.WinWinService;
import com.winwin.winwin.util.CsvUtils;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 *
 */

@RestController
@RequestMapping(value = "/winwin-offline")
public class WinWinController extends BaseController {
	@Autowired
	private WinWinService winWinService;

	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;

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

	@Autowired
	CsvUtils csvUtils;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinController.class);

	/**
	 * 
	 */
	// for offline bulk organization creation
	@RequestMapping(value = "/organization/addAll", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> createOrganizationsOffline(@RequestParam("file") MultipartFile file) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != file) {
			List<DataMigrationCsvPayload> dataMigrationCsvPayload = csvUtils.read(DataMigrationCsvPayload.class, file,
					exceptionResponse);
			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
			UserPayload user = userService.getCurrentUserDetails();
			winWinService.createOrganizationsOffline(dataMigrationCsvPayload, exceptionResponse, user);

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		} else {
			return sendErrorResponse("org.file.null");
		}
		return sendSuccessResponse("org.file.upload.success");
	}

	/**
	 * 
	 */
	// for offline bulk program creation
	@RequestMapping(value = "/program/addAll", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> createProgramsOffline(@RequestParam("file") MultipartFile file) {
		List<Program> programList = new ArrayList<Program>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != file) {
			List<DataMigrationCsvPayload> dataMigrationCsvPayload = csvUtils.read(DataMigrationCsvPayload.class, file,
					exceptionResponse);
			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
			UserPayload user = userService.getCurrentUserDetails();
			programList = winWinService.createProgramsOffline(dataMigrationCsvPayload, exceptionResponse, user);

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		} else {
			return sendErrorResponse("org.file.null");
		}
		return sendSuccessResponse(programList);
	}

}
