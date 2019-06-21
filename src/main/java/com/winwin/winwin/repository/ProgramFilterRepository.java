package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.Program;
import com.winwin.winwin.payload.OrganizationFilterPayload;

@Repository
public interface ProgramFilterRepository {
	List<Program> filterProgram(OrganizationFilterPayload payload, String type, Long orgId);
}
