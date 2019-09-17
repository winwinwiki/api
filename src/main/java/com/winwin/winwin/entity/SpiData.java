/**
 * 
 */
package com.winwin.winwin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "spi_data")
public class SpiData {
	@Id
	@SequenceGenerator(name = "spi_data_generator", sequenceName = "spi_data_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spi_data_generator")
	private Long id;

	@Column(name = "dimension_id")
	private Long dimensionId;

	@Column(name = "dimension_name")
	private String dimensionName;

	@Column(name = "component_id")
	private String componentId;

	@Column(name = "component_name")
	private String componentName;

	@Column(name = "indicator_id")
	private String indicatorId;

	@Column(name = "indicator_name")
	private String indicatorName;

	@Column(name = "indicator_definition", columnDefinition = "TEXT")
	private String indicatorDefinition;

	@Column(name = "indicator_description", columnDefinition = "TEXT")
	private String indicatorDescription;

	@Column(name = "spi_source")
	private String spiSource;

	@Column(name = "is_active")
	private Boolean isActive;

}
