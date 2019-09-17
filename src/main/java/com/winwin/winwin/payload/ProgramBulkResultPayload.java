/**
 * 
 */
package com.winwin.winwin.payload;

import java.util.List;

import com.winwin.winwin.entity.Program;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Getter
@Setter
public class ProgramBulkResultPayload {
	private List<Program> programList;
	Boolean isFailed;

}
