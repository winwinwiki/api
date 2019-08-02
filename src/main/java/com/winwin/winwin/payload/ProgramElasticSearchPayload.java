/**
 * 
 */
package com.winwin.winwin.payload;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Getter
@Setter
public class ProgramElasticSearchPayload {
	private Long id;
	private String name;
	private String description;
	private String websiteUrl;
	private Boolean isActive;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdByEmail;
	private String updatedByEmail;
	private List<ProgramDataSetElasticSearchPayload> datasets;
	private List<ProgramResourceElasticSearchPayload> resources;
	private List<ProgramRegionServedElasticSearchPayload> regionServed;
	private List<ProgramSpiElasticSearchPayload> spi;
	private List<ProgramSdgElasticSearchPayload> sdg;
}
