package com.winwin.winwin.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.controller.BaseController;
import com.winwin.winwin.entity.ApplicationUser;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.service.UserService;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class TokenFilter extends HttpFilter {

	private static final long serialVersionUID = 1L;
	@Autowired
	BaseController baseController;

	@Autowired
	UserService userService;

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterchain)
			throws IOException, ServletException {
		if (request.getRequestURI().equals("/user/login")) {
			filterchain.doFilter(request, response);
			return;
		}
		ExceptionResponse exceptionRes = new ExceptionResponse();
		String accessToken = request.getHeader(OrganizationConstants.USER_AUTH_ID);
		UserPayload user = null;

		if (!StringUtils.isEmpty(accessToken)) {
			user = userService.getLoggedInUser(accessToken, exceptionRes);

			if (!(StringUtils.isEmpty(exceptionRes.getErrorMessage())) && exceptionRes.getStatusCode() != null)
				response.sendError(400, exceptionRes.getErrorMessage());

			if (null != user) {
				UserDetails userDetails = new ApplicationUser(user);
				UsernamePasswordAuthenticationToken userAuthToken = new UsernamePasswordAuthenticationToken(
						user.getEmail(), "", userDetails.getAuthorities());
				userAuthToken.setDetails(user);
				SecurityContextHolder.getContext().setAuthentication(userAuthToken);
				filterchain.doFilter(request, response);
			}

		}else{
			response.sendError(400, "Token Found as null");
		}
	}

	@Override
	public void init(FilterConfig filterconfig) throws ServletException {
	}
}