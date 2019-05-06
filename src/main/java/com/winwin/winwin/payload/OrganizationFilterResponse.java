package com.winwin.winwin.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationFilterResponse {

	private OrganizationFilterPayload filter;
	private List<OrganizationPayload> payload;
}
