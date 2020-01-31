package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationHistoryService {
	void createOrganizationHistory(UserPayload user, Long orgId, String actionPerformed, String entityType,
			Long entityId, String entityName, String entityCode);

	void createOrganizationHistory(UserPayload user, Long orgId, Long programId, String actionPerformed,
			String entityType, Long entityId, String entityName, String entityCode);

	void createOrganizationHistory(List<OrganizationHistory> orgHistoryList);
}
