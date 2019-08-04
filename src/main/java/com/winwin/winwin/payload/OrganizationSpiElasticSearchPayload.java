/**
 * 
 */
package com.winwin.winwin.payload;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindK
 *
 */
@Getter
@Setter
public class OrganizationSpiElasticSearchPayload {
	private Long id;
	private String name;
	private String adminUrl;
	private Boolean isChecked;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdByEmail;
	private String updatedByEmail;
}
