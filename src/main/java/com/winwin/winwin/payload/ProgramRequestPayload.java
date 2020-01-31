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
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class ProgramRequestPayload {
	private Long id;
	private String name;
	private String description;
	private String websiteUrl;
	private Long organizationId;
	private Boolean isActive = true;
	private Long parentId;
	private String datasetType;
	private String spiTagIds;
	private String sdgTagIds;
	private String resourceIds;
	private String datasetIds;
}
