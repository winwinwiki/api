package com.winwin.winwin.payload;

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
public class OrganizationSpiDataMapPayload {
	Long id;
	Long organizationId;
	Long dimensionId;
	String dimensionName;
	String componentId;
	String componentName;
	String indicatorId;
	String indicatorName;
	Boolean isChecked; 

}
