/**
 * Controller to upload Data of Organization and Program via respective endpoint's
 * This is an Data Migration one time activity for fresh DB Setup 
 */
package com.winwin.winwin.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.OrganizationDataMigrationCsvPayload;
import com.winwin.winwin.payload.ProgramDataMigrationCsvPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.service.WinWinService;
import com.winwin.winwin.util.CsvUtils;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */

@RestController
@RequestMapping(value = "/winwin-offline")
public class WinWinController extends BaseController {
	@Autowired
	private WinWinService winWinService;
	@Autowired
	private UserService userService;
	@Autowired
	private CsvUtils csvUtils;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinController.class);

	/**
	 * end point to create organizations in bulk, call this method only when the
	 * organization id's are already imported into organization table. end point
	 * method for data migration and can only be used for new environment setup
	 * 
	 * @param file
	 * @return
	 */
	@PostMapping(path = "/organization/addAll")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> createOrganizationsOffline(@RequestParam("file") MultipartFile file) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != file) {
			List<OrganizationDataMigrationCsvPayload> dataMigrationCsvPayload = csvUtils
					.read(OrganizationDataMigrationCsvPayload.class, file, exceptionResponse);
			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
			UserPayload user = userService.getCurrentUserDetails();
			if (null != user)
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
	 * end point to create organizations in bulk, call this method only when the
	 * organization id's are already imported into organization table. end point
	 * method for data migration and can only be used for new environment setup
	 * 
	 * @param file
	 * @return
	 */
	@PostMapping(path = "/program/addAll")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> createProgramsOffline(@RequestParam("file") MultipartFile file) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != file) {
			List<ProgramDataMigrationCsvPayload> dataMigrationCsvPayload = csvUtils
					.read(ProgramDataMigrationCsvPayload.class, file, exceptionResponse);
			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
			UserPayload user = userService.getCurrentUserDetails();
			if (null != user)
				winWinService.createProgramsOffline(dataMigrationCsvPayload, exceptionResponse, user);

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		} else {
			return sendErrorResponse("org.file.null");
		}
		return sendSuccessResponse("org.file.upload.success");
	}

}
