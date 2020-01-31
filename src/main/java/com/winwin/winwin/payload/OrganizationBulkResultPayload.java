/**
 * 
 */
package com.winwin.winwin.payload;

import java.util.List;

import com.winwin.winwin.entity.Organization;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Getter
@Setter
public class OrganizationBulkResultPayload {
	private List<Organization> organizationList;
	private List<Organization> successOrganizationList;
	private List<OrganizationBulkFailedPayload> failedOrganizationList;
	private Boolean isFailed;

}
