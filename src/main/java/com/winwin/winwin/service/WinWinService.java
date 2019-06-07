/**
 * 
 */
package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.OrganizationRequestPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface WinWinService {
	List<Organization> createOrganizationsOffline(List<OrganizationRequestPayload> organizationPayloadList,
			ExceptionResponse response);

}
