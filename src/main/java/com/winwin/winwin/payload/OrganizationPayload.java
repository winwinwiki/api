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
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class OrganizationPayload {
	Long id;
	AddressPayload address;
	String name;
	Long revenue;
	Long assets;
	String sector;
	String sectorLevel;
	String sectorLevelName;
	String description;
	String naicsCode;
	String nteeCode;
	String status;
	String priority;
	Long parentId;
	Boolean isActive;
	Boolean isTaggingReady;
	String tagStatus;
	Long totalAssets;
	String websiteUrl;
	String facebookUrl;
	String linkedinUrl;
	String twitterUrl;
	Long classificationId;
	String values;
	String purpose;
	String selfInterest;
	String businessModel;
	Long populationServed;
	String missionStatement;
	String contactInfo;
	
}
