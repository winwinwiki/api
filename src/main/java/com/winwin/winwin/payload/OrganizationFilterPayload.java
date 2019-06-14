package com.winwin.winwin.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationFilterPayload {
	private List<String> sectors;
	private List<String> sectorLevel;
	private List<String> tagStatus;
	private String priority;
	private List<String> editedBy;
	private Long nteeCode;
	private Long naicsCode;
	private long revenueMin = 0;
	private long revenueMax = Long.MAX_VALUE;
	private long assetsMin = 0;
	private long assetsMax = Long.MAX_VALUE;
	private String frameworkTag;
	private String indicatorId;
	private long dimensionId;
	private String componentId;
	private String nameSearch;
	private String shortNameCode;
	private long goalCode;
	private Integer pageNo;
	private Integer pageSize;
	private Integer orgCount;
	private String sortBy;
	private String sortOrder;
	private String address;
}
