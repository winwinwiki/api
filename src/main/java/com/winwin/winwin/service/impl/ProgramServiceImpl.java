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
import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.payload.ProgramRequestPayload;
import com.winwin.winwin.payload.ProgramResponsePayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.NaicsDataRepository;
import com.winwin.winwin.repository.NteeDataRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.service.AddressService;
import com.winwin.winwin.service.ProgramService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class ProgramServiceImpl implements ProgramService {
	@Autowired
	ProgramRepository programRepository;

	@Autowired
	NaicsDataRepository naicsRepository;

	@Autowired
	NteeDataRepository nteeRepository;

	@Autowired
	AddressService addressService;

	@Autowired
	OrganizationRepository organizationRepository;

	@Autowired
	UserService userService;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramServiceImpl.class);

	@Override
	@Transactional
	public Program createProgram(ProgramRequestPayload programPayload) {
		Program program = getProgramFromProgramRequestPayload(programPayload);
		return programRepository.saveAndFlush(program);
	}

	@Override
	@Transactional
	public void deleteProgram(Long id) {
		programRepository.deleteById(id);
	}

	@Override
	@Transactional
	public Program updateProgram(ProgramRequestPayload programPayload) {
		Program program = getProgramFromProgramRequestPayload(programPayload);
		return programRepository.saveAndFlush(program);
	}

	@Override
	public List<Program> getProgramList(Long orgId) {
		return programRepository.findAllProgramList(orgId);
	}

	@Override
	public ProgramResponsePayload getProgramResponseFromProgram(Program payload) {
		ProgramResponsePayload responsePayload = new ProgramResponsePayload();
		BeanUtils.copyProperties(payload, responsePayload);
		responsePayload.setAddress(addressService.getAddressPayloadFromAddress(payload.getAddress()));
		if (payload.getOrganization() != null)
			responsePayload.setOrganizationId(payload.getOrganization().getId());
		return responsePayload;
	}

	@Override
	public Program getProgramFromProgramRequestPayload(ProgramRequestPayload payload) {
		Program program = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			Date date = CommonUtils.getFormattedDate();
			if (payload.getId() != null)
				program = programRepository.findProgramById(payload.getId());
			if (program == null)
				program = new Program();
			Address address = addressService.saveAddress(payload.getAddress());
			program.setAddress(address);

			BeanUtils.copyProperties(payload, program);

			if (payload.getNaicsCode() != null)
				program.setNaicsCode(naicsRepository.findById(payload.getNaicsCode()).orElse(null));

			if (payload.getNteeCode() != null)
				program.setNteeCode(nteeRepository.findById(payload.getNteeCode()).orElse(null));

			if (payload.getOrganizationId() != null)
				program.setOrganization(organizationRepository.findOrgById(payload.getOrganizationId()));

			program.setCreatedAt(date);
			program.setUpdatedAt(date);
			program.setCreatedBy(user.getEmail());
			program.setUpdatedBy(user.getEmail());
			return program;
		} catch (Exception e) {
			return null;
		}
	}

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
