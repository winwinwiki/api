package com.winwin.winwin.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.ProgramDataSet;
import com.winwin.winwin.exception.DataSetCategoryException;
import com.winwin.winwin.exception.DataSetException;
import com.winwin.winwin.payload.DataSetCategoryPayload;
import com.winwin.winwin.payload.ProgramDataSetPayLoad;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.DataSetCategoryRepository;
import com.winwin.winwin.repository.ProgramDataSetRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.ProgramDataSetService;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.util.CommonUtils;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class ProgramDataSetServiceImpl implements ProgramDataSetService {
	@Autowired
	private ProgramRepository programRepository;
	@Autowired
	private ProgramDataSetRepository programDataSetRepository;
	@Autowired
	private DataSetCategoryRepository dataSetCategoryRepository;
	@Autowired
	protected CustomMessageSource customMessageSource;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationHistoryService organizationHistoryService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataSetServiceImpl.class);

	private final Long CATEGORY_ID = -1L;

	/**
	 * create or update ProgramDataSet and DataSetCategory, create new entry in
	 * DataSetCategory if the value of CATEGORY_ID is -1L;
	 * 
	 * @param orgDataSetPayLoad
	 */
	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(value = "program_dataset_category_list"),
			@CacheEvict(value = "program_dataset_list") })
	public ProgramDataSet createOrUpdateProgramDataSet(ProgramDataSetPayLoad programDataSetPayLoad) {
		ProgramDataSet programDataSet = null;
		try {
			UserPayload user = userService.getCurrentUserDetails();
			if (null != programDataSetPayLoad && null != user) {
				programDataSet = constructProgramDataSet(programDataSetPayLoad, user);
				programDataSet = programDataSetRepository.saveAndFlush(programDataSet);

				if (null != programDataSet && null != programDataSet.getProgram()
						&& null != programDataSet.getProgram().getId()) {
					if (null != programDataSetPayLoad.getId()) {
						organizationHistoryService.createOrganizationHistory(user,
								programDataSetPayLoad.getOrganizationId(), programDataSetPayLoad.getProgramId(),
								OrganizationConstants.UPDATE, OrganizationConstants.DATASET, programDataSet.getId(),
								programDataSet.getDataSetCategory().getCategoryName(), "");
					} else {
						organizationHistoryService.createOrganizationHistory(user,
								programDataSetPayLoad.getOrganizationId(), programDataSetPayLoad.getProgramId(),
								OrganizationConstants.CREATE, OrganizationConstants.DATASET, programDataSet.getId(),
								programDataSet.getDataSetCategory().getCategoryName(), "");
					}
				}
			}
		} catch (Exception e) {
			if (null != programDataSetPayLoad.getId()) {
				LOGGER.error(customMessageSource.getMessage("org.dataset.exception.updated"), e);
			} else {
				LOGGER.error(customMessageSource.getMessage("org.dataset.exception.created"), e);
			}
		}
		return programDataSet;
	}

	/**
	 * delete ProgramDataSet by Id
	 * 
	 * @param dataSetId
	 * @param organizationId
	 * @param programId
	 */
	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(value = "program_dataset_category_list"),
			@CacheEvict(value = "program_dataset_list") })
	public void removeProgramDataSet(Long dataSetId, Long organizationId, Long programId) {
		try {
			ProgramDataSet dataSet = programDataSetRepository.findProgramDataSetById(dataSetId);
			UserPayload user = userService.getCurrentUserDetails();
			Date date = CommonUtils.getFormattedDate();
			if (null != dataSet && null != user) {
				dataSet.setUpdatedAt(date);
				dataSet.setUpdatedBy(user.getUserDisplayName());
				dataSet.setUpdatedByEmail(user.getEmail());
				dataSet.setIsActive(false);
				dataSet = programDataSetRepository.saveAndFlush(dataSet);

				if (null != dataSet) {
					organizationHistoryService.createOrganizationHistory(user, organizationId, programId,
							OrganizationConstants.DELETE, OrganizationConstants.DATASET, dataSet.getId(),
							dataSet.getDataSetCategory().getCategoryName(), "");
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.error.deleted"), e);
		}
	}

	/**
	 * returns DataSetCategory by Id
	 * 
	 * @param categoryId
	 */
	@Override
	public DataSetCategory getDataSetCategoryById(Long categoryId) {
		return dataSetCategoryRepository.getOne(categoryId);
	}

	/**
	 * returns ProgramDataSet List by programId
	 * 
	 * @param id
	 */
	@Override
	@CachePut(value = "program_dataset_list")
	public List<ProgramDataSet> getProgramDataSetList(Long id) {
		return programDataSetRepository.findAllActiveProgramDataSets(id);
	}

	/**
	 * returns DataSetCategory List
	 * 
	 * @param categoryId
	 */
	@Override
	@CachePut(value = "program_dataset_category_list")
	public List<DataSetCategory> getDataSetCategoryList() {
		return dataSetCategoryRepository.findAll();
	}

	private ProgramDataSet constructProgramDataSet(ProgramDataSetPayLoad programDataSetPayLoad, UserPayload user) {
		ProgramDataSet programDataSet = null;
		try {
			Date date = CommonUtils.getFormattedDate();
			if (null != programDataSetPayLoad.getId()) {
				programDataSet = programDataSetRepository.getOne(programDataSetPayLoad.getId());
				if (programDataSet == null) {
					throw new DataSetException("Org dataset record not found for Id: " + programDataSetPayLoad.getId()
							+ " to update in DB ");
				}
			} else {
				programDataSet = new ProgramDataSet();
				programDataSet.setCreatedAt(date);
				programDataSet.setCreatedBy(user.getUserDisplayName());
				programDataSet.setCreatedByEmail(user.getEmail());
			}
			setDataSetCategory(programDataSetPayLoad, programDataSet, user);
			BeanUtils.copyProperties(programDataSetPayLoad, programDataSet);
			programDataSet.setIsActive(true);
			programDataSet.setUpdatedAt(date);
			programDataSet.setUpdatedBy(user.getUserDisplayName());
			programDataSet.setUpdatedByEmail(user.getEmail());

			if (null != programDataSetPayLoad.getProgramId()) {
				Program program = programRepository.findProgramById(programDataSetPayLoad.getProgramId());
				programDataSet.setProgram(program);
			}

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.exception.construct"), e);
		}
		return programDataSet;
	}

	private void setDataSetCategory(ProgramDataSetPayLoad programDataSetPayLoad, ProgramDataSet programDataSet,
			UserPayload user) {
		DataSetCategory dataSetCategory = null;
		try {
			if (null != programDataSetPayLoad.getDataSetCategory()) {
				Long categoryId = programDataSetPayLoad.getDataSetCategory().getId();
				if (null != categoryId) {
					if (categoryId.equals(CATEGORY_ID)) {
						dataSetCategory = saveDataSetCategory(programDataSetPayLoad.getDataSetCategory(), user);
						LOGGER.info(customMessageSource.getMessage("org.dataset.category.success.created"));
						programDataSet.setDataSetCategory(dataSetCategory);

					} else {
						dataSetCategory = dataSetCategoryRepository.getOne(categoryId);
						if (dataSetCategory == null) {
							throw new DataSetCategoryException(
									"Org dataset category record not found for Id: " + categoryId + " in DB ");
						} else {
							programDataSet.setDataSetCategory(dataSetCategory);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.category.error.created"), e);
		}
	}

	private DataSetCategory saveDataSetCategory(DataSetCategoryPayload categoryFromPayLoad, UserPayload user) {
		DataSetCategory category = new DataSetCategory();
		try {
			Date date = CommonUtils.getFormattedDate();
			if (!StringUtils.isEmpty(categoryFromPayLoad.getCategoryName())) {
				category.setCategoryName(categoryFromPayLoad.getCategoryName());
			}
			category.setCreatedAt(date);
			category.setUpdatedAt(date);
			category.setCreatedBy(user.getUserDisplayName());
			category.setUpdatedBy(user.getUserDisplayName());
			category.setCreatedByEmail(user.getEmail());
			category.setUpdatedByEmail(user.getEmail());
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.category.error.updated"), e);
		}
		return dataSetCategoryRepository.saveAndFlush(category);
	}

}
