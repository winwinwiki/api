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
@Entity
@Table(name = "sdg_data")
public class SdgData {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sdg_data_seq")
	private Long id;

	@Column(name = "goal_code")
	private Long goalCode;

	@Column(name = "goal_name")
	private String goalName;

	@Column(name = "short_name_code")
	private String shortNameCode;

	@Column(name = "short_name")
	private String shortName;

	@Column(name = "is_active")
	private Boolean isActive;

}
