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
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "org_spi_mapping")
public class OrganizationSpiData extends AbstractAuditableEntity {

	@Id
	@SequenceGenerator(name = "org_spi_mapping_generator", sequenceName = "org_spi_mapping_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_spi_mapping_generator")
	private Long id;

	@JoinColumn(name = "organization_id")
	@ManyToOne(cascade = CascadeType.ALL)
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "spi_id")
	private SpiData spiData;

	@Column(name = "is_checked")
	private Boolean isChecked = false;
}
