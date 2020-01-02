package com.winwin.winwin.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
import org.springframework.stereotype.Service;

import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.ApplicationUser;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.service.impl.UserServiceImpl;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Service
public class TokenFilter extends HttpFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Autowired
	private UserServiceImpl userService;

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterchain)
			throws IOException, ServletException {
		final Set<String> requestEndpoint = new HashSet<String>(Arrays.asList("/user/login", "/user/resetPassword",
				"/user/confirmResetPassword", "/user/resendCode", "/user/createKibanaUser", "/user/actuator/health"));

		if (requestEndpoint.contains(request.getRequestURI())) {
			filterchain.doFilter(request, response);
			return;
		}

		ExceptionResponse exceptionRes = new ExceptionResponse();
		String accessToken = request.getHeader(OrganizationConstants.USER_AUTH_ID);
		UserPayload user = null;

		if (!StringUtils.isEmpty(accessToken)) {
			user = userService.getLoggedInUser(accessToken, exceptionRes);

			if (!(StringUtils.isEmpty(exceptionRes.getErrorMessage())) && exceptionRes.getStatusCode() != null)
				response.sendError(401, "Unauthorized");

			if (null != user) {
				UserDetails userDetails = new ApplicationUser(user);
				UsernamePasswordAuthenticationToken userAuthToken = new UsernamePasswordAuthenticationToken(
						user.getEmail(), "", userDetails.getAuthorities());
				userAuthToken.setDetails(user);
				SecurityContextHolder.getContext().setAuthentication(userAuthToken);
				filterchain.doFilter(request, response);
			}

		} else {
			response.sendError(401, "Unauthorized");
		}
	}

	@Override
	public void init(FilterConfig filterconfig) throws ServletException {
	}
}