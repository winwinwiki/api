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
public class ProgramSdgDataMapPayload {
	Long id;
	Long programId;
	Long goalCode;
	String goalName;
	String subGoalCode;
	String subGoalName;
	Boolean isChecked;
	Long organizationId;
}
