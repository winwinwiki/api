/**
 * 
 */
package com.winwin.winwin.payload;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Getter
@Setter
public class OrganizationElasticSearchPayload {
	private Long id;
	private String businessModel;
	private String contactInfo;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdByEmail;
	private String updatedByEmail;
	private String ein;
	private String facebookUrl;
	private String instagramUrl;
	private Boolean isActive;
	private String linkedinUrl;
	private String missionStatement;
	private String naics_code;
	private String name;
	private String ntee_code;
	private String populationServed;
	private String priority;
	private String purpose;
	private BigDecimal assets;
	private BigDecimal revenue;
	private String sector;
	private String sectorLevel;
	private String sectorLevelName;
	private String selfInterest;
	private String tagStatus;
	private String twitterUrl;
	private String type;
	private String values;
	private String websiteUrl;
	private String adminUrl;
	private Long parentId;
	private Long rootParentId;
	private String parentName;
	private String rootParentName;
	private AddressElasticSearchPayload address;
	private List<ProgramElasticSearchPayload> programs;
	private List<OrganizationNoteElasticSearchPayload> notes;
	private List<OrganizationDataSetElasticSearchPayload> datasets;
	private List<OrganizationResourceElasticSearchPayload> resources;
	private List<OrganizationRegionServedElasticSearchPayload> regionServed;
	private List<OrganizationSpiElasticSearchPayload> spi;
	private List<OrganizationSdgElasticSearchPayload> sdg;

}
