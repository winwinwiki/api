package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProgramResourcePayLoad {
	Long id;
	ResourceCategoryPayLoad resourceCategory;
	Long organizationId;
	Long programId;
	String description;
	Long count;
	Boolean isActive;
	private String adminUrl;
}