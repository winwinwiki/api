package com.winwin.winwin.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@MappedSuperclass
@EntityListeners({ AuditingEntityListener.class })
public class AbstractAuditableEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@CreatedDate
	@Column(name = "created_at")
	@JoinColumn(name = "created_at")
	protected Date createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	@JoinColumn(name = "updated_at")
	protected Date updatedAt;

	@CreatedBy
	@Column(name = "created_by")
	@JoinColumn(name = "created_by")
	protected String createdBy;

	@LastModifiedBy
	@Column(name = "updated_by")
	@JoinColumn(name = "updated_by")
	protected String updatedBy;

	@Column(name = "created_by_email")
	protected String createdByEmail;

	@Column(name = "updated_by_email")
	protected String updatedByEmail;
}
