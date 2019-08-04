package com.winwin.winwin.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.payload.ProgramRequestPayload;
import com.winwin.winwin.payload.ProgramResponsePayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.ProgramService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class ProgramServiceImpl implements ProgramService {
	@Autowired
	private ProgramRepository programRepository;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationHistoryService orgHistoryService;
	@Autowired
	private CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramServiceImpl.class);

	/**
	 * create new Program for Organization
	 * 
	 * @param programPayload
	 * @param exceptionResponse
	 */
	@Override
	@Transactional
	public Program createProgram(ProgramRequestPayload programPayload, ExceptionResponse exceptionResponse) {
		Program program = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			program = getProgramFromProgramRequestPayload(programPayload, user);
			program = programRepository.saveAndFlush(program);

			if (null != program.getId() && null != program.getOrganization()) {
				orgHistoryService.createOrganizationHistory(user, program.getOrganization().getId(),
						OrganizationConstants.CREATE, OrganizationConstants.PROGRAM, program.getId(), program.getName(),
						"");
			}
		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			exceptionResponse.setException(e);
			LOGGER.error(customMessageSource.getMessage("prg.error.created"), e);
		}
		return program;
	}

	/**
	 * delete Program by Id
	 * 
	 * @param program
	 * @param exceptionResponse
	 */
	@Override
	@Transactional
	public void deleteProgram(Program program, ExceptionResponse exceptionResponse) {
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (program.getOrganization() != null) {
				Long orgId = program.getOrganization().getId();
				program.setIsActive(false);
				programRepository.saveAndFlush(program);
				orgHistoryService.createOrganizationHistory(user, orgId, OrganizationConstants.DELETE,
						OrganizationConstants.PROGRAM, program.getId(), program.getName(), "");
			}

		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			exceptionResponse.setException(e);
			LOGGER.error(customMessageSource.getMessage("prg.error.deleted"), e);
		}
	}

	/**
	 * update Program by Id
	 * 
	 * @param programPayload
	 * @param exceptionResponse
	 */
	@Override
	@Transactional
	public Program updateProgram(ProgramRequestPayload programPayload, ExceptionResponse exceptionResponse) {
		Program program = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			program = getProgramFromProgramRequestPayload(programPayload, user);
			program = programRepository.saveAndFlush(program);

			if (null != program.getId() && null != program.getOrganization()) {
				orgHistoryService.createOrganizationHistory(user, program.getOrganization().getId(),
						OrganizationConstants.UPDATE, OrganizationConstants.PROGRAM, program.getId(), program.getName(),
						"");
			}
		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			exceptionResponse.setException(e);
			LOGGER.error(customMessageSource.getMessage("prg.error.updated"), e);
		}
		return program;
	}

	/**
	 * returns Program List by orgId
	 * 
	 * @param orgId
	 * @return
	 */
	@Override
	public List<Program> getProgramList(Long orgId) {
		return programRepository.findAllProgramList(orgId);
	}

	@Override
	public ProgramResponsePayload getProgramResponseFromProgram(Program payload) {
		ProgramResponsePayload responsePayload = new ProgramResponsePayload();
		BeanUtils.copyProperties(payload, responsePayload);
		if (payload.getOrganization() != null)
			responsePayload.setOrganizationId(payload.getOrganization().getId());
		return responsePayload;
	}

	/**
	 * return Program from ProgramRequestPayload
	 * 
	 * @param payload
	 * @param user
	 * @return
	 */
	@Override
	public Program getProgramFromProgramRequestPayload(ProgramRequestPayload payload, UserPayload user) {
		Program program = null;
		try {
			Date date = CommonUtils.getFormattedDate();
			if (payload.getId() != null)
				program = programRepository.findProgramById(payload.getId());
			if (program == null)
				program = new Program();

			BeanUtils.copyProperties(payload, program);

			if (payload.getOrganizationId() != null)
				program.setOrganization(organizationRepository.findOrgById(payload.getOrganizationId()));

			program.setIsActive(true);
			program.setCreatedAt(date);
			program.setUpdatedAt(date);
			program.setCreatedBy(user.getUserDisplayName());
			program.setUpdatedBy(user.getUserDisplayName());
			program.setCreatedByEmail(user.getEmail());
			program.setUpdatedByEmail(user.getEmail());
			return program;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * returns OrganizationFilterPayload based Program List by orgId
	 * 
	 * @param payload
	 * @param orgId
	 * @param response
	 * @return
	 */
	@Override
	public List<Program> getProgramList(OrganizationFilterPayload payload, Long orgId, ExceptionResponse response) {
		List<Program> programList = new ArrayList<Program>();
		try {
			if (payload.getNameSearch() != null)
				return programRepository.findProgramByNameIgnoreCaseContaining(orgId, payload.getNameSearch());

			else
				return programRepository.filterProgram(payload, OrganizationConstants.PROGRAM, orgId);

		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("prg.error.list"), e);
		}
		return programList;
	}
}
