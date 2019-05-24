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
public class NaicsDataMappingPayload {

	private String naicsCode;
	private List<Long>spiTagIds;
	private List<Long>sdgTagIds;
	
	@Override
	public int hashCode(){
		return naicsCode.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		NaicsDataMappingPayload other=(		NaicsDataMappingPayload)o;
		return naicsCode.equals(other.naicsCode);
	}
}
