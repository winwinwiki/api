package com.winwin.winwin.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.exception.UserException;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.payload.UserSignInPayload;
import com.winwin.winwin.repository.UserRepository;
import com.winwin.winwin.service.UserService;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 *
 */
@RestController
@RequestMapping(value = "/user")
public class UserController extends BaseController {

	@Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@RequestMapping(value = "", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> createUser(@Valid @RequestBody UserPayload payload) throws UserException {
		try {
			if (null != payload) {
				userService.createUser(payload);
			}
		} catch (UserException e) {
			return sendErrorResponse("org.user.bad_request", HttpStatus.BAD_REQUEST);
		}

		return sendSuccessResponse("org.user.success.created", HttpStatus.CREATED);

	}

	@RequestMapping(value = "login", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> userSignInRequest(@Valid @RequestBody UserSignInPayload payload) throws UserException {
		AuthenticationResultType authenticationResult = null;
		UserSignInPayload userSignInPayload = null;
		try {
			if (null != payload) {
				if (!StringUtils.isEmpty(payload.getUserName())) {

					try {
						if (isNewUser(payload.getUserName())) {
							userSignInPayload = new UserSignInPayload();
							userSignInPayload.setUserName(payload.getUserName());
							userSignInPayload.setIsNewUser(true);
							return sendSuccessResponse(userSignInPayload, HttpStatus.OK);
						}
					} catch (Exception e) {
						return sendErrorResponse("org.user.error.not_found", HttpStatus.NOT_FOUND);
					}
					authenticationResult = userService.userSignIn(payload);

				} else {
					return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
				}

			} else {
				return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			return sendErrorResponse("org.user.error.signIn", HttpStatus.UNAUTHORIZED);
		}

		return sendSuccessResponse(authenticationResult, HttpStatus.OK);

	}

	@RequestMapping(value = "info", method = RequestMethod.POST)
	public ResponseEntity<?> getUserInfo(@Valid @RequestBody UserPayload userPayload) {
		UserPayload payload = null;
		try {
			if (null != userPayload) {
				if (!StringUtils.isEmpty(userPayload.getEmail())) {
					payload = userService.getUserInfo(userPayload.getEmail());

				} else {
					return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
				}

			} else {
				return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			return sendErrorResponse("org.user.error.not_found", HttpStatus.NOT_FOUND);
		}
		return sendSuccessResponse(payload, HttpStatus.OK);

	}

	public Boolean isNewUser(String userName) throws UserException {
		try {
			String userStatus = userService.getUserStatus(userName);
			if (!userStatus.isEmpty()) {
				if (OrganizationConstants.AWS_USER_STATUS_FORCE_CHANGE_PASSWORD.equals(userStatus)) {
					return true;
				}
			} else {
				throw new UserException(customMessageSource.getMessage("org.user.error.not_found"));
			}

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.error.signIn"), e);
		}
		return false;

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> getUserList() {
		List<UserPayload> payloadList = null;
		try {
			payloadList = userService.getUserList();

			if (payloadList == null) {
				return sendErrorResponse("org.user.error.not_found", HttpStatus.NOT_FOUND);

			}

		} catch (Exception e) {
			return sendErrorResponse("org.user.exception.info", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return sendSuccessResponse(payloadList, HttpStatus.OK);

	}

}
