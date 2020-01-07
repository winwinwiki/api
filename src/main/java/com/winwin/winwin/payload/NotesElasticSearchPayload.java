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
public class NotesElasticSearchPayload {
	private OrganizationNoteElasticSearchPayload notes;
	private Long id;
	private String programOrOrgName;
	private String programOrOrgDescription;
	private String programOrOrgType;
	private String values;
	private String websiteUrl;
	private String adminUrl;
	private Long parentOrgId;
	private Long topParentOrgId;
	private String parentOrgName;
	private String topParentOrgName;
	private String parentOrgUrl;
	private String topParentOrgUrl;
	private String parentOrgDescription;
	private List<String> connectedOrganizations;
	private AddressElasticSearchPayload address;

}
