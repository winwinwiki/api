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
 * @author ArvindKhatik
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class OrgSpiDataComponentsPayload {
	String componentId;
	String componentName;
	List<OrgSpiDataIndicatorsPayload> indicators;

}
