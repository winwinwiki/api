package com.winwin.winwin.repository;

import java.util.List;

import com.winwin.winwin.entity.Program;
import com.winwin.winwin.payload.OrganizationFilterPayload;

public interface ProgramFilterRepository {
	List<Program> filterProgram(OrganizationFilterPayload payload, String type, Long orgId);
}
