package com.winwin.winwin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

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
@Table(name = "dataset_category")
public class DataSetCategory extends AbstractAuditableEntity {
	@Id
	@SequenceGenerator(name = "dataset_category_generator", sequenceName = "dataset_category_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataset_category_generator")
	private Long id;

	@NotBlank
	@Column(name = "category", columnDefinition = "TEXT", nullable = false)
	private String categoryName;
}
