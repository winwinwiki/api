package com.winwin.winwin.entity;

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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "program_resource")
public class ProgramResource extends AbstractAuditableEntity {

	@Id
	@SequenceGenerator(name = "program_resource_generator", sequenceName = "program_resource_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_resource_generator")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private ResourceCategory resourceCategory;

	@Column(name = "program_id")
	private Long programId;

	@Column(name = "count")
	private Long count;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "is_active")
	private Boolean isActive = true;
}