package com.winwin.winwin.payload;

import java.math.BigDecimal;

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
public class OrganizationRequestPayload {
	Long id;
	AddressPayload address;
	String name;
	String ein;
	String notes;
	BigDecimal revenue;
	BigDecimal assets;
	String sector;
	String sectorLevel;
	String sectorLevelName;
	String description;
	Long naicsCode;
	Long nteeCode;
	String status;
	String priority;
	Long parentId;
	Boolean isActive;
	Boolean isTaggingReady;
	String tagStatus;
	BigDecimal totalAssets;
	String websiteUrl;
	String facebookUrl;
	String linkedinUrl;
	String twitterUrl;
	String instagramUrl;
	Long classificationId;
	String values;
	String purpose;
	String selfInterest;
	String businessModel;
	String populationServed;
	String missionStatement;
	String contactInfo;
	String datasetType;
	String spiTagIds;
	String sdgTagIds;
	String resourceIds;
	String datasetIds;

}
