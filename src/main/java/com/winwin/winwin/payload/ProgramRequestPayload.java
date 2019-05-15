package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class ProgramRequestPayload {

	private Long id;

	AddressPayload address;

	private String name;

	private Long revenue;

	private Long assets;

	private String sector;

	private String sectorLevel;

	private String sectorLevelName;

	private String description;

	private Long naicsCode;

	private Long nteeCode;

	private String priority = "Normal";

	private Long orgId;

	private Boolean isActive = true;

	private String tagStatus;

	private String websiteUrl;

	private String facebookUrl;

	private String linkedinUrl;

	private String twitterUrl;

	String values;

	String purpose;

	private String selfInterest;

	String businessModel;

	Long populationServed;

	String missionStatement;

	String contactInfo;
}
