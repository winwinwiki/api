/**
 * The class ProgramController is mapped to all incoming request comes at '/program' for creating new programs and it's associated entities.
 * i.e. SPI tags mapping,SDG tags mapping, address, Resources, DataSets.
 * 
 */
package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.ProgramDataSet;
import com.winwin.winwin.entity.ProgramRegionServed;
import com.winwin.winwin.entity.ProgramResource;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.payload.DataSetCategoryPayload;
import com.winwin.winwin.payload.ProgramDataSetPayLoad;
import com.winwin.winwin.payload.ProgramRegionServedPayload;
import com.winwin.winwin.payload.ProgramResourcePayLoad;
import com.winwin.winwin.payload.ProgramResponsePayload;
import com.winwin.winwin.payload.ProgramSdgDataMapPayload;
import com.winwin.winwin.payload.ProgramSpiDataMapPayload;
import com.winwin.winwin.payload.RegionMasterFilterPayload;
import com.winwin.winwin.payload.RegionMasterPayload;
import com.winwin.winwin.payload.ResourceCategoryPayLoad;
import com.winwin.winwin.payload.SdgGoalPayload;
import com.winwin.winwin.payload.SpiDataDimensionsPayload;
import com.winwin.winwin.repository.ProgramDataSetRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.repository.ProgramResourceRepository;
import com.winwin.winwin.service.ProgramDataSetService;
import com.winwin.winwin.service.ProgramRegionServedService;
import com.winwin.winwin.service.ProgramResourceService;
import com.winwin.winwin.service.ProgramSdgDataService;
import com.winwin.winwin.service.ProgramService;
import com.winwin.winwin.service.ProgramSpiDataService;
import com.winwin.winwin.service.SdgDataService;
import com.winwin.winwin.service.SpiDataService;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@RestController
@RequestMapping(value = "/program")

public class ProgramController extends BaseController {

	@Autowired
	private ProgramRegionServedService programRegionServedService;
	@Autowired
	private SpiDataService spiDataService;
	@Autowired
	private SdgDataService sdgDataService;
	@Autowired
	private ProgramSdgDataService programSdgDataService;
	@Autowired
	private ProgramSpiDataService programSpiDataService;
	@Autowired
	private ProgramService programService;
	@Autowired
	private ProgramRepository programRepository;
	@Autowired
	private ProgramDataSetRepository programDataSetRepository;
	@Autowired
	private ProgramDataSetService programDataSetService;
	@Autowired
	private ProgramResourceService programResourceService;
	@Autowired
	private ProgramResourceRepository programResourceRepository;

