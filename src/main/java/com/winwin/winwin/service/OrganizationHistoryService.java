package com.winwin.winwin.service;

import java.text.SimpleDateFormat;

import com.winwin.winwin.payload.UserPayload;

public interface OrganizationHistoryService {
	void createOrganizationHistory(UserPayload user, Long orgId, SimpleDateFormat sdf, String formattedDte,
			String actionPerformed, String entityType, Long entityId, String entityName);
}
