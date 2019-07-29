/**
 * 
 */
package com.winwin.winwin.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.payload.OrganizationNotePayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationNoteRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.OrganizationNoteService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class OrganizationNoteServiceImpl implements OrganizationNoteService {
	@Autowired
	private OrganizationNoteRepository organizationNoteRepository;
	@Autowired
	private CustomMessageSource customMessageSource;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationHistoryService orgHistoryService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationNoteServiceImpl.class);

	/**
	 * create OrganizationNote
	 * 
	 * @param organizationNotePayload
	 */
	@Override
	@Transactional
	public OrganizationNote createOrganizationNote(OrganizationNotePayload organizationNotePayload) {
		OrganizationNote note = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (organizationNotePayload != null) {
				note = new OrganizationNote();
				Date date = CommonUtils.getFormattedDate();
				BeanUtils.copyProperties(organizationNotePayload, note);
				note.setCreatedAt(date);
				note.setUpdatedAt(date);
				note.setCreatedBy(user.getUserDisplayName());
				note.setUpdatedBy(user.getUserDisplayName());
				note.setCreatedByEmail(user.getEmail());
				note.setUpdatedByEmail(user.getEmail());
				note = organizationNoteRepository.saveAndFlush(note);

				if (null != note && null != note.getOrganization()) {
					orgHistoryService.createOrganizationHistory(user, note.getOrganization().getId(),
							OrganizationConstants.CREATE, OrganizationConstants.NOTE, note.getId(), note.getName(), "");
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.note.exception.created"), e);
		}
		return note;
	}

	/**
	 * create OrganizationNote from List
	 * 
	 * @param organizationNoteList
	 */
	@Override
	@Transactional
	public List<OrganizationNote> createOrganizationsNotes(List<OrganizationNote> organizationNoteList) {
		try {
			if (!organizationNoteList.isEmpty()) {
				organizationNoteList = organizationNoteRepository.saveAll(organizationNoteList);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.note.exception.created"), e);
		}
		return organizationNoteList;
	}

	/**
	 * update OrganizationNote
	 * 
	 * @param organizationNotePayload
	 */
	@Override
	@Transactional
	public OrganizationNote updateOrganizationNote(OrganizationNotePayload organizationNotePayload) {
		OrganizationNote note = null;
		try {
			if (null != organizationNotePayload.getId()) {
				note = organizationNoteRepository.findOrgNoteById(organizationNotePayload.getId());
				if (null != note) {
					UserPayload user = userService.getCurrentUserDetails();
					BeanUtils.copyProperties(organizationNotePayload, note);
					Date date = CommonUtils.getFormattedDate();
					note.setUpdatedAt(date);
					note.setUpdatedBy(user.getUserDisplayName());
					note.setUpdatedByEmail(user.getEmail());
					note = organizationNoteRepository.saveAndFlush(note);

					if (null != note && null != note.getOrganization()) {
						orgHistoryService.createOrganizationHistory(user, note.getOrganization().getId(),
								OrganizationConstants.UPDATE, OrganizationConstants.NOTE, note.getId(), note.getName(),
								"");
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.note.exception.updated"), e);
		}
		return note;
	}

	/**
	 * delete OrganizationNote
	 * 
	 * @param noteId
	 * @param orgId
	 */
	@Override
	@Transactional
	public void removeOrganizationNote(Long noteId, Long orgId) {
		UserPayload user = userService.getCurrentUserDetails();
		if (null != orgId && null != user) {
			try {
				OrganizationNote note = organizationNoteRepository.getOne(noteId);
				if (null != note) {
					organizationNoteRepository.deleteById(noteId);
					orgHistoryService.createOrganizationHistory(user, orgId, OrganizationConstants.DELETE,
							OrganizationConstants.NOTE, note.getId(), note.getName(), "");
				}
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.note.error.deleted"), e);
			}
		}
	}

	/**
	 * returns OrganizationNote List by orgId
	 * 
	 * @param orgId
	 */
	@Override
	public List<OrganizationNote> getOrganizationNoteList(Long orgId) {
		return organizationNoteRepository.findAllOrgNotesList(orgId);
	}

}
