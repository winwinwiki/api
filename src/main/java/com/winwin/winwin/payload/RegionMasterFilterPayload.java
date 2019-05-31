package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ArvindKhatik
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegionMasterFilterPayload {
	private String nameSearch;

	public String getNameSearch() {
		return nameSearch;
	}
}