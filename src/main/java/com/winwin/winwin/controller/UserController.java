package com.winwin.winwin.controller;

import java.util.List;

import javax.validation.Valid;

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
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.UserException;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.payload.UserSignInPayload;
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
	protected CustomMessageSource customMessageSource;

	@RequestMapping(value = "", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> createUser(@Valid @RequestBody UserPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (null != payload) {
			userService.createUser(payload, exceptionResponse);

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
		}

		return sendSuccessResponse("org.user.success.created", HttpStatus.CREATED);

	}

	@RequestMapping(value = "login", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> userSignInRequest(@Valid @RequestBody UserSignInPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		AuthenticationResultType authenticationResult = null;
		UserSignInPayload userSignInPayload = null;
		try {
			if (null != payload) {
				if (!StringUtils.isEmpty(payload.getUserName())) {
					if (isNewUser(payload.getUserName(), exceptionResponse) && payload.getNewPassword() == null) {
						userSignInPayload = new UserSignInPayload();
						userSignInPayload.setUserName(payload.getUserName());
						userSignInPayload.setIsNewUser(true);

						if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
								&& exceptionResponse.getStatusCode() != null)
							return sendMsgResponse(exceptionResponse.getErrorMessage(),
									exceptionResponse.getStatusCode());

						return sendSuccessResponse(userSignInPayload, HttpStatus.OK);
					}
					authenticationResult = userService.userSignIn(payload, exceptionResponse);

					if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
							&& exceptionResponse.getStatusCode() != null)
						return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

				} else {
					return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
				}

			} else {
				return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			// return sendExceptionResponse(e, "org.user.error.signIn");
			throw new UserException(e);
		}

		return sendSuccessResponse(authenticationResult, HttpStatus.OK);

	}

	@RequestMapping(value = "info", method = RequestMethod.POST)
	public ResponseEntity<?> getUserInfo(@Valid @RequestBody UserPayload userPayload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		UserPayload payload = null;

		if (null != userPayload) {
			if (!StringUtils.isEmpty(userPayload.getEmail())) {
				payload = userService.getUserInfo(userPayload.getEmail(), exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			} else {
				return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
			}

		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}

		return sendSuccessResponse(payload, HttpStatus.OK);

	}

	@RequestMapping(value = "updateInfo", method = RequestMethod.POST)
	public ResponseEntity<?> updateUserInfo(@Valid @RequestBody UserPayload userPayload) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		UserPayload payload = null;

		if (null != userPayload) {
			if (!StringUtils.isEmpty(userPayload.getEmail())) {
				userService.updateUserInfo(userPayload, exceptionResponse);
				payload = userService.getUserInfo(userPayload.getEmail(), exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			} else {
				return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
			}

		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}

		return sendSuccessResponse(payload, HttpStatus.OK);

	}

	public Boolean isNewUser(String userName, ExceptionResponse exceptionResponse) throws UserException {
		String userStatus = userService.getUserStatus(userName, exceptionResponse);
		if (!userStatus.isEmpty()) {
			if (OrganizationConstants.AWS_USER_STATUS_FORCE_CHANGE_PASSWORD.equals(userStatus)) {
				return true;
			}
		} else {
			throw new UserException(customMessageSource.getMessage("org.user.error.not_found"));
		}

		return false;

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> getUserList() {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		List<UserPayload> payloadList = null;

		payloadList = userService.getUserList(exceptionResponse);

		if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage())) && exceptionResponse.getStatusCode() != null)
			return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

		return sendSuccessResponse(payloadList, HttpStatus.OK);

	}

	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> resetPassword(@Valid @RequestBody UserPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != payload) {
			if (!StringUtils.isEmpty(payload.getEmail())) {
				userService.resetUserPassword(payload, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			} else {
				return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
			}
		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}

		return sendSuccessResponse("org.user.password.success.reset", HttpStatus.OK);

	}

	@RequestMapping(value = "confirmResetPassword", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> confirmResetPassword(@Valid @RequestBody UserSignInPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != payload) {
			if ((!StringUtils.isEmpty(payload.getConfirmationCode()))
					&& (!StringUtils.isEmpty(payload.getNewPassword()))
					&& (!StringUtils.isEmpty(payload.getUserName()))) {
				userService.confirmResetPassword(payload, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			} else {
				return sendErrorResponse("org.user.bad_request", HttpStatus.BAD_REQUEST);
			}
		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}

		return sendSuccessResponse("org.user.password.success.reset", HttpStatus.OK);

	}

	@RequestMapping(value = "resendCode", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> resendConfirmationCode(@Valid @RequestBody UserPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != payload) {
			if (!StringUtils.isEmpty(payload.getEmail())) {
				userService.resendConfirmationCode(payload, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			} else {
				return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
			}
		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}

		return sendSuccessResponse("org.user.code.success", HttpStatus.OK);

	}

	@RequestMapping(value = "changePassword", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> changeUserPassword(@Valid @RequestBody UserSignInPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != payload) {
			if ((!StringUtils.isEmpty(payload.getAccessToken())) && (!StringUtils.isEmpty(payload.getPassword()))
					&& (!StringUtils.isEmpty(payload.getNewPassword()))) {
				userService.changePassword(payload, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			} else {
				return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
			}
		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}

		return sendSuccessResponse("org.user.password.success.changed", HttpStatus.OK);

	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@Transactional
	public ResponseEntity<?> deleteUser(@Valid @RequestBody UserPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		if (null != payload) {
			if (!StringUtils.isEmpty(payload.getEmail())) {
				userService.deleteUser(payload, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

			} else {
				return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
			}
		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}

		return sendSuccessResponse("org.user.delete.success", HttpStatus.OK);

	}

}
