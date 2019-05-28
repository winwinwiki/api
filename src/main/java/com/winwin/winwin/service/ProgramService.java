package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.Program;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.payload.ProgramRequestPayload;
import com.winwin.winwin.payload.ProgramResponsePayload;

public interface ProgramService {
	public Program createProgram(ProgramRequestPayload programPayload);

	void deleteProgram(Long id);

	Program updateProgram(ProgramRequestPayload organizationPayload);

	List<Program> getProgramList(Long orgId);

	List<Program> getProgramList(OrganizationFilterPayload payload, Long orgId, ExceptionResponse response);

	ProgramResponsePayload getProgramResponseFromProgram(Program payload);

	Program getProgramFromProgramRequestPayload(ProgramRequestPayload payload);

}
