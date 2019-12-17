package com.winwin.winwin.payload;

import com.winwin.winwin.entity.Program;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Getter
@Setter
public class ProgramBulkFailedPayload {
	private Program failedProgram;
	private String failedMessage;

}