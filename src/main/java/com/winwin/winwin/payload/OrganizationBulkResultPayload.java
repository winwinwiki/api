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
 * @version 1.0
 */
@Getter
@Setter
public class OrganizationBulkResultPayload {
	private List<Organization> organizationList;
	Boolean isFailed;

}
