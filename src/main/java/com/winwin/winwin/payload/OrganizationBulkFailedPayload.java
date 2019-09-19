/**
 * 
 */
package com.winwin.winwin.payload;

import com.winwin.winwin.entity.Organization;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindK
 * @version 1.0
 */
@Getter
@Setter
public class OrganizationBulkFailedPayload {
	private Organization failedOrganization;
	private String failedMessage;

}