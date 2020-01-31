package com.winwin.winwin.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class OrganizationChartPayload {
	private Long id;
	private Long parentId;
	private Long rootParentId;
	private String name;
	private String parentName;
	private String rootParentName;
	private AddressPayload location;
	private List<OrganizationChartPayload> children;
}
