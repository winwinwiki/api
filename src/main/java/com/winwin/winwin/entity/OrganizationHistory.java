package com.winwin.winwin.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

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
@Entity
@Table(name = "org_history")
public class OrganizationHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_history_seq")
	private Long id;

	@JoinColumn(name = "organization_id")
	private Long organizationId;

	@JoinColumn(name = "program_id")
	private Long programId;

	@LastModifiedDate
	@Column(name = "updated_at")
	protected Date updatedAt;

	@LastModifiedBy
	@Column(name = "updated_by")
	protected String updatedBy;

	@Column(name = "action_performed")
	private String actionPerformed;

	@Column(name = "entity_type")
	private String entityType;

	@Column(name = "entity_id")
	private Long entityId;

	@Column(name = "entity_name", columnDefinition = "TEXT")
	private String entityName;

	@Column(name = "entity_code")
	private String entityCode;
}
