/**
 * 
 */
package com.winwin.winwin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "org_spi_data")
public class OrgSpiData {
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "dimension_id")
	private Long dimensionId;

	@Column(name = "dimension_name")
	private String dimensionName;

	@Column(name = "component_id")
	private Long componentId;

	@Column(name = "component_name")
	private String componentName;

	@Column(name = "indicator_id")
	private Long indicatorId;

	@Column(name = "indicator_name")
	private String indicatorName;

}
