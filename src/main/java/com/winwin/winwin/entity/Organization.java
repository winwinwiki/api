package com.winwin.winwin.entity;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
@Entity
@Table(name = "organization")
public class Organization extends AbstractAuditableEntity {

	@Id
	@SequenceGenerator(name = "organization_generator", sequenceName = "organization_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_generator")
	private Long id;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "address_id")
	private Address address;

	@Column(name = "name")
	private String name;

	@Column(name = "ein")
	private String ein;

	@Column(name = "revenue")
	private BigDecimal revenue;

	@Column(name = "assets")
	private BigDecimal assets;

	@Column(name = "sector")
	private String sector;

	@Column(name = "sector_level")
	private String sectorLevel;

	@Column(name = "sector_level_name")
	private String sectorLevelName;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@ManyToOne
	@JoinColumn(name = "naics_code")
	private NaicsData naicsCode;

	@ManyToOne
	@JoinColumn(name = "ntee_code")
	private NteeData nteeCode;

	@Column(name = "priority")
	private String priority = "Normal";

	@Column(name = "parent_id")
	private Long parentId;

	@Column(name = "type")
	private String type;

	@Column(name = "is_active")
	private Boolean isActive = true;

	@Column(name = "tag_status")
	private String tagStatus;

	@Column(name = "website_url")
	private String websiteUrl;

	@Column(name = "facebook_url")
	private String facebookUrl;

	@Column(name = "linkedin_url")
	private String linkedinUrl;

	@Column(name = "twitter_url")
	private String twitterUrl;

	@Column(name = "instagram_url")
	private String instagramUrl;

	@Column(name = "values", columnDefinition = "TEXT")
	String values;

	@Column(name = "purpose", columnDefinition = "TEXT")
	String purpose;

	@Column(name = "self_interest", columnDefinition = "TEXT")
	private String selfInterest;

	@Column(name = "business_model", columnDefinition = "TEXT")
	String businessModel;

	@Column(name = "population_served")
	String populationServed;

	@Column(name = "mission_statement", columnDefinition = "TEXT")
	String missionStatement;

	@Column(name = "contact_info", columnDefinition = "TEXT")
	String contactInfo;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<OrganizationNote> note;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<OrganizationSpiData> organizationSpiData;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<OrganizationSdgData> organizationSdgData;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<OrganizationRegionServed> organizationRegionServed;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<OrganizationResource> organizationResource;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<OrganizationDataSet> organizationDataSet;
}
