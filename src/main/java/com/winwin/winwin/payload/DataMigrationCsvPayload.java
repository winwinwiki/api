/**
 * 
 */

package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindKhatik
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DataMigrationCsvPayload {
	private Long id;
	private String name;
	private Long revenue;
	private Long assets;
	private String sector;
	private String sectorLevel;
	private String sectorLevelName;
	private String description;
	private String naicsCode;
	private String nteeCode;
	private String websiteUrl;
	private String facebookUrl;
	private String linkedinUrl;
	private String twitterUrl;
	private String instagramUrl;
	private String values;
	private String purpose;
	private String selfInterest;
	private String businessModel;
	private Long populationServed;
	private String missionStatement;
	private String country;
	private String state;
	private String city;
	private String county;
	private Long zip;
	private String street;
	private String resourceIds;
	private String datasetIds;
	private String datasetType;
	private String spiTagIds;
	private String sdgTagIds;
	private String notes;
	private String ein;
	private String tagStatus;
	private Long parentId;
	private Long addressId;
}