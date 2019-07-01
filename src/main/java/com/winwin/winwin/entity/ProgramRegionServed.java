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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "program_region_served")
public class ProgramRegionServed extends AbstractAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_region_served_seq")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "region_master_id")
	private RegionMaster regionMaster;

	@Column(name = "program_id")
	private Long programId;

	@Column(name = "is_active")
	private Boolean isActive = true;
}
