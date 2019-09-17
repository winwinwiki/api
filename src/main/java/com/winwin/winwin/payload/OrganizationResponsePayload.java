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

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class OrganizationResponsePayload {
	private Long id;
	private AddressPayload address;
	private String name;
	private BigDecimal revenue;
	private BigDecimal assets;
	private String sector;
	private String sectorLevel;
	private String sectorLevelName;
	private String description;
	private NaicsData naicsCode;
	private NteeData nteeCode;
	private String status;
	private String priority;
	private String ein;
	private String notes;
	private Long parentId;
	private String parentName;
	private Long rootParentId;
	private String rootParentName;
	private Boolean isActive;
	private Boolean isTaggingReady;
	private String tagStatus;
	private BigDecimal totalAssets;
	private String websiteUrl;
	private String facebookUrl;
	private String linkedinUrl;
	private String twitterUrl;
	private String instagramUrl;
	private Long classificationId;
	private String values;
	private String purpose;
	private String selfInterest;
	private String businessModel;
	private String populationServed;
	private String missionStatement;
	private String contactInfo;
	private String adminUrl;
	private String resourceIds;
	private String datasetIds;
	private String datasetType;
	private String spiTagIds;
	private String sdgTagIds;
	private String createdBy;
	private String createdByEmail;
	private String lastEditedBy;
	private String lastEditedByEmail;
	private Date lastEditedAt;
}