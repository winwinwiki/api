package com.winwin.winwin.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.service.OrganizationHistoryService;

@Service
public class OrganizationHistoryServiceImpl implements OrganizationHistoryService {

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Override
	public void createOrganizationHistory(UserPayload user, Long orgId, SimpleDateFormat sdf, String formattedDte,
			String actionPerformed, String entityType, Long entityId, String entityName) {
		// TODO Auto-generated method stub
		try {
			OrganizationHistory orgHistory = new OrganizationHistory();
			orgHistory.setOrganizationId(orgId);
			orgHistory.setEntityId(entityId);
			orgHistory.setEntityName(entityName);
			orgHistory.setEntityType(entityType);

			orgHistory.setUpdatedAt(sdf.parse(formattedDte));

			orgHistory.setUpdatedBy(user.getUserDisplayName());
			orgHistory.setActionPerformed(actionPerformed);
			orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
		} catch (ParseException e) {

		}

	}

}
