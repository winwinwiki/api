package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.ProgramDataSet;
import com.winwin.winwin.entity.ProgramRegionServed;
import com.winwin.winwin.entity.ProgramResource;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.exception.DataSetCategoryException;
import com.winwin.winwin.exception.DataSetException;
import com.winwin.winwin.exception.OrganizationException;
import com.winwin.winwin.exception.RegionServedException;
import com.winwin.winwin.exception.ResourceCategoryException;
import com.winwin.winwin.exception.ResourceException;
import com.winwin.winwin.exception.SdgDataException;
import com.winwin.winwin.exception.SpiDataException;
import com.winwin.winwin.payload.DataSetCategoryPayload;
import com.winwin.winwin.payload.ProgramDataSetPayLoad;
import com.winwin.winwin.payload.ProgramRegionServedPayload;
import com.winwin.winwin.payload.ProgramResourcePayLoad;
import com.winwin.winwin.payload.ProgramResponsePayload;
import com.winwin.winwin.payload.ProgramSdgDataMapPayload;
import com.winwin.winwin.payload.ProgramSpiDataMapPayload;
import com.winwin.winwin.payload.RegionMasterPayload;
import com.winwin.winwin.payload.ResourceCategoryPayLoad;
import com.winwin.winwin.payload.SdgGoalPayload;
import com.winwin.winwin.payload.SpiDataDimensionsPayload;
import com.winwin.winwin.repository.ProgramDataSetRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.repository.ProgramResourceRepository;
import com.winwin.winwin.service.OrgSdgDataService;
import com.winwin.winwin.service.OrgSpiDataService;
import com.winwin.winwin.service.ProgramDataSetService;
import com.winwin.winwin.service.ProgramRegionServedService;
import com.winwin.winwin.service.ProgramResourceService;
import com.winwin.winwin.service.ProgramSdgDataService;
import com.winwin.winwin.service.ProgramService;
import com.winwin.winwin.service.ProgramSpiDataService;
import com.winwin.winwin.service.SdgDataService;
import com.winwin.winwin.service.SpiDataService;

/**
 * @author ArvindKhatik
 *
 */
@RestController
@RequestMapping(value = "/program")

public class ProgramController extends BaseController {

	@Autowired
	private ProgramRegionServedService programRegionServedService;

	@Autowired
	OrgSpiDataService orgSpiDataService;

	@Autowired
	SpiDataService spiDataService;
	@Autowired
	SdgDataService sdgDataService;

	@Autowired
	OrgSdgDataService orgSdgDataService;

	@Autowired
	ProgramSdgDataService programSdgDataService;
	@Autowired
	ProgramSpiDataService programSpiDataService;

	@Autowired
	ProgramService programService;

	@Autowired
	ProgramRepository programRepository;

	@Autowired
	ProgramDataSetRepository programDataSetRepository;

	@Autowired
	ProgramDataSetService programDataSetService;

	@Autowired
	ProgramResourceService programResourceService;

	@Autowired
	private ProgramResourceRepository programResourceRepository;

