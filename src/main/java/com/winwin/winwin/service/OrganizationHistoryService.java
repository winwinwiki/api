package com.winwin.winwin.service;

import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationHistoryService {
	void createOrganizationHistory(UserPayload user, Long orgId, String actionPerformed, String entityType,
			Long entityId, String entityName);

	void createOrganizationHistory(UserPayload user, Long orgId, Long programId, String actionPerformed,
			String entityType, Long entityId, String entityName);
}
