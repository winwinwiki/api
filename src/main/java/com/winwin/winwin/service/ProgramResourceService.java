package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.ProgramResource;
import com.winwin.winwin.entity.ResourceCategory;
import com.winwin.winwin.payload.ProgramResourcePayLoad;

public interface ProgramResourceService {
	ProgramResource createOrUpdateProgramResource(ProgramResourcePayLoad programResourcePayLoad);

	void removeProgramResource(Long resourceId, Long organizationId);

	List<ProgramResource> getProgramResourceList(Long programId);

	ProgramResource getProgramResourceById(Long id);

	ResourceCategory getResourceCategoryById(Long categoryId);

	List<ResourceCategory> getResourceCategoryList();

}
