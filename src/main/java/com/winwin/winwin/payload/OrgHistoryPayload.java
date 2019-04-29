package com.winwin.winwin.payload;

import java.util.Date;

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
public class OrgHistoryPayload {
	Long id;
	String entityType;
	String entityName;
	String parentEntityName;
	String actionPerformed;
	String modifiedBy;
	Date modifiedAt;

}