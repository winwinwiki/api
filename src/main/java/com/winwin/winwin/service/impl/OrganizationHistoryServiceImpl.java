package com.winwin.winwin.service.impl;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class OrganizationHistoryServiceImpl implements OrganizationHistoryService {

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private ProgramRepository programRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationHistoryServiceImpl.class);

	/**
	 * create OrganizationHistory by orgId
	 * 
	 * @param user
	 * @param orgId
	 * @param actionPerformed
	 * @param entityType
	 * @param entityId
	 * @param entityName
	 * @param entityCode
	 */
	@Override
	@Transactional
	public void createOrganizationHistory(UserPayload user, Long orgId, String actionPerformed, String entityType,
			Long entityId, String entityName, String entityCode) {
		try {
			OrganizationHistory orgHistory = new OrganizationHistory();
			Date date = CommonUtils.getFormattedDate();
			Organization organization = organizationRepository.findOrgById(orgId);
			if (null != organization) {
				organization.setUpdatedAt(date);
				organization.setUpdatedBy(user.getUserDisplayName());
				organization.setUpdatedByEmail(user.getEmail());
			}
			orgHistory.setOrganizationId(orgId);
			orgHistory.setEntityId(entityId);
			orgHistory.setEntityName(entityName);
			orgHistory.setEntityType(entityType);
			orgHistory.setEntityCode(entityCode);
			orgHistory.setUpdatedAt(date);
			orgHistory.setUpdatedBy(user.getUserDisplayName());
			orgHistory.setUpdatedByEmail(user.getEmail());
			orgHistory.setActionPerformed(actionPerformed);
			orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
		} catch (Exception e) {
			LOGGER.error("exception occured while creating history", e);
		}

	}

	/**
	 * create OrganizationHistory by orgId and programId
	 * 
	 * @param user
	 * @param orgId
	 * @param programId
	 * @param actionPerformed
	 * @param entityType
	 * @param entityId
	 * @param entityName
	 * @param entityCode
	 */
	@Override
	@Transactional
	public void createOrganizationHistory(UserPayload user, Long orgId, Long programId, String actionPerformed,
			String entityType, Long entityId, String entityName, String entityCode) {
		try {
			OrganizationHistory orgHistory = new OrganizationHistory();
			Date date = CommonUtils.getFormattedDate();
			if (null != orgId) {
				Organization organization = organizationRepository.findOrgById(orgId);
				if (null != organization) {
					organization.setUpdatedAt(date);
					organization.setUpdatedBy(user.getUserDisplayName());
					organization.setUpdatedByEmail(user.getEmail());
				}
			}
			if (null != programId) {
				Program program = programRepository.findProgramById(programId);
				if (null != program) {
					program.setUpdatedAt(date);
					program.setUpdatedBy(user.getUserDisplayName());
					program.setUpdatedByEmail(user.getEmail());
				}
			}
			orgHistory.setOrganizationId(orgId);
			orgHistory.setProgramId(programId);
			orgHistory.setEntityId(entityId);
			orgHistory.setEntityName(entityName);
			orgHistory.setEntityType(entityType);
			orgHistory.setEntityCode(entityCode);
			orgHistory.setUpdatedAt(date);
			orgHistory.setUpdatedBy(user.getUserDisplayName());
			orgHistory.setUpdatedByEmail(user.getEmail());
			orgHistory.setActionPerformed(actionPerformed);
			orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
		} catch (Exception e) {
			LOGGER.error("exception occured while creating history", e);
		}

	}

	/**
	 * create OrganizationHistory from List
	 * 
	 * @param orgHistoryList
	 */
	@Override
	@Transactional
	public void createOrganizationHistory(List<OrganizationHistory> orgHistoryList) {
		try {
			if (orgHistoryList != null)
				orgHistoryRepository.saveAll(orgHistoryList);
		} catch (Exception e) {
			LOGGER.error("exception occured while creating history", e);
		}
	}

}
