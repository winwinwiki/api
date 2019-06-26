package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.payload.OrganizationNotePayload;

/**
 * @author ArvindKhatik
 *
 */
public interface OrganizationNoteService {
	OrganizationNote createOrganizationNote(OrganizationNotePayload organizationNotePayload);

	public List<OrganizationNote> createOrganizationsNotes(List<OrganizationNote> organizationNoteList);

	OrganizationNote updateOrganizationNote(OrganizationNotePayload organizationNotePayload);

	void removeOrganizationNote(Long noteId, Long orgId);

	List<OrganizationNote> getOrganizationNoteList(Long id);

}
