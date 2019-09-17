package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.Program;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.ProgramFilterPayloadData;
import com.winwin.winwin.payload.ProgramRequestPayload;
import com.winwin.winwin.payload.ProgramResponsePayload;
import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
public interface ProgramService {
	public Program createProgram(ProgramRequestPayload programPayload, ExceptionResponse exceptionResponse);

	void deleteProgram(Program program, ExceptionResponse exceptionResponse);

	Program updateProgram(ProgramRequestPayload organizationPayload, ExceptionResponse exceptionResponse);

	List<Program> getProgramList(Long orgId);

	List<Program> getProgramList(ProgramFilterPayloadData payload, Long orgId, ExceptionResponse response);

	ProgramResponsePayload getProgramResponseFromProgram(Program payload);

	Program getProgramFromProgramRequestPayload(ProgramRequestPayload payload, UserPayload user);

}
