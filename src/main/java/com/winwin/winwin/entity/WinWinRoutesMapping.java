/**
 * 
 */
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
 * @version 1.0
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Table(name = "winwin_routes_mapping")
@Entity
public class WinWinRoutesMapping {
	@Id
	@SequenceGenerator(name = "route_generator", sequenceName = "winwin_routes_mapping_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "route_generator")
	private Long id;

	@Column(name = "key")
	private String key;

	@Column(name = "value", columnDefinition = "TEXT")
	private String value;

	@Column(name = "is_active")
	private Boolean isActive = true;

}
