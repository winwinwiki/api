package com.winwin.winwin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.winwin.winwin.filter.TokenFilter;

/**
 * @author ArvindKhatik
 *
 */
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	TokenFilter tokenFilter;

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.addFilterAfter(tokenFilter, BasicAuthenticationFilter.class).authorizeRequests().anyRequest()
				.permitAll().and().httpBasic();
		httpSecurity.csrf().disable();
	}

	@Override
	public UserDetailsService userDetailsService() {

		return new InMemoryUserDetailsManager();
	}

}
