package com.winwin.winwin.payload;

import java.sql.Timestamp;

import com.winwin.winwin.entity.OrganizationResourceCategory;

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
public class OrganizationResourcePayLoad {
	Long id;
	OrganizationResourceCategory organizationResourceCategory;
	Long organization_id;
	String description;
	Long count;
	String type;
	String url;
	Timestamp createdAt;
	Timestamp updatedAt;

}
