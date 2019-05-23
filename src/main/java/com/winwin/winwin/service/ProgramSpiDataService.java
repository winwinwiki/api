package com.winwin.winwin.service;

import java.util.List;

import com.winwin.winwin.exception.SpiDataException;
import com.winwin.winwin.payload.ProgramSpiDataMapPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface ProgramSpiDataService {
	void createSpiDataMapping(List<ProgramSpiDataMapPayload> payloadList, Long orgId) throws SpiDataException;

	List<ProgramSpiDataMapPayload> getSelectedSpiData(Long orgId);
}
