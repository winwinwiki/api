package com.winwin.winwin.payload;

import java.math.BigDecimal;
import java.util.Date;

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
public class OrganizationResponsePayload {
	Long id;
	AddressPayload address;
	String name;
	BigDecimal revenue;
	BigDecimal assets;
	String sector;
	String sectorLevel;
	String sectorLevelName;
	String description;
	NaicsData naicsCode;
	NteeData nteeCode;
	String status;
	String priority;
	String ein;
	String notes;
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
	String adminUrl;
	String resourceIds;
	String datasetIds;
	String datasetType;
	String spiTagIds;
	String sdgTagIds;
	String createdBy;
	String createdByEmail;
	String lastEditedBy;
	String lastEditedByEmail;
	Date lastEditedAt;
}