	// Code for program data set start
	/**
	 * Returns an Program Details by Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(path = "/{id}")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getProgramDetails(@PathVariable("id") Long id) {
		Program program = null;
		ProgramResponsePayload payload = null;
		try {
			program = programRepository.findProgramById(id);
			if (program == null) {
				throw new OrganizationException(customMessageSource.getMessage("prg.error.not_found"));
			} else {
				payload = programService.getProgramResponseFromProgram(program);
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prg.error.fetch");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Creates DataSet for an Program by Id
	 * 
	 * @param programDataSetPayLoad
	 * @return @
	 */
	@PostMapping(path = "/{id}/dataset")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createProgrmDataSet(@Valid @RequestBody ProgramDataSetPayLoad programDataSetPayLoad) {
		ProgramDataSet programDataSet = null;
		ProgramDataSetPayLoad payload = new ProgramDataSetPayLoad();
		if (null != programDataSetPayLoad) {
			try {
				programDataSet = programDataSetService.createOrUpdateProgramDataSet(programDataSetPayLoad);
				if (null != programDataSet) {
					payload.setId(programDataSet.getId());
					DataSetCategory category = programDataSet.getDataSetCategory();
					DataSetCategoryPayload payloadCategory = null;
					if (null != category) {
						payloadCategory = new DataSetCategoryPayload();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
					}
					payload.setDataSetCategory(payloadCategory);

					if (null != programDataSet.getProgram())
						payload.setProgramId(programDataSet.getProgram().getId());

					payload.setDescription(programDataSet.getDescription());
					payload.setType(programDataSet.getType());
					payload.setUrl(programDataSet.getUrl());
					payload.setIsActive(programDataSet.getIsActive());
				}
			} catch (Exception e) {
				return sendExceptionResponse(e, "prog.dataset.error.created");
			}
		} else {
			return sendErrorResponse("org.bad.request");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Update DataSet for an Program by Id
	 * 
	 * @param programDataSetPayLoad
	 * @return @
	 */
	@PutMapping(path = "/{id}/dataset")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateProgramDataSet(@Valid @RequestBody ProgramDataSetPayLoad programDataSetPayLoad) {
		ProgramDataSetPayLoad payload = new ProgramDataSetPayLoad();

		if (null != programDataSetPayLoad && null != programDataSetPayLoad.getId()) {
			Long id = programDataSetPayLoad.getId();
			ProgramDataSet dataSet = programDataSetRepository.findProgramDataSetById(id);
			if (dataSet == null) {
				return sendErrorResponse("prog.dataset.error.not_found");
			} else {
				dataSet = programDataSetService.createOrUpdateProgramDataSet(programDataSetPayLoad);
				BeanUtils.copyProperties(dataSet, payload);
				DataSetCategory category = dataSet.getDataSetCategory();
				DataSetCategoryPayload payloadCategory = null;
				if (null != category) {
					payloadCategory = new DataSetCategoryPayload();
					BeanUtils.copyProperties(category, payloadCategory);
				}

				if (null != dataSet.getProgram())
					payload.setProgramId(dataSet.getProgram().getId());

				payload.setDataSetCategory(payloadCategory);
			}
		} else {
			return sendErrorResponse("org.bad.request");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Delete a DataSet for an Program by Id
	 * 
	 * @param programDataSetPayLoad
	 * @return @
	 */
	@DeleteMapping(path = "/{id}/dataset")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> deleteProgramDataSet(@RequestBody ProgramDataSetPayLoad programDataSetPayLoad) {
		try {
			if (null != programDataSetPayLoad && null != programDataSetPayLoad.getId()) {
				Long id = programDataSetPayLoad.getId();
				ProgramDataSet dataSet = programDataSetRepository.findProgramDataSetById(id);
				if (dataSet == null) {
					return sendErrorResponse("prog.dataset.error.not_found");
				}
				programDataSetService.removeProgramDataSet(id, programDataSetPayLoad.getOrganizationId(),
						programDataSetPayLoad.getProgramId());
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.dataset.error.deleted");
		}
		return sendSuccessResponse("prog.dataset.success.deleted");
	}

	/**
	 * Returns a DataSet List associated with Program by Id
	 * 
	 * @param id
	 * @return @
	 */
	@GetMapping(path = "{id}/datasets")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getProgramDataSetList(@PathVariable("id") Long id) {
		List<ProgramDataSet> programDataSetList = null;
		ProgramDataSetPayLoad payload = null;
		List<ProgramDataSetPayLoad> payloadList = new ArrayList<>();
		try {
			programDataSetList = programDataSetService.getProgramDataSetList(id);
			if (programDataSetList != null) {
				for (ProgramDataSet dataSet : programDataSetList) {
					payload = new ProgramDataSetPayLoad();
					BeanUtils.copyProperties(dataSet, payload);
					DataSetCategory category = dataSet.getDataSetCategory();
					DataSetCategoryPayload payloadCategory = null;
					if (null != category) {
						payloadCategory = new DataSetCategoryPayload();
						BeanUtils.copyProperties(category, payloadCategory);
					}
					payload.setDataSetCategory(payloadCategory);

					if (null != dataSet.getProgram())
						payload.setProgramId(dataSet.getProgram().getId());

					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.dataset.error.list");
		}
		return sendSuccessResponse(payloadList);

	}

	/**
	 * Returns a DataSet Category Master List
	 * 
	 * @return @
	 */
	@GetMapping(path = "/{id}/dataset/categorylist")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getDataSetCategoryList(HttpServletResponse httpServletResponce) {
		List<DataSetCategory> dataSetCategoryList = null;
		List<DataSetCategoryPayload> payloadList = new ArrayList<>();
		try {
			dataSetCategoryList = programDataSetService.getDataSetCategoryList();
			if (dataSetCategoryList != null) {
				for (DataSetCategory category : dataSetCategoryList) {
					DataSetCategoryPayload payload = new DataSetCategoryPayload();
					BeanUtils.copyProperties(category, payload);
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.dataset.category.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	// Code for program data set end

	// Code for program resource start

	/**
	 * Creates Resources for an Program by Id
	 * 
	 * @param programResourcePayLoad
	 * @return @
	 */
	@PostMapping(path = "/{id}/resource")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createProgramResource(@Valid @RequestBody ProgramResourcePayLoad programResourcePayLoad) {
		ProgramResource programResource = null;
		ProgramResourcePayLoad payload = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		if (null != programResourcePayLoad) {
			try {
				programResource = programResourceService.createOrUpdateProgramResource(programResourcePayLoad);
				if (null != programResource) {
					payload = new ProgramResourcePayLoad();
					BeanUtils.copyProperties(programResource, payload);

					category = programResource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						BeanUtils.copyProperties(category, payloadCategory);
					}
					payload.setResourceCategory(payloadCategory);

					if (null != programResource.getProgram())
						payload.setProgramId(programResource.getProgram().getId());
				}
			} catch (Exception e) {
				return sendExceptionResponse(e, "prog.resource.error.created");
			}
		} else {
			return sendErrorResponse("org.bad.request");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Update Resource for an Program by Id
	 * 
	 * @param programResourcePayLoad
	 * @return @
	 */
	@PutMapping(path = "/{id}/resource")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateProgramResource(@Valid @RequestBody ProgramResourcePayLoad programResourcePayLoad) {
		ProgramResource programResource = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		ProgramResourcePayLoad payload = new ProgramResourcePayLoad();
		try {
			if (null != programResourcePayLoad && null != programResourcePayLoad.getId()) {
				Long id = programResourcePayLoad.getId();
				programResource = programResourceRepository.findProgramResourceById(id);
				if (programResource == null) {
					return sendErrorResponse("prog.resource.error.not_found");
				} else {
					programResource = programResourceService.createOrUpdateProgramResource(programResourcePayLoad);
					BeanUtils.copyProperties(programResource, payload);

					category = programResource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						BeanUtils.copyProperties(category, payloadCategory);
					}
					payload.setResourceCategory(payloadCategory);

					if (null != programResource.getProgram())
						payload.setProgramId(programResource.getProgram().getId());

				}
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.resource.error.updated");
		}
		return sendSuccessResponse(payload);
	}

	/**
	 * Delete Resource associated with Program by Id
	 * 
	 * @param programResourcePayLoad
	 * @return @
	 */
	@DeleteMapping(path = "/{id}/resource")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> deleteProgramResource(@Valid @RequestBody ProgramResourcePayLoad programResourcePayLoad) {
		try {
			if (null != programResourcePayLoad && null != programResourcePayLoad.getId()) {
				Long id = programResourcePayLoad.getId();
				ProgramResource programResource = programResourceRepository.findProgramResourceById(id);
				if (programResource == null) {
					return sendErrorResponse("prog.resource.error.not_found");
				}
				programResourceService.removeProgramResource(id, programResourcePayLoad.getOrganizationId(),
						programResourcePayLoad.getProgramId());
			} else {
				return sendErrorResponse("org.bad.request");
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.resource.error.deleted");
		}
		return sendSuccessResponse("prog.resource.success.deleted");
	}

	/**
	 * Returns a List of Resources for an Program by Id
	 * 
	 * @param id
	 * @return @
	 */
	@GetMapping(path = "/{id}/resources")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getProgramResourceList(@PathVariable("id") Long id) {
		List<ProgramResource> programResourceList = null;
		ProgramResourcePayLoad payload = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		List<ProgramResourcePayLoad> payloadList = new ArrayList<>();
		try {
			programResourceList = programResourceService.getProgramResourceList(id);
			if (programResourceList != null) {
				for (ProgramResource resource : programResourceList) {
					payload = new ProgramResourcePayLoad();
					BeanUtils.copyProperties(resource, payload);

					category = resource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						BeanUtils.copyProperties(category, payloadCategory);
					}
					payload.setResourceCategory(payloadCategory);

					if (null != resource.getProgram())
						payload.setProgramId(resource.getProgram().getId());

					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.resource.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Returns an Resource Category Master List
	 * 
	 * @return @
	 */
	@GetMapping(path = "/{id}/resource/categorylist")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> getResourceCategoryList() {
		List<ResourceCategory> resourceCategoryList = null;
		ResourceCategoryPayLoad payload = null;
		List<ResourceCategoryPayLoad> payloadList = new ArrayList<>();
		try {
			resourceCategoryList = programResourceService.getResourceCategoryList();
			if (resourceCategoryList != null) {
				for (ResourceCategory category : resourceCategoryList) {
					payload = new ResourceCategoryPayLoad();
					BeanUtils.copyProperties(category, payload);
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.resource.category.error.list");
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for program resource end

	// Code for program region served start
	/**
	 * Creates ProgramRegionServed for an Program by Id
	 * 
	 * @param programRegionServedPayloadList
	 * @return @
	 */
	@PutMapping(path = "/{id}/region")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> createOrgRegions(
			@RequestBody List<ProgramRegionServedPayload> programRegionServedPayloadList) {
		List<ProgramRegionServed> progranRegionServedList = null;
		List<ProgramRegionServedPayload> payloadList = new ArrayList<>();
		ProgramRegionServedPayload payload = null;
		try {
			progranRegionServedList = programRegionServedService
					.createProgramRegionServed(programRegionServedPayloadList);
			if (null != progranRegionServedList) {
				for (ProgramRegionServed region : progranRegionServedList) {
					payload = new ProgramRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						RegionMasterPayload regionMasterPayload = new RegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());
						payload.setRegion(regionMasterPayload);
					}

					if (null != region.getProgram())
						payload.setProgramId(region.getProgram().getId());

					payload.setIsActive(region.getIsActive());
					payloadList.add(payload);
				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.region.error.created");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Returns ProgramRegionServed List
	 * 
	 * @param id
	 * @return @
	 */
	@GetMapping(path = "/{id}/regions")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgRegionsList(@PathVariable Long id) {
		List<ProgramRegionServed> programRegionList = null;
		ProgramRegionServedPayload payload = null;
		List<ProgramRegionServedPayload> payloadList = new ArrayList<>();
		try {
			programRegionList = programRegionServedService.getProgramRegionServedList(id);
			if (programRegionList != null) {
				for (ProgramRegionServed region : programRegionList) {
					payload = new ProgramRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						RegionMasterPayload regionMasterPayload = new RegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());
						payload.setRegion(regionMasterPayload);
					}
					if (null != region.getProgram())
						payload.setProgramId(region.getProgram().getId());

					payload.setIsActive(region.getIsActive());
					payloadList.add(payload);

				}
			}
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.region.error.list");
		}
		return sendSuccessResponse(payloadList);

	}

	/**
	 * Returns a Region Master List
	 * 
	 * @param filterPayload
	 * @return @
	 */
	@GetMapping(path = "/{id}/regionmasters")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgRegionsMasterList(RegionMasterFilterPayload filterPayload) {
		List<RegionMaster> orgRegionMasterList = null;
		RegionMasterPayload payload = null;
		List<RegionMasterPayload> payloadList = new ArrayList<>();
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		try {
			if (null != filterPayload) {
				orgRegionMasterList = programRegionServedService.getProgramRegionMasterList(filterPayload,
						exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

				if (orgRegionMasterList != null) {
					for (RegionMaster region : orgRegionMasterList) {
						payload = new RegionMasterPayload();
						payload.setRegionId(region.getId());
						payload.setRegionName(region.getRegionName());
						payloadList.add(payload);
					}
				}
			}
		} catch (Exception e) {
			exceptionResponse.setErrorMessage(e.getMessage());
			exceptionResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		}
		return sendSuccessResponse(payloadList);
	}
	// Code for program region served end

	// Code for program SPI data start
	/**
	 * Returns a SpiData Master List
	 * 
	 * @return @
	 */
	@GetMapping(path = "/spidata")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgSpiDataList() {
		List<SpiDataDimensionsPayload> payloadList = new ArrayList<>();
		try {
			payloadList = spiDataService.getSpiDataForResponse();
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.spidata.error.list");
		}
		return sendSuccessResponse(payloadList);
	}

	/**
	 * Creates SpiData Mapping for an Program by Id
	 * 
	 * @param payloadList
	 * @param progId
	 * @return @
	 */
	@PutMapping(path = "/{id}/spidata")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgSpiDataMapping(@RequestBody List<ProgramSpiDataMapPayload> payloadList,
			@PathVariable("id") Long progId) {
		Program program = programRepository.findProgramById(progId);
		if (program == null)
			return sendErrorResponse(customMessageSource.getMessage("prg.error.not_found"));
		if (null != payloadList) {
			try {
				programSpiDataService.createSpiDataMapping(payloadList, program);
			} catch (Exception e) {
				return sendExceptionResponse(e, "prog.spidata.error.created");
			}
			return sendSuccessResponse("prog.spidata.success.created");
		} else {
			return sendErrorResponse("org.bad.request");
		}

	}

	/**
	 * Returns a SpiData selected list for an Program by Id
	 * 
	 * @param orgId
	 * @return @
	 */
	@GetMapping(path = "/{id}/spidata/selected")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSpiData(@PathVariable("id") Long orgId) {
		List<ProgramSpiDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));

		try {
			payloadList = programSpiDataService.getSelectedSpiData(orgId);
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.spidata.error.selectedlist");
		}
		if (payloadList == null)
			return sendSuccessResponse(new ArrayList<>());

		return sendSuccessResponse(payloadList);
	}// Code for program SPI data end

	// Code for program SDG data start
	/**
	 * Returns a SdgData Master List
	 * 
	 * @return @
	 */
	@GetMapping(path = "/sdgdata")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgSdgDataList() {
		List<SdgGoalPayload> payloadList = new ArrayList<>();
		try {
			payloadList = sdgDataService.getSdgDataForResponse();
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.sdgdata.error.list");
		}
		return sendSuccessResponse(payloadList);

	}

	/**
	 * Creates SdgData Mapping for an Program by Id
	 * 
	 * @param payloadList
	 * @param progId
	 * @return @
	 */
	@PutMapping(path = "/{id}/sdgdata")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createProgramSdgDataMapping(@RequestBody List<ProgramSdgDataMapPayload> payloadList,
			@PathVariable("id") Long progId) {
		Program program = programRepository.findProgramById(progId);
		if (program == null)
			return sendErrorResponse(customMessageSource.getMessage("prg.error.not_found"));
		if (null != payloadList) {
			try {
				programSdgDataService.createSdgDataMapping(payloadList, program);
			} catch (Exception e) {
				return sendExceptionResponse(e, "prog.sdgdata.error.created");
			}
			return sendSuccessResponse("prog.sdgdata.success.created");
		} else {
			return sendErrorResponse("org.bad.request");
		}

	}

	/**
	 * Returns a SdgData selected list for an Program by Id
	 * 
	 * @param orgId
	 * @return @
	 */
	@GetMapping(path = "/{id}/sdgdata/selected")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSdgData(@PathVariable("id") Long orgId) {
		List<ProgramSdgDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));
		try {
			payloadList = programSdgDataService.getSelectedSdgData(orgId);
		} catch (Exception e) {
			return sendExceptionResponse(e, "prog.spidata.error.selectedlist");
		}

		if (payloadList == null)
			return sendSuccessResponse(new ArrayList<>());

		return sendSuccessResponse(payloadList);
	}// Code for program SDG data end

}
