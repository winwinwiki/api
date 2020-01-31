package com.winwin.winwin.payload;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Getter
@Setter
public class NaicsDataMappingPayload {

	private String naicsCode;
	private List<Long> spiTagIds;
	private List<Long> sdgTagIds;

	@Override
	public int hashCode() {
		return naicsCode.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (this.getClass() != o.getClass())
			return false;

		NaicsDataMappingPayload obj = (NaicsDataMappingPayload) o;
		return naicsCode.equals(obj.naicsCode);
	}
}
