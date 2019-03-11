package com.winwin.winwin.payload;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class OrganizationPayload {
	Long id;
	AddressPayload address;
	String name;
	Long revenue;
	Long assets;
	String sector;
	String sectorLevel;
	String description;
	String status;
	String priority;
	Timestamp createdAt;
	Timestamp updatedAt;	
}
