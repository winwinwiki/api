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
public class OrganizationCsvPayload {
	private Long id;
	private Long addressId;
	private String name;
	private Long revenue;
	private Long assets;
	private String sector;
	private String sectorLevel;
	private String sectorLevelName;
	private String description;
	private String naicsCode;
	private String nteeCode;
	private String status;
	private String priority;
	private Long parentId;
	private Boolean isActive;
	private Boolean isTaggingReady;
	private String tagStatus;
	private Long totalAssets;
	private String websiteUrl;
	private String facebookUrl;
	private String linkedinUrl;
	private String twitterUrl;
	private Long classificationId;
	private String values;
	private String purpose;
	private String selfInterest;
	private String businessModel;
	private Long populationServed;
	private String missionStatement;
	private String contactInfo;
	private String country;
	private String state;
	private String city;
	private String county;
	private Long zip;
	private String street;
}
