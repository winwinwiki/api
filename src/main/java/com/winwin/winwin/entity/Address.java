package com.winwin.winwin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
@Table(name = "address")
@Entity
public class Address extends AbstractAuditableEntity {

	@Id
	@SequenceGenerator(name = "address_generator", sequenceName = "address_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_generator")
	private Long id;

	@Column(name = "country", nullable=false)
	private String country;

	@Column(name = "is_active")
	private Boolean isActive = true;

	@Column(name = "state")
	private String state;

	@Column(name = "city")
	private String city;

	@Column(name = "county")
	private String county;

	@Column(name = "zip")
	private String zip;

	@Column(name = "street", columnDefinition = "TEXT")
	private String street;

	@Column(name = "place_id")
	private String placeId;
}