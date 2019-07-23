package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindKhatik
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class ProgramRequestPayload {
	Long id;
	AddressPayload address;
	String name;
	Long revenue;
	Long assets;
	String sector;
	String sectorLevel;
	String sectorLevelName;
	String description;
	Long naicsCode;
	Long nteeCode;
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
	String populationServed;
	String missionStatement;
	String contactInfo;
	String notes;
	Long parentId;
	String datasetType;
	String spiTagIds;
	String sdgTagIds;
	String resourceIds;
	String datasetIds;
}
