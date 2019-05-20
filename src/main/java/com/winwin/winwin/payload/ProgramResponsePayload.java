package com.winwin.winwin.payload;

import java.util.Date;

import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.entity.NteeData;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProgramResponsePayload {
	private Long id;

	AddressPayload address;

	private String name;

	private Long revenue;

	private Long assets;

	private String sector;

	private String sectorLevel;

	private String sectorLevelName;

	private String description;

	private NaicsData naicsCode;

	private NteeData nteeCode;

	private String priority = "Normal";

	private Long orgId;

	private Boolean isActive = true;

	private String tagStatus;

	private String websiteUrl;

	private String facebookUrl;

	private String linkedinUrl;

	private String twitterUrl;

	String values;

	String purpose;

	private String selfInterest;

	String businessModel;

	Long populationServed;

	String missionStatement;

	String contactInfo;
	String createdBy;
	String updatedBy;
	Date createdAt;
	Date updatedAt;
	private String adminUrl;
}
