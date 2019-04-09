/**
 * 
 */
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
public class OrgRegionServedPayload {
	Long id;
	OrgRegionMasterPayload orgRegionMasterPayload;
	Long organizationId;
	Boolean isActive;
}
