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
public class ProgramResponsePayload {
private Long id;
private String name;
private String description;
private Long organizationId;
private Boolean isActive = true;
private String tagStatus;
private String websiteUrl;
private String notes;
private Long parentId;
private String resourceIds;
private String datasetIds;
private String datasetType;
private String spiTagIds;
private String sdgTagIds;
}
