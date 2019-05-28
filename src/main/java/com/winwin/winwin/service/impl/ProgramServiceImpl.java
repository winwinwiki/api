package com.winwin.winwin.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Address;
import com.winwin.winwin.entity.Organization;
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
	public Program createProgram(ProgramRequestPayload programPayload) {
		// TODO Auto-generated method stub

		Program program = getProgramFromProgramRequestPayload(programPayload);

		return programRepository.saveAndFlush(program);

	}

	@Override
	public void deleteProgram(Long id) {
		programRepository.deleteById(id);
	}

	@Override
	public Program updateProgram(ProgramRequestPayload programPayload) {
		// TODO Auto-generated method stub
		Program program = getProgramFromProgramRequestPayload(programPayload);

		return programRepository.saveAndFlush(program);
	}

	@Override
	public List<Program> getProgramList(Long orgId) {
		// TODO Auto-generated method stub
		return programRepository.findAllProgramList(orgId);
	}

	@Override
	public ProgramResponsePayload getProgramResponseFromProgram(Program payload) {
		ProgramResponsePayload responsePayload = new ProgramResponsePayload();
		responsePayload.setId(payload.getId());

		responsePayload.setAddress(addressService.getAddressPayloadFromAddress(payload.getAddress()));

		responsePayload.setAssets(payload.getAssets());
		responsePayload.setBusinessModel(payload.getBusinessModel());
		responsePayload.setContactInfo(payload.getContactInfo());
		responsePayload.setDescription(payload.getDescription());
		responsePayload.setFacebookUrl(payload.getFacebookUrl());
		responsePayload.setIsActive(payload.getIsActive());
		responsePayload.setLinkedinUrl(payload.getLinkedinUrl());
		responsePayload.setMissionStatement(payload.getMissionStatement());
		if (payload.getOrganization() != null)
			responsePayload.setOrganizationId(payload.getOrganization().getId());
		responsePayload.setPopulationServed(payload.getPopulationServed());
		responsePayload.setPriority(payload.getPriority());
		responsePayload.setPurpose(payload.getPurpose());
		responsePayload.setRevenue(payload.getRevenue());
		responsePayload.setSector(payload.getSector());
		responsePayload.setSectorLevel(payload.getSectorLevel());
		responsePayload.setSectorLevelName(payload.getSectorLevelName());
		responsePayload.setSelfInterest(payload.getSelfInterest());
		responsePayload.setTagStatus(payload.getTagStatus());
		responsePayload.setTwitterUrl(payload.getTwitterUrl());
		responsePayload.setValues(payload.getValues());
		responsePayload.setWebsiteUrl(payload.getWebsiteUrl());
		responsePayload.setCreatedAt(payload.getCreatedAt());
		responsePayload.setCreatedBy(payload.getCreatedBy());
		responsePayload.setUpdatedAt(payload.getUpdatedAt());
		responsePayload.setUpdatedBy(payload.getUpdatedBy());
		responsePayload.setAdminUrl(payload.getAdminUrl());
		responsePayload.setName(payload.getName());

		responsePayload.setNaicsCode(payload.getNaicsCode());

		responsePayload.setNteeCode(payload.getNteeCode());

		return responsePayload;
	}

	@Override
	public Program getProgramFromProgramRequestPayload(ProgramRequestPayload payload) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

			UserPayload user = userService.getCurrentUserDetails();
			Program program = null;
			if (payload.getId() != null)
				program = programRepository.findProgramById(payload.getId());
			if (program == null)
				program = new Program();
			Address address = addressService.saveAddress(payload.getAddress());
			program.setAddress(address);
			program.setAssets(payload.getAssets());
			program.setBusinessModel(payload.getBusinessModel());
			program.setContactInfo(payload.getContactInfo());
			program.setDescription(payload.getDescription());
			program.setFacebookUrl(payload.getFacebookUrl());
			program.setIsActive(payload.getIsActive());
			program.setLinkedinUrl(payload.getLinkedinUrl());
			program.setMissionStatement(payload.getMissionStatement());

			if (payload.getNaicsCode() != null)
				program.setNaicsCode(naicsRepository.findById(payload.getNaicsCode()).orElse(null));
			program.setName(payload.getName());
			if (payload.getNteeCode() != null)
				program.setNteeCode(nteeRepository.findById(payload.getNteeCode()).orElse(null));
			if (payload.getOrganizationId() != null)
				program.setOrganization(organizationRepository.findOrgById(payload.getOrganizationId()));
			program.setPopulationServed(payload.getPopulationServed());
			program.setPriority(payload.getPriority());
			program.setPurpose(payload.getPurpose());
			program.setRevenue(payload.getRevenue());
			program.setSector(payload.getSector());
			program.setSectorLevel(payload.getSectorLevel());
			program.setSectorLevelName(payload.getSectorLevelName());
			program.setSelfInterest(payload.getSelfInterest());
			program.setTagStatus(program.getTagStatus());
			program.setTwitterUrl(program.getTwitterUrl());
			program.setValues(payload.getValues());
			program.setWebsiteUrl(payload.getWebsiteUrl());

			program.setCreatedAt(sdf.parse(formattedDte));
			program.setAdminUrl(payload.getAdminUrl());
			program.setUpdatedAt(sdf.parse(formattedDte));
			program.setCreatedBy(user.getEmail());
			program.setUpdatedBy(user.getEmail());
			return program;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	@Override
	public List<Program> getProgramList(OrganizationFilterPayload payload, Long orgId, ExceptionResponse response) {
		try {
			if (payload.getNameSearch() != null)
				return programRepository.findProgramByNameIgnoreCaseContaining(payload.getNameSearch(), orgId);
			/*
			 * else return organizationRepository.filterOrganization(payload,
			 * OrganizationConstants.PROGRAM, orgId);
			 */
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(customMessageSource.getMessage("prg.error.list"), e);
		}
		return null;
	}
}
