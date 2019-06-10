package com.winwin.winwin.service.impl;

import java.util.Date;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class OrganizationHistoryServiceImpl implements OrganizationHistoryService {

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationHistoryServiceImpl.class);

	@Override
	@Transactional
	public void createOrganizationHistory(UserPayload user, Long orgId, String actionPerformed, String entityType,
			Long entityId, String entityName) {
		try {
			OrganizationHistory orgHistory = new OrganizationHistory();
			Date date = CommonUtils.getFormattedDate();
			orgHistory.setOrganizationId(orgId);
			orgHistory.setEntityId(entityId);
			orgHistory.setEntityName(entityName);
			orgHistory.setEntityType(entityType);

			orgHistory.setUpdatedAt(date);

			orgHistory.setUpdatedBy(user.getUserDisplayName());
			orgHistory.setActionPerformed(actionPerformed);
			orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
		} catch (Exception e) {
			LOGGER.error("exception occured while creating history", e);
		}

	}

	@Override
	@Transactional
	public void createOrganizationHistory(UserPayload user, Long orgId, Long programId, String actionPerformed,
			String entityType, Long entityId, String entityName) {
		try {
			OrganizationHistory orgHistory = new OrganizationHistory();
			Date date = CommonUtils.getFormattedDate();
			orgHistory.setOrganizationId(orgId);
			orgHistory.setProgramId(programId);
			orgHistory.setEntityId(entityId);
			orgHistory.setEntityName(entityName);
			orgHistory.setEntityType(entityType);

			orgHistory.setUpdatedAt(date);

			orgHistory.setUpdatedBy(user.getUserDisplayName());
			orgHistory.setActionPerformed(actionPerformed);
			orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
		} catch (Exception e) {
			LOGGER.error("exception occured while creating history", e);
		}

	}

}
