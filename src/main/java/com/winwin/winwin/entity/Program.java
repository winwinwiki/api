package com.winwin.winwin.entity;

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
import javax.validation.constraints.NotBlank;

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
@Table(name = "program")
public class Program extends AbstractAuditableEntity {

	@Id
	@SequenceGenerator(name = "program_generator", sequenceName = "program_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_generator")
	private Long id;

	@NotBlank
	@Column(name = "name", nullable=false)
	private String name;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "org_id")
	private Organization organization;

	@Column(name = "is_active")
	private Boolean isActive = true;

	@Column(name = "website_url", columnDefinition = "TEXT")
	private String websiteUrl;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "program")
	private List<ProgramSpiData> programSpiData;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "program")
	private List<ProgramSdgData> programSdgData;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "program")
	private List<ProgramRegionServed> programRegionServed;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "program")
	private List<ProgramResource> programResource;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "program")
	private List<ProgramDataSet> programDataSet;
}