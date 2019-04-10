package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.Address;

/**
 * @author ArvindKhatik
 *
 */
public interface AddressRepository extends JpaRepository<Address, Long> {
	@Query(value = "select * from address	 where id = :id", nativeQuery = true)
	Address findAddressById(@Param("id") Long id);
}
