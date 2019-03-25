package com.winwin.winwin.payload;

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
	OrganizationResourceCategoryPayLoad organizationResourceCategory;
	Long organizationId;
	String description;
	Long count;
	Boolean isActive;
}
