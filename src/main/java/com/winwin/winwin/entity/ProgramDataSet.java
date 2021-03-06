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
@Table(name = "program_dataset")
public class ProgramDataSet extends AbstractAuditableEntity {

	@Id
	@SequenceGenerator(name = "program_dataset_generator", sequenceName = "program_dataset_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_dataset_generator")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private DataSetCategory dataSetCategory;

	@JoinColumn(name = "program_id")
	@ManyToOne(cascade = CascadeType.ALL)
	private Program program;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "type")
	private String type;

	@Column(name = "url", columnDefinition = "TEXT")
	private String url;

	@Column(name = "is_active")
	private Boolean isActive = true;
}
