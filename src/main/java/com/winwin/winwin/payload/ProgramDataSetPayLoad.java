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
public class ProgramDataSetPayLoad {
	Long id;
	Long organizationId;
	DataSetCategoryPayload dataSetCategory;
	Long programId;
	String description;
	String type;
	String url;
	Boolean isActive;
}
