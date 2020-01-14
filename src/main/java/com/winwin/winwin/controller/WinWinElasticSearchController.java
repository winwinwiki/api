package com.winwin.winwin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.service.UserService;
import com.winwin.winwin.service.WinWinElasticSearchService;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@RestController
@RequestMapping(value = "/winwin/es")
public class WinWinElasticSearchController extends BaseController {
	@Autowired
	private WinWinElasticSearchService winWinElasticSearchService;
	@Autowired
	private UserService userService;

	@GetMapping(path = "")
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> send() {
		UserPayload user = userService.getCurrentUserDetails();
		if (null != user)
			winWinElasticSearchService.sendPostRequestToElasticSearch(user);
		return sendSuccessResponse("org.success.created");
	}

}
