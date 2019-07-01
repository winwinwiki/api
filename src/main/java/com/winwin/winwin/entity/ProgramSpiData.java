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
@Table(name = "program_spi_mapping")
public class ProgramSpiData extends AbstractAuditableEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_spi_mapping_seq")
	@Column(name = "id")
	private Long id;

	@Column(name = "program_id")
	private Long programId;

	@ManyToOne
	@JoinColumn(name = "spi_id")
	private SpiData spiData;

	@Column(name = "is_checked")
	private Boolean isChecked = false;
}
