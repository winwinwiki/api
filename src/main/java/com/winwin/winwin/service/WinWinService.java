/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.DataMigrationCsvPayload;
import com.winwin.winwin.payload.ProgramRequestPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface WinWinService {
	List<Organization> createOrganizationsOffline(List<DataMigrationCsvPayload> organizationPayloadList,
			ExceptionResponse response);

	List<Program> createProgramsOffline(List<ProgramRequestPayload> programPayloadList, ExceptionResponse response);

}
