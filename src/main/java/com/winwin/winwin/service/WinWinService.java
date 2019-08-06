/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.Program;
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
	List<Organization> createOrganizationsOffline(List<OrganizationDataMigrationCsvPayload> organizationPayloadList,
			ExceptionResponse response, UserPayload user);

	List<Program> createProgramsOffline(List<ProgramDataMigrationCsvPayload> programPayloadList,
			ExceptionResponse response, UserPayload user);

}
