package com.winwin.winwin.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationFilterPayload {
	private List<String> sectorLevel;
	private List<String> tagStatus;
	private String priority;
	private String editedBy;
	private Long nteeCode;
	private Long naicsCode;
	private float revenueMin = 0.0f;
	private float revenueMax = Float.MAX_VALUE;
	private float assestsMin = 0.0f;
	private float assestsMax = Float.MAX_VALUE;
	private String frameworkTag;
	private String indicatorId;
	private String dimensionId;
	private String componentId;
	private String nameSearch;
	private String shortNameCode;
	private String goalCode;
}
