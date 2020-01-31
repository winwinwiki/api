/**
 * 
 */
package com.winwin.winwin.payload;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Getter
@Setter
public class OrganizationResourceElasticSearchPayload {
	private Long id;
	private String name;
	private Long count;
	private String description;
	private String adminUrl;
	private Boolean isActive;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdByEmail;
	private String updatedByEmail;

}
