package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.entity.ProgramRegionServed;
import com.winwin.winwin.entity.RegionMaster;
import com.winwin.winwin.payload.ProgramRegionServedPayload;

public interface ProgramRegionServedService {
	List<ProgramRegionServed> createProgramRegionServed(List<ProgramRegionServedPayload> programRegionPayloadList);

	List<ProgramRegionServed> getProgramRegionServedList(Long programId);

	List<RegionMaster> getRegionMasterList();
}
