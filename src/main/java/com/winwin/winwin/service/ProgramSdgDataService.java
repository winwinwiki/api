package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.exception.SdgDataException;
import com.winwin.winwin.payload.ProgramSdgDataMapPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface ProgramSdgDataService {
	void createSdgDataMapping(List<ProgramSdgDataMapPayload> payloadList, Long orgId) throws SdgDataException;

	List<ProgramSdgDataMapPayload> getSelectedSdgData(Long orgId);
}
