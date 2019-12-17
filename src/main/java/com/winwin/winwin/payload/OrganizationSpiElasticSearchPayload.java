/**
 * 
 */
package com.winwin.winwin.payload;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 *
 */
@Getter
@Setter
public class OrganizationSpiElasticSearchPayload {
	private String type;
	private Long id;
	private String dimension;
	private String component;
	private String indicator;
	private String adminUrl;
	private Boolean isChecked;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdByEmail;
	private String updatedByEmail;
}
