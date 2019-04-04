/**
 * 
 */
package com.winwin.winwin.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.payload.OrganizationNotePayload;
import com.winwin.winwin.repository.OrganizationNoteRepository;

/**
 * @author ArvindKhatik
 *
 */
public class OrganizationNoteService implements IOrganizationNoteService {
	@Autowired
	OrganizationNoteRepository organizationNoteRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationNoteService.class);

	@Override
	public OrganizationNote createOrganizationNote(OrganizationNotePayload organizationNotePayload) {
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
				note.setCreatedBy(OrganizationConstants.CREATED_BY);
				note.setUpdatedBy(OrganizationConstants.UPDATED_BY);
				note = organizationNoteRepository.saveAndFlush(note);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.note.exception.created"), e);
		}

		return note;
	}

	@Override
	public void removeOrganizationNote(Long noteId) {
		organizationNoteRepository.deleteById(noteId);
	}

	@Override
	public List<OrganizationNote> getOrganizationNoteList(Long orgId) {
		return organizationNoteRepository.findAllOrgNotesList(orgId);
	}

}
