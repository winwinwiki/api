package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProgramRegionServedPayload {
	Long id;
	RegionMasterPayload region;
	Long programId;
	Long organizationId;
	Boolean isActive;
	private String adminUrl;
}
