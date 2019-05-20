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
public class OrganizationNotePayload {
	Long noteId;
	String note;
	Long organizationId;
	String createdBy;
	Date createdAt;
	private String adminUrl;
}
