/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.OrganizationDataMigrationCsvPayload;
import com.winwin.winwin.payload.ProgramDataMigrationCsvPayload;
import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
public interface WinWinService {
	void createOrganizationsOffline(List<OrganizationDataMigrationCsvPayload> organizationPayloadList,
			ExceptionResponse response, UserPayload user);

	void createProgramsOffline(List<ProgramDataMigrationCsvPayload> programPayloadList, ExceptionResponse response,
			UserPayload user);

}
