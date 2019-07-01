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
@Table(name = "org_dataset")
public class OrganizationDataSet extends AbstractAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_dataset_seq")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private DataSetCategory dataSetCategory;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "type")
	private String type;

	@Column(name = "url", columnDefinition = "TEXT")
	private String url;

	@Column(name = "is_active")
	private Boolean isActive = true;
}
