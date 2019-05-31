package com.winwin.winwin.payload;

import java.util.List;

import javax.persistence.Column;

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
	Long revenue;
	Long assets;
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
	List<Long> spiTagIds;
	List<Long> sdgTagIds;

}
