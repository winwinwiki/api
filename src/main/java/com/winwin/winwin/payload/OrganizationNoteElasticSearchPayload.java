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
public class OrganizationNoteElasticSearchPayload {
	private Long id;
	private String name;
	private String adminUrl;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdByEmail;
	private String updatedByEmail;

}
