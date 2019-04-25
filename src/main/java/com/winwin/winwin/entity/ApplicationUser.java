package com.winwin.winwin.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 *
 */
public class ApplicationUser implements UserDetails {
	private static final long serialVersionUID = 1L;

	private UserPayload userPayload;

	public ApplicationUser() {
	}

	public ApplicationUser(UserPayload userPayload) {
		this.userPayload = userPayload;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();

		String role = this.userPayload.getRole();
		list.add(new SimpleGrantedAuthority(role));

		return list;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return this.userPayload.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

}
