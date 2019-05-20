package com.winwin.winwin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
@Entity
@Table(name = "program")
public class Program extends AbstractAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "address_id")
	private Address address;

	@Column(name = "name")
	private String name;

	@Column(name = "revenue")
	private Long revenue;

	@Column(name = "assets")
	private Long assets;

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

	@OneToOne
	@JoinColumn(name = "org_id")
	private Organization orgId;

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

	@Column(name = "values", columnDefinition = "TEXT")
	String values;

	@Column(name = "purpose", columnDefinition = "TEXT")
	String purpose;

	@Column(name = "self_interest", columnDefinition = "TEXT")
	private String selfInterest;

	@Column(name = "business_model", columnDefinition = "TEXT")
	String businessModel;

	@Column(name = "population_served")
	Long populationServed;

	@Column(name = "mission_statement", columnDefinition = "TEXT")
	String missionStatement;

	@Column(name = "contact_info", columnDefinition = "TEXT")
	String contactInfo;

	@Column(name = "admin_url", columnDefinition = "TEXT")
	private String adminUrl;

}