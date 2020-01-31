package com.winwin.winwin.payload;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 *
 */
@Getter
@Setter
public class NteeDataMappingPayload {

	private String nteeCode;
	private List<Long> spiTagIds;
	private List<Long> sdgTagIds;

	@Override
	public int hashCode() {
		return nteeCode.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (this.getClass() != o.getClass())
			return false;

		NteeDataMappingPayload obj = (NteeDataMappingPayload) o;
		return nteeCode.equals(obj.nteeCode);
	}
}
