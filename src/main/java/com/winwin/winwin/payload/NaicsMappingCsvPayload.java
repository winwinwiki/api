package com.winwin.winwin.payload;

import lombok.Data;

/**
 * @author ArvindKhatik
 *
 */
@Data
public class NaicsMappingCsvPayload {
	private String naicsCode;
	private String spiTagIds;
	private String sdgTagIds;
}
