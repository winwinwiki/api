package com.winwin.winwin.payload;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationFilterPayload {
	private List<String> sectors;
	private List<String> sectorLevel;
	private List<String> tagStatus;
	private String priority;
	public List<String> createdBy;
	public List<String> editedBy;
	private Long nteeCode;
	private Long naicsCode;
	private BigDecimal revenueMin;
	private BigDecimal revenueMax;
	private BigDecimal assetsMin;
	private BigDecimal assetsMax;
	private String frameworkTag;
	private String indicatorId;
	private long dimensionId;
	private String componentId;
	public String nameSearch;
	private String shortNameCode;
	private long goalCode;
	public Integer pageNo;
	public Integer pageSize;
	private BigInteger orgCount;
	public String sortBy;
	public String sortOrder;
	private String address;
	private String country;
	private String state;
	private String city;
	private String county;
}
