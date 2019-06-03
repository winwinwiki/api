package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ArvindKhatik
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionMasterFilterPayload {
	private String nameSearch;
	private Integer pageNo;
	private Integer pageSize;

}