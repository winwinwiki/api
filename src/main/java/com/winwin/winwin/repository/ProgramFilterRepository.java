package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.winwin.winwin.entity.Program;
import com.winwin.winwin.payload.OrganizationFilterPayload;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Repository
public interface ProgramFilterRepository {
	List<Program> filterProgram(OrganizationFilterPayload payload, String type, Long orgId);
}
