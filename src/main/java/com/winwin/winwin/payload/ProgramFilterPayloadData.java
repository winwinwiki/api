package com.winwin.winwin.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramFilterPayloadData {
	private List<String> createdBy;
	private List<String> editedBy;
	private String nameSearch;
	private Integer pageNo;
	private Integer pageSize;
	private String sortBy;
	private String sortOrder;

}