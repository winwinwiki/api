/**
 * 
 */
package com.winwin.winwin.payload;

import java.util.List;

import com.winwin.winwin.entity.Organization;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindK
 *
 */
@Getter
@Setter
public class OrganizationBulkResultPayload {
	private List<Organization> organizationList;
	Boolean isFailed;

}
