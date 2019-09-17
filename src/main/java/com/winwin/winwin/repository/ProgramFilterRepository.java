package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.winwin.winwin.entity.Program;
import com.winwin.winwin.payload.ProgramFilterPayloadData;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Repository
public interface ProgramFilterRepository {
	List<Program> filterProgram(ProgramFilterPayloadData payload, String type, Long orgId);
}
