/**
 * 
 */
package com.winwin.winwin.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.payload.OrganizationNotePayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.OrganizationNoteRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.UserService;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class OrganizationNoteServiceImpl implements OrganizationNoteService {
	@Autowired
	OrganizationNoteRepository organizationNoteRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService orgHistoryService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationNoteServiceImpl.class);

	@Override
	public OrganizationNote createOrganizationNote(OrganizationNotePayload organizationNotePayload) {
		UserPayload user = userService.getCurrentUserDetails();
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
					orgHistoryService.createOrganizationHistory(user, note.getOrganizationId(), sdf, formattedDte,
							OrganizationConstants.CREATE, OrganizationConstants.NOTE, note.getId(), note.getName());
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.note.exception.created"), e);
		}

		return note;
	}

	@Override
	public void removeOrganizationNote(Long noteId, Long orgId) {
		UserPayload user = userService.getCurrentUserDetails();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		if (null != orgId && null != user) {
			try {
				OrganizationNote note = organizationNoteRepository.getOne(noteId);
				if (null != note) {

					organizationNoteRepository.deleteById(noteId);

					orgHistoryService.createOrganizationHistory(user, orgId, sdf, formattedDte,
							OrganizationConstants.DELETE, OrganizationConstants.NOTE, note.getId(), note.getName());

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

}
