package com.winwin.winwin.payload;

import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;

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
public class ProgramResponsePayload {
	Long id;
	AddressPayload address;
	String name;
	Long revenue;
	Long assets;
	String sector;
	String sectorLevel;
	String sectorLevelName;
	String description;
	NaicsData naicsCode;
	NteeData nteeCode;
	String priority = "Normal";
	Long organizationId;
	Boolean isActive = true;
	String tagStatus;
	String websiteUrl;
	String facebookUrl;
	String linkedinUrl;
	String twitterUrl;
	String instagramUrl;
	String values;
	String purpose;
	String selfInterest;
	String businessModel;
	Long populationServed;
	String missionStatement;
	String contactInfo;
	String notes;
	Long parentId;
	String resourceIds;
	String datasetIds;
	String datasetType;
	String spiTagIds;
	String sdgTagIds;
}