	// Code for program data set start
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@Transactional
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
			throw new OrganizationException(customMessageSource.getMessage("prg.error.fetch") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);

	}

	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createProgrmDataSet(@Valid @RequestBody ProgramDataSetPayLoad programDataSetPayLoad)
			throws DataSetException {
		ProgramDataSet programDataSet = null;
		ProgramDataSetPayLoad payload = null;
		DataSetCategory category = null;
		DataSetCategoryPayload payloadCategory = null;

		if (null != programDataSetPayLoad) {
			try {
				programDataSet = programDataSetService.createOrUpdateProgramDataSet(programDataSetPayLoad);
				if (null != programDataSet) {
					payload = new ProgramDataSetPayLoad();
					payload.setId(programDataSet.getId());
					category = programDataSet.getDataSetCategory();
					if (null != category) {
						payloadCategory = new DataSetCategoryPayload();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
						payloadCategory.setAdminUrl(category.getAdminUrl());
					}
					payload.setDataSetCategory(payloadCategory);
					payload.setProgramId(programDataSet.getProgramId());
					payload.setDescription(programDataSet.getDescription());
					payload.setType(programDataSet.getType());
					payload.setUrl(programDataSet.getUrl());
					payload.setAdminUrl(programDataSet.getAdminUrl());
					payload.setIsActive(programDataSet.getIsActive());
				}

			} catch (Exception e) {
				throw new DataSetException(
						customMessageSource.getMessage("prog.dataset.error.created") + ": " + e.getMessage());
			}

		} else {
			return sendErrorResponse("org.bad.request");

		}

		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateProgramDataSet(@Valid @RequestBody ProgramDataSetPayLoad programDataSetPayLoad)
			throws DataSetException {
		ProgramDataSet dataSet = null;
		DataSetCategory category = null;
		DataSetCategoryPayload payloadCategory = null;
		ProgramDataSetPayLoad payload = null;
		if (null != programDataSetPayLoad && null != programDataSetPayLoad.getId()) {
			Long id = programDataSetPayLoad.getId();
			dataSet = programDataSetRepository.findProgramDataSetById(id);
			if (dataSet == null) {
				throw new DataSetException(customMessageSource.getMessage("prog.dataset.error.not_found"));
			} else {
				payload = new ProgramDataSetPayLoad();
				dataSet = programDataSetService.createOrUpdateProgramDataSet(programDataSetPayLoad);
				payload.setId(dataSet.getId());
				category = dataSet.getDataSetCategory();
				if (null != category) {
					payloadCategory = new DataSetCategoryPayload();
					payloadCategory.setId(category.getId());
					payloadCategory.setCategoryName(category.getCategoryName());
					payloadCategory.setAdminUrl(category.getAdminUrl());
				}
				payload.setDataSetCategory(payloadCategory);
				payload.setOrganizationId(programDataSetPayLoad.getOrganizationId());
				payload.setProgramId(programDataSetPayLoad.getProgramId());
				payload.setAdminUrl(programDataSetPayLoad.getAdminUrl());
				payload.setDescription(dataSet.getDescription());
				payload.setType(dataSet.getType());
				payload.setUrl(dataSet.getUrl());
				payload.setIsActive(dataSet.getIsActive());
			}
		} else {
			return sendErrorResponse("org.bad.request");

		}

		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/dataset", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> deleteProgramDataSet(@RequestBody ProgramDataSetPayLoad programDataSetPayLoad)
			throws DataSetException {
		try {
			if (null != programDataSetPayLoad && null != programDataSetPayLoad.getId()) {
				Long id = programDataSetPayLoad.getId();
				ProgramDataSet dataSet = programDataSetRepository.findProgramDataSetById(id);
				if (dataSet == null) {
					throw new DataSetException(customMessageSource.getMessage("prog.dataset.error.not_found"));
				}
				programDataSetService.removeProgramDataSet(id, programDataSetPayLoad.getOrganizationId());
			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new DataSetException(
					customMessageSource.getMessage("prog.dataset.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("prog.dataset.success.deleted");
	}

	@RequestMapping(value = "{id}/datasets", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getProgramDataSetList(@PathVariable("id") Long id) throws DataSetException {
		List<ProgramDataSet> programDataSetList = null;
		ProgramDataSetPayLoad payload = null;
		DataSetCategory category = null;
		DataSetCategoryPayload payloadCategory = null;
		List<ProgramDataSetPayLoad> payloadList = null;
		try {
			programDataSetList = programDataSetService.getProgramDataSetList(id);
			if (programDataSetList == null) {
				throw new DataSetException(customMessageSource.getMessage("prog.dataset.error.not_found"));
			} else {
				payloadList = new ArrayList<>();
				for (ProgramDataSet dataSet : programDataSetList) {
					payload = new ProgramDataSetPayLoad();
					payload.setId(dataSet.getId());
					category = dataSet.getDataSetCategory();
					if (null != category) {
						payloadCategory = new DataSetCategoryPayload();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
						payloadCategory.setAdminUrl(category.getAdminUrl());
					}
					payload.setDataSetCategory(payloadCategory);
					payload.setProgramId(dataSet.getProgramId());
					payload.setDescription(dataSet.getDescription());
					payload.setType(dataSet.getType());
					payload.setUrl(dataSet.getUrl());
					payload.setAdminUrl(dataSet.getAdminUrl());
					payload.setIsActive(dataSet.getIsActive());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new DataSetException(
					customMessageSource.getMessage("prog.dataset.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/dataset/categorylist", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getDataSetCategoryList(HttpServletResponse httpServletResponce)
			throws DataSetCategoryException {
		List<DataSetCategory> dataSetCategoryList = null;
		List<DataSetCategoryPayload> payloadList = null;
		DataSetCategoryPayload payload = null;
		try {
			dataSetCategoryList = programDataSetService.getDataSetCategoryList();
			if (dataSetCategoryList == null) {
				throw new DataSetCategoryException(
						customMessageSource.getMessage("prog.dataset.category.error.not_found"));
			} else {
				payloadList = new ArrayList<>();
				for (DataSetCategory category : dataSetCategoryList) {
					payload = new DataSetCategoryPayload();
					payload.setId(category.getId());
					payload.setCategoryName(category.getCategoryName());
					payload.setAdminUrl(category.getAdminUrl());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new DataSetCategoryException(
					customMessageSource.getMessage("prog.dataset.category.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for program data set end

	// Code for program resource start
	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createProgramResource(@Valid @RequestBody ProgramResourcePayLoad programResourcePayLoad)
			throws ResourceException {
		ProgramResource programResource = null;
		ProgramResourcePayLoad payload = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		if (null != programResourcePayLoad) {
			try {
				programResource = programResourceService.createOrUpdateProgramResource(programResourcePayLoad);
				if (null != programResource) {
					payload = new ProgramResourcePayLoad();
					payload.setId(programResource.getId());
					category = programResource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
						payloadCategory.setAdminUrl(category.getAdminUrl());
					}
					payload.setResourceCategory(payloadCategory);
					payload.setProgramId(programResource.getProgramId());
					payload.setCount(programResource.getCount());
					payload.setDescription(programResource.getDescription());
					payload.setAdminUrl(programResource.getAdminUrl());
					payload.setIsActive(programResource.getIsActive());
				}
			} catch (Exception e) {
				throw new ResourceException(
						customMessageSource.getMessage("prog.resource.error.created") + ": " + e.getMessage());
			}

		} else {
			return sendErrorResponse("org.bad.request");

		}
		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> updateProgramResource(@Valid @RequestBody ProgramResourcePayLoad programResourcePayLoad)
			throws ResourceException {
		ProgramResource programResource = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		ProgramResourcePayLoad payload = null;
		try {
			if (null != programResourcePayLoad && null != programResourcePayLoad.getId()) {
				Long id = programResourcePayLoad.getId();
				programResource = programResourceRepository.findProgramResourceById(id);
				if (programResource == null) {
					throw new ResourceException(customMessageSource.getMessage("prog.resource.error.not_found"));
				} else {
					programResource = programResourceService.createOrUpdateProgramResource(programResourcePayLoad);
					payload = new ProgramResourcePayLoad();
					payload.setId(programResource.getId());
					category = programResource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
						payloadCategory.setAdminUrl(category.getAdminUrl());
					}
					payload.setResourceCategory(payloadCategory);
					payload.setProgramId(programResource.getProgramId());
					payload.setCount(programResource.getCount());
					payload.setAdminUrl(programResource.getAdminUrl());
					payload.setDescription(programResource.getDescription());
					payload.setIsActive(programResource.getIsActive());
				}

			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new ResourceException(
					customMessageSource.getMessage("prog.resource.error.updated") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payload);
	}

	@Transactional
	@RequestMapping(value = "/{id}/resource", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> deleteProgramResource(@Valid @RequestBody ProgramResourcePayLoad programResourcePayLoad)
			throws ResourceException {
		try {
			if (null != programResourcePayLoad && null != programResourcePayLoad.getId()) {
				Long id = programResourcePayLoad.getId();
				ProgramResource programResource = programResourceRepository.findProgramResourceById(id);
				if (programResource == null) {
					throw new ResourceException(customMessageSource.getMessage("prog.resource.error.not_found"));
				}
				programResourceService.removeProgramResource(id, programResourcePayLoad.getOrganizationId());
			} else {
				return sendErrorResponse("org.bad.request");

			}

		} catch (Exception e) {
			throw new ResourceException(
					customMessageSource.getMessage("prog.resource.error.deleted") + ": " + e.getMessage());
		}
		return sendSuccessResponse("prog.resource.success.deleted");
	}

	@RequestMapping(value = "/{id}/resources", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getProgramResourceList(@PathVariable("id") Long id) throws ResourceException {
		List<ProgramResource> programResourceList = null;
		ProgramResourcePayLoad payload = null;
		ResourceCategory category = null;
		ResourceCategoryPayLoad payloadCategory = null;
		List<ProgramResourcePayLoad> payloadList = null;
		try {
			programResourceList = programResourceService.getProgramResourceList(id);
			if (programResourceList == null) {
				throw new ResourceException(customMessageSource.getMessage("prog.resource.error.not_found"));
			} else {
				payloadList = new ArrayList<>();
				for (ProgramResource resource : programResourceList) {
					payload = new ProgramResourcePayLoad();
					payload.setId(resource.getId());
					category = resource.getResourceCategory();
					if (null != category) {
						payloadCategory = new ResourceCategoryPayLoad();
						payloadCategory.setId(category.getId());
						payloadCategory.setCategoryName(category.getCategoryName());
						payloadCategory.setAdminUrl(category.getAdminUrl());
					}
					payload.setResourceCategory(payloadCategory);
					payload.setProgramId(resource.getProgramId());
					payload.setCount(resource.getCount());
					payload.setDescription(resource.getDescription());
					payload.setIsActive(resource.getIsActive());
					payload.setAdminUrl(resource.getAdminUrl());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new ResourceException(
					customMessageSource.getMessage("prog.resource.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/resource/categorylist", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> getResourceCategoryList(HttpServletResponse httpServletResponce)
			throws ResourceCategoryException {
		List<ResourceCategory> resourceCategoryList = null;
		ResourceCategoryPayLoad payload = null;
		List<ResourceCategoryPayLoad> payloadList = null;

		try {
			resourceCategoryList = programResourceService.getResourceCategoryList();
			if (resourceCategoryList == null) {
				throw new ResourceCategoryException(
						customMessageSource.getMessage("prog.resource.category.error.not_found"));
			} else {
				payloadList = new ArrayList<>();
				for (ResourceCategory category : resourceCategoryList) {
					payload = new ResourceCategoryPayLoad();
					payload.setId(category.getId());
					payload.setCategoryName(category.getCategoryName());
					payload.setAdminUrl(category.getAdminUrl());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new ResourceCategoryException(
					customMessageSource.getMessage("prog.resource.category.error.list") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);

	}

	// Code for program resource end

	// Code for program region served start
	@RequestMapping(value = "/{id}/region", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> createOrgRegions(
			@RequestBody List<ProgramRegionServedPayload> programRegionServedPayloadList) throws RegionServedException {
		List<ProgramRegionServed> progranRegionServedList = null;
		List<ProgramRegionServedPayload> payloadList = null;
		ProgramRegionServedPayload payload = null;
		try {
			progranRegionServedList = programRegionServedService
					.createProgramRegionServed(programRegionServedPayloadList);
			if (null != progranRegionServedList) {
				payloadList = new ArrayList<>();
				for (ProgramRegionServed region : progranRegionServedList) {
					payload = new ProgramRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						RegionMasterPayload regionMasterPayload = new RegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());
						regionMasterPayload.setAdminUrl(region.getRegionMaster().getAdminUrl());
						payload.setRegion(regionMasterPayload);
					}
					payload.setProgramId(region.getProgramId());
					payload.setIsActive(region.getIsActive());
					payload.setAdminUrl(region.getAdminUrl());
					payloadList.add(payload);

				}
			}
		} catch (Exception e) {
			throw new RegionServedException(
					customMessageSource.getMessage("prog.region.error.created") + ": " + e.getMessage());
		}
		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "/{id}/regions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getOrgRegionsList(@PathVariable Long id) throws RegionServedException {
		List<ProgramRegionServed> programRegionList = null;
		ProgramRegionServedPayload payload = null;
		List<ProgramRegionServedPayload> payloadList = null;
		try {
			programRegionList = programRegionServedService.getProgramRegionServedList(id);
			if (programRegionList == null) {
				throw new RegionServedException(customMessageSource.getMessage("prog.region.error.not_found"));
			} else {
				payloadList = new ArrayList<>();
				for (ProgramRegionServed region : programRegionList) {
					payload = new ProgramRegionServedPayload();
					payload.setId(region.getId());
					if (null != region.getRegionMaster()) {
						RegionMasterPayload regionMasterPayload = new RegionMasterPayload();
						regionMasterPayload.setRegionId(region.getRegionMaster().getId());
						regionMasterPayload.setRegionName(region.getRegionMaster().getRegionName());
						regionMasterPayload.setAdminUrl(region.getRegionMaster().getAdminUrl());
						payload.setRegion(regionMasterPayload);
					}
					payload.setAdminUrl(region.getAdminUrl());
					payload.setProgramId(region.getProgramId());
					payload.setIsActive(region.getIsActive());
					payloadList.add(payload);

				}

			}
		} catch (Exception e) {
			throw new RegionServedException(customMessageSource.getMessage("prog.region.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/regionmasters", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') ")
	public ResponseEntity<?> getProgramRegionsMasterList() throws RegionServedException {
		List<RegionMaster> regionMasterList = null;
		RegionMasterPayload payload = null;
		List<RegionMasterPayload> payloadList = null;
		try {
			regionMasterList = programRegionServedService.getRegionMasterList();
			if (regionMasterList == null) {
				throw new RegionServedException(customMessageSource.getMessage("prog.region.error.not_found"));
			} else {
				payloadList = new ArrayList<RegionMasterPayload>();
				for (RegionMaster region : regionMasterList) {
					payload = new RegionMasterPayload();
					payload.setRegionId(region.getId());
					payload.setRegionName(region.getRegionName());
					payload.setAdminUrl(region.getAdminUrl());
					payloadList.add(payload);
				}

			}
		} catch (Exception e) {
			throw new RegionServedException(customMessageSource.getMessage("prog.region.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}
	// Code for program region served end

	// Code for program SPI data start
	@RequestMapping(value = "/{id}/spidata", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgSpiDataList() throws SpiDataException {
		List<SpiDataDimensionsPayload> payloadList = new ArrayList<SpiDataDimensionsPayload>();
		try {
			payloadList = spiDataService.getSpiDataForResponse();
			if (payloadList == null) {
				throw new SpiDataException(customMessageSource.getMessage("prog.spidata.error.not_found"));
			}

		} catch (Exception e) {
			throw new SpiDataException(customMessageSource.getMessage("prog.spidata.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/spidata", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createOrgSpiDataMapping(@RequestBody List<ProgramSpiDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) throws SpiDataException {
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));
		if (null != payloadList) {
			try {
				programSpiDataService.createSpiDataMapping(payloadList, orgId);
			} catch (Exception e) {
				throw new SpiDataException(customMessageSource.getMessage("prog.spidata.error.created"));
			}
			return sendSuccessResponse("prog.spidata.success.created");

		} else {
			return sendErrorResponse("org.bad.request");
		}

	}

	@RequestMapping(value = "/{id}/spidata/selected", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSpiData(@PathVariable("id") Long orgId) throws SpiDataException {
		List<ProgramSpiDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));

		try {
			payloadList = programSpiDataService.getSelectedSpiData(orgId);

			if (payloadList == null) {
				throw new SpiDataException(customMessageSource.getMessage("prog.spidata.error.not_found"));
			}

		} catch (Exception e) {
			throw new SpiDataException(customMessageSource.getMessage("prog.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(payloadList);

	}// Code for program SPI data end

	// Code for program SDG data start
	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getOrgSdgDataList() throws SdgDataException {
		List<SdgGoalPayload> payloadList = new ArrayList<SdgGoalPayload>();
		try {
			payloadList = sdgDataService.getSdgDataForResponse();
			if (payloadList == null) {
				throw new SdgDataException(customMessageSource.getMessage("prog.sdgdata.error.not_found"));
			}

		} catch (Exception e) {
			throw new SdgDataException(customMessageSource.getMessage("prog.sdgdata.error.list"));
		}
		return sendSuccessResponse(payloadList);

	}

	@RequestMapping(value = "/{id}/sdgdata", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> createProgramSdgDataMapping(@RequestBody List<ProgramSdgDataMapPayload> payloadList,
			@PathVariable("id") Long orgId) throws SdgDataException {
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));
		if (null != payloadList) {
			try {
				programSdgDataService.createSdgDataMapping(payloadList, orgId);
			} catch (Exception e) {
				throw new SdgDataException(customMessageSource.getMessage("prog.sdgdata.error.created"));
			}
			return sendSuccessResponse("prog.sdgdata.success.created");

		} else {
			return sendErrorResponse("org.bad.request");
		}

	}

	@RequestMapping(value = "/{id}/sdgdata/selected", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
	public ResponseEntity<?> getSelectedOrgSdgData(@PathVariable("id") Long orgId) throws SdgDataException {
		List<ProgramSdgDataMapPayload> payloadList = null;
		if (orgId == null)
			return sendErrorResponse(customMessageSource.getMessage("prog.error.organization.null"));
		try {
			payloadList = programSdgDataService.getSelectedSdgData(orgId);

			if (payloadList == null) {
				throw new SdgDataException(customMessageSource.getMessage("prog.sdgdata.error.not_found"));
			}
		} catch (Exception e) {
			throw new SdgDataException(customMessageSource.getMessage("prog.spidata.error.selectedlist"));
		}
		return sendSuccessResponse(payloadList);

	}// Code for program SDG data end

}
