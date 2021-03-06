package com.winwin.winwin.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "org_region_served")
public class OrganizationRegionServed extends AbstractAuditableEntity {

	@Id
	@SequenceGenerator(name = "org_region_served_generator", sequenceName = "org_region_served_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_region_served_generator")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "region_master_id")
	private RegionMaster regionMaster;

	@JoinColumn(name = "org_id")
	@ManyToOne(cascade = CascadeType.ALL)
	private Organization organization;

	@Column(name = "is_active")
	private Boolean isActive = true;
}
