/**
 * 
 */
package com.winwin.winwin.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrganizationHistory;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.payload.OrganizationNotePayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrgHistoryRepository;
import com.winwin.winwin.repository.OrganizationNoteRepository;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class OrganizationNoteService implements IOrganizationNoteService {
	@Autowired
	OrganizationNoteRepository organizationNoteRepository;

	@Autowired
	OrgHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationNoteService.class);

	@Override
	public OrganizationNote createOrganizationNote(OrganizationNotePayload organizationNotePayload) {
		UserPayload user = getUserDetails();
		OrganizationNote note = null;
		try {
			if (organizationNotePayload != null) {
				note = new OrganizationNote();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				note.setName(organizationNotePayload.getNote());
				note.setOrganizationId(organizationNotePayload.getOrganizationId());
				note.setCreatedAt(sdf.parse(formattedDte));
				note.setUpdatedAt(sdf.parse(formattedDte));
				note.setCreatedBy(user.getEmail());
				note.setUpdatedBy(user.getEmail());
				note = organizationNoteRepository.saveAndFlush(note);

				if (null != note && null != note.getOrganizationId()) {
					createOrgHistory(user, note.getOrganizationId(), sdf, formattedDte, OrganizationConstants.CREATE,
							OrganizationConstants.NOTE, note.getId(), note.getName());
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.note.exception.created"), e);
		}

		return note;
	}

	@Override
	public void removeOrganizationNote(Long noteId, Long orgId) {
		UserPayload user = getUserDetails();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		if (null != orgId && null != user) {
			try {
				OrganizationNote note = organizationNoteRepository.getOne(noteId);
				if (null != note) {

					organizationNoteRepository.deleteById(noteId);

					createOrgHistory(user, orgId, sdf, formattedDte, OrganizationConstants.DELETE,
							OrganizationConstants.NOTE, note.getId(), note.getName());

				}
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.note.error.deleted"), e);
			}

		}

	}

	@Override
	public List<OrganizationNote> getOrganizationNoteList(Long orgId) {
		return organizationNoteRepository.findAllOrgNotesList(orgId);
	}

	/**
	 * @param user
	 * @return
	 */
	private UserPayload getUserDetails() {
		UserPayload user = null;
		if (null != SecurityContextHolder.getContext() && null != SecurityContextHolder.getContext().getAuthentication()
				&& null != SecurityContextHolder.getContext().getAuthentication().getDetails()) {
			user = (UserPayload) SecurityContextHolder.getContext().getAuthentication().getDetails();

		}
		return user;
	}

	/**
	 * @param user
	 * @param orgId
	 * @param sdf
	 * @param formattedDte
	 * @param actionPerformed
	 * @param entityType
	 * @param entityId
	 * @param entityName
	 * @throws ParseException
	 */
	private void createOrgHistory(UserPayload user, Long orgId, SimpleDateFormat sdf, String formattedDte,
			String actionPerformed, String entityType, Long entityId, String entityName) throws ParseException {
		OrganizationHistory orgHistory = new OrganizationHistory();
		orgHistory.setOrganizationId(orgId);
		orgHistory.setEntityId(entityId);
		orgHistory.setEntityName(entityName);
		orgHistory.setEntityType(entityType);
		orgHistory.setUpdatedAt(sdf.parse(formattedDte));
		orgHistory.setUpdatedBy(user.getUserDisplayName());
		orgHistory.setActionPerformed(actionPerformed);
		orgHistory = orgHistoryRepository.saveAndFlush(orgHistory);
	}

}
