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
	public List<String> createdBy;
	public List<String> editedBy;
	public String nameSearch;
	public Integer pageNo;
	public Integer pageSize;
	public String sortBy;
	public String sortOrder;

}