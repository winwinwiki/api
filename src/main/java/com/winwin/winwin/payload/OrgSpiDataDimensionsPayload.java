/**
 * 
 */
package com.winwin.winwin.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindK
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class OrgSpiDataDimensionsPayload {
	Long dimensionId;
	String dimensionName;
	List<OrgSpiDataComponentsPayload> components;

}
