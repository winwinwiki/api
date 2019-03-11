package com.winwin.winwin.entity;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@MappedSuperclass
@EntityListeners({ AuditingEntityListener.class })
public class AbstractAuditableEntity extends AbstractPersistable<Long>{
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
}
