package com.winwin.winwin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
@Entity
@Table(name = "organization")
public class Organization extends AbstractAuditableEntity {

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

	@Column(name = "description")
	private String description;

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

	@Column(name = "social_url")
	private String socialUrl;

	@Column(name = "key_activities")
	String keyActivities;

	@Column(name = "org_driver")
	String orgDriver;

	@Column(name = "business_model")
	String businessModel;

	@Column(name = "population_served")
	Long populationServed;

	@Column(name = "mission_statement")
	String missionStatement;
	
	@Column(name = "contact_info")
	String contactInfo;

}
