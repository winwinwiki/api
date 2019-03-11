package com.winwin.winwin.payload;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class AddressPayload {
	private String country;
	private String state;
	private String city;
	private String county;
	private Long zip;
	private String street;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	
}
