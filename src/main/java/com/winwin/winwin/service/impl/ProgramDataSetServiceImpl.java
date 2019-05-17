package com.winwin.winwin.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.ProgramDataSet;
import com.winwin.winwin.exception.DataSetCategoryException;
import com.winwin.winwin.exception.DataSetException;
import com.winwin.winwin.payload.DataSetCategoryPayLoad;
import com.winwin.winwin.payload.ProgramDataSetPayLoad;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.repository.DataSetCategoryRepository;
import com.winwin.winwin.repository.OrganizationHistoryRepository;
import com.winwin.winwin.repository.ProgramDataSetRepository;
import com.winwin.winwin.service.OrganizationHistoryService;
import com.winwin.winwin.service.ProgramDataSetService;
import com.winwin.winwin.service.UserService;

import io.micrometer.core.instrument.util.StringUtils;

@Service
public class ProgramDataSetServiceImpl implements ProgramDataSetService {
	@Autowired
	ProgramDataSetRepository programDataSetRepository;

	@Autowired
	DataSetCategoryRepository dataSetCategoryRepository;

	@Autowired
	OrganizationHistoryRepository orgHistoryRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	@Autowired
	UserService userService;

	@Autowired
	OrganizationHistoryService organizationHistoryService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataSetServiceImpl.class);

	private final Long CATEGORY_ID = -1L;

	private ProgramDataSet constructProgramDataSet(ProgramDataSetPayLoad programDataSetPayLoad, UserPayload user) {
		ProgramDataSet programDataSet = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = null;
		try {
			if (null != programDataSetPayLoad.getId()) {
				programDataSet = programDataSetRepository.getOne(programDataSetPayLoad.getId());
			} else {
				programDataSet = new ProgramDataSet();
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				programDataSet.setCreatedAt(sdf.parse(formattedDte));
				programDataSet.setCreatedBy(user.getEmail());
			}

			if (programDataSet == null) {
				throw new DataSetException(
						"Org dataset record not found for Id: " + programDataSetPayLoad.getId() + " to update in DB ");
			} else {
				setDataSetCategory(programDataSetPayLoad, programDataSet, user);
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				programDataSet.setProgramId(programDataSetPayLoad.getProgramId());
				programDataSet.setDescription(programDataSetPayLoad.getDescription());
				programDataSet.setType(programDataSetPayLoad.getType());
				programDataSet.setUrl(programDataSetPayLoad.getUrl());
				programDataSet.setUpdatedAt(sdf.parse(formattedDte));
				programDataSet.setUpdatedBy(user.getEmail());

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

	public DataSetCategory saveDataSetCategory(DataSetCategoryPayLoad categoryFromPayLoad, UserPayload user) {
		DataSetCategory category = new DataSetCategory();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
			if (!StringUtils.isEmpty(categoryFromPayLoad.getCategoryName())) {
				category.setCategoryName(categoryFromPayLoad.getCategoryName());
			}
			category.setCreatedAt(sdf.parse(formattedDte));
			category.setUpdatedAt(sdf.parse(formattedDte));
			category.setCreatedBy(user.getEmail());
			category.setUpdatedBy(user.getEmail());
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.category.error.updated"), e);
		}

		return dataSetCategoryRepository.saveAndFlush(category);
	}

	@Override
	public ProgramDataSet createOrUpdateProgramDataSet(ProgramDataSetPayLoad programDataSetPayLoad) {
		// TODO Auto-generated method stub
		UserPayload user = userService.getCurrentUserDetails();
		ProgramDataSet programDataSet = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));

		try {
			if (null != programDataSetPayLoad && null != user) {
				programDataSet = constructProgramDataSet(programDataSetPayLoad, user);
				programDataSet = programDataSetRepository.saveAndFlush(programDataSet);

				if (null != programDataSet.getId()) {

					if (null != programDataSetPayLoad.getId()) {
						organizationHistoryService.createOrganizationHistory(user,
								programDataSetPayLoad.getOrganizationId(), sdf, formattedDte,
								OrganizationConstants.UPDATE, OrganizationConstants.DATASET, programDataSet.getId(),
								programDataSet.getDescription());
					} else {
						organizationHistoryService.createOrganizationHistory(user,
								programDataSetPayLoad.getOrganizationId(), sdf, formattedDte,
								OrganizationConstants.CREATE, OrganizationConstants.DATASET, programDataSet.getId(),
								programDataSet.getDescription());
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

	@Override
	public void removeProgramDataSet(Long dataSetId, Long organizationId) {
		// TODO Auto-generated method stub
		ProgramDataSet dataSet = programDataSetRepository.findProgramDataSetById(dataSetId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
		UserPayload user = userService.getCurrentUserDetails();

		if (null != dataSet && null != user) {
			try {
				dataSet.setUpdatedAt(sdf.parse(formattedDte));
				dataSet.setUpdatedBy(user.getEmail());
				dataSet.setIsActive(false);

				dataSet = programDataSetRepository.saveAndFlush(dataSet);

				if (null != dataSet) {
					organizationHistoryService.createOrganizationHistory(user, organizationId, sdf, formattedDte,
							OrganizationConstants.DELETE, OrganizationConstants.DATASET, dataSet.getId(),
							dataSet.getDescription());
				}
			} catch (Exception e) {
				LOGGER.error(customMessageSource.getMessage("org.dataset.error.deleted"), e);
			}

		}
	}

	@Override
	public DataSetCategory getDataSetCategoryById(Long categoryId) {
		return dataSetCategoryRepository.getOne(categoryId);
	}

	@Override
	public List<DataSetCategory> getDataSetCategoryList() {
		return dataSetCategoryRepository.findAll();
	}

	@Override
	public List<ProgramDataSet> getProgramDataSetList(Long id) {
		// TODO Auto-generated method stub
		return programDataSetRepository.findAllProgramDataSetList(id);
	}

	@Override
	public ProgramDataSet getProgramDataSet(ProgramDataSetPayLoad programDataSetPayLoad) {
		UserPayload user = userService.getCurrentUserDetails();
		ProgramDataSet programDataSet = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = null;
		try {
			if (null != programDataSetPayLoad.getId()) {
				programDataSet = programDataSetRepository.getOne(programDataSetPayLoad.getId());
			} else {
				programDataSet = new ProgramDataSet();
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				programDataSet.setCreatedAt(sdf.parse(formattedDte));
				programDataSet.setCreatedBy(user.getEmail());
			}

			if (programDataSet == null) {
				throw new DataSetException(
						"Org dataset record not found for Id: " + programDataSetPayLoad.getId() + " to update in DB ");
			} else {
				setDataSetCategory(programDataSetPayLoad, programDataSet, user);
				formattedDte = sdf.format(new Date(System.currentTimeMillis()));
				programDataSet.setProgramId(programDataSetPayLoad.getProgramId());
				programDataSet.setDescription(programDataSetPayLoad.getDescription());
				programDataSet.setType(programDataSetPayLoad.getType());
				programDataSet.setUrl(programDataSetPayLoad.getUrl());
				programDataSet.setUpdatedAt(sdf.parse(formattedDte));
				programDataSet.setUpdatedBy(user.getEmail());

			}

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.dataset.exception.construct"), e);
		}

		return programDataSet;
	}
}
