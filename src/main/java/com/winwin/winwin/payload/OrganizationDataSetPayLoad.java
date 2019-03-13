/**
 * 
 */
package com.winwin.winwin.payload;

import java.sql.Timestamp;

import com.winwin.winwin.entity.OrganizationDataSetCategory;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindK
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class OrganizationDataSetPayLoad {
	Long id;
	OrganizationDataSetCategory organizationDataSetCategory;
	Long organization_id;
	String description;
	String type;
	String url;
	Timestamp createdAt;
	Timestamp updatedAt;

}
