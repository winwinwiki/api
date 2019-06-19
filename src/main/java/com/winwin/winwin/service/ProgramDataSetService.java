package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.DataSetCategory;
import com.winwin.winwin.entity.ProgramDataSet;
import com.winwin.winwin.payload.ProgramDataSetPayLoad;

/**
 * @author ArvindKhatik
 *
 */
public interface ProgramDataSetService {
	ProgramDataSet createOrUpdateProgramDataSet(ProgramDataSetPayLoad programDataSetPayLoad);

	List<ProgramDataSet> getProgramDataSetList(Long id);

	ProgramDataSet getProgramDataSet(ProgramDataSetPayLoad programDataSetPayLoad);

	void removeProgramDataSet(Long dataSetId, Long organizationId, Long programId);

	DataSetCategory getDataSetCategoryById(Long categoryId);

	List<DataSetCategory> getDataSetCategoryList();

}
