package com.winwin.winwin.entity;

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
@Table(name = "org_classification_mapping")
public class OrganizationClassification extends AbstractAuditableEntity {

	@Id
	@SequenceGenerator(name = "org_classification_mapping_generator", sequenceName = "org_classification_mapping_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_classification_mapping_generator")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "org_id")
	private Organization orgId;

	@ManyToOne
	@JoinColumn(name = "classification_id")
	private Classification classificationId;
}
