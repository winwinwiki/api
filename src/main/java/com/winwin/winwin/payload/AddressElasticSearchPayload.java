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
public class AddressElasticSearchPayload {
	private Long id;
	private String city;
	private String country;
	private String county;
	private Boolean isActive;
	private String state;
	private String street;
	private String zip;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdByEmail;
	private String updatedByEmail;

}
