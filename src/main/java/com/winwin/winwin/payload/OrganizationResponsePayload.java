package com.winwin.winwin.payload;

import javax.persistence.Column;

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
	Long revenue;
	Long assets;
	String sector;
	String sectorLevel;
	String sectorLevelName;
	String description;
	NaicsData naicsCode;
	NteeData nteeCode;
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
	String instagramUrl;
	Long classificationId;
	String values;
	String purpose;
	String selfInterest;
	String businessModel;
	Long populationServed;
	String missionStatement;
	String contactInfo;
	String adminUrl;
}