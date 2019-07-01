package com.winwin.winwin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq")
	private Long id;

	@Column(name = "country")
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

	@Column(name = "street")
	private String street;

	@Column(name = "place_id")
	private String placeId;
}