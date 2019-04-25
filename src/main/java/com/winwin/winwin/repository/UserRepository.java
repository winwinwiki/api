package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.winwin.winwin.entity.User;

/**
 * @author ArvindKhatik
 *
 */
public interface UserRepository extends JpaRepository<User, Long> {
	@Query(value = "select * from org_users where aws_user_email = :email", nativeQuery = true)
	User findUserByEmail(@Param("email")String email);
}
