package com.winwin.winwin.payload;

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
public class ResourceElasticSearchPayload {
	private OrganizationResourceElasticSearchPayload resource;
	private Long id;
	private String name;
	private String description;
	private String type;
	private String values;
	private String websiteUrl;
	private String adminUrl;
	private Long parentId;
	private Long rootParentId;
	private String parentName;
	private String rootParentName;
	private String parentUrl;
	private String rootParentUrl;
	private List<String> connectedOrganizations;
	private AddressElasticSearchPayload address;

}
