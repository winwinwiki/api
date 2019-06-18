package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindKhatik
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class OrganizationSdgDataMapPayload {
	Long id;
	Long organizationId;
	Long goalCode;
	String goalName;
	String subGoalCode;
	String subGoalName;
	Boolean isChecked;
}
