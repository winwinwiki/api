package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.ProgramRegionServed;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.ProgramRegionServedPayload;
import com.winwin.winwin.payload.RegionMasterFilterPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface ProgramRegionServedService {
	List<ProgramRegionServed> createProgramRegionServed(List<ProgramRegionServedPayload> programRegionPayloadList);

	List<ProgramRegionServed> getProgramRegionServedList(Long programId);

	List<RegionMaster> getProgramRegionMasterList(RegionMasterFilterPayload filterPayload,
			ExceptionResponse exceptionResponse);
}
