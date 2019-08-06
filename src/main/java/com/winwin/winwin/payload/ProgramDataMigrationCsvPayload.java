package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProgramDataMigrationCsvPayload {
	private Long id;
	private String name;
	private String description;
	private String naicsCode;
	private String nteeCode;
	private String websiteUrl;
	private String regionServedIds;
	private String resourceIds;
	private String datasetIds;
	private String datasetType;
	private String spiTagIds;
	private String sdgTagIds;
	private Long parentId;
	private Long organizationId;
	private Boolean isActive;
}