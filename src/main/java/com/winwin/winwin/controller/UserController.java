package com.winwin.winwin.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.UserException;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.payload.UserSignInPayload;
import com.winwin.winwin.payload.UserSignInResponsePayload;
import com.winwin.winwin.payload.UserStatusPayload;
import com.winwin.winwin.service.impl.UserServiceImpl;
import com.winwin.winwin.util.UserComparator;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@RestController
@RequestMapping(value = "/user")
public class UserController extends BaseController {

	@Autowired
	UserServiceImpl userService;

	@Autowired
	protected CustomMessageSource customMessageSource;

	@RequestMapping(value = "", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> createUser(@Valid @RequestBody UserPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (null != payload) {
			// Check user with status as FORCE_CHANGE_PASSWORD
			if (isNewUser(payload.getEmail(), exceptionResponse)) {
				userService.resendUserInvitation(payload, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
				return sendSuccessResponse("org.user.success.resend_invitation");
			} else {
				if (exceptionResponse.getException() != null
						&& exceptionResponse.getException() instanceof UserNotFoundException) {
					userService.createUser(payload, exceptionResponse);
					return sendSuccessResponse("org.user.success.created", HttpStatus.CREATED);
				} else if (exceptionResponse.getException() != null
						&& !(exceptionResponse.getException() instanceof UserNotFoundException)) {
					return sendMsgResponse(exceptionResponse.getException().getMessage(),
							exceptionResponse.getStatusCode());
				}
			}
		}
		return sendSuccessResponse("org.user.success.created", HttpStatus.CREATED);
	}

	@RequestMapping(value = "createKibanaUser", method = RequestMethod.POST)
	public ResponseEntity<?> createKibanaUser(@Valid @RequestBody UserPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (null != payload) {
			// Check user with status as FORCE_CHANGE_PASSWORD
			if (isNewUser(payload.getEmail(), exceptionResponse)) {
				userService.resendUserInvitation(payload, exceptionResponse);

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
				return sendSuccessResponse("org.user.success.resend_invitation");
			} else {
				if (exceptionResponse.getException() != null
						&& exceptionResponse.getException() instanceof UserNotFoundException) {
					userService.createKibanaUser(payload, exceptionResponse);
					return sendSuccessResponse("org.user.success.created", HttpStatus.CREATED);
				} else if (exceptionResponse.getException() != null
						&& !(exceptionResponse.getException() instanceof UserNotFoundException)) {
					return sendMsgResponse(exceptionResponse.getException().getMessage(),
							exceptionResponse.getStatusCode());
				}
			}
		}
		return sendSuccessResponse("org.user.success.created", HttpStatus.CREATED);
	}

	@RequestMapping(value = "login", method = RequestMethod.POST)
	public ResponseEntity<?> userSignInRequest(@Valid @RequestBody UserSignInPayload payload) throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		AuthenticationResultType authenticationResult = null;
		UserSignInPayload userSignInPayload = null;
		UserSignInResponsePayload userSignInResPayload = null;

		if (null != payload) {
			if (!StringUtils.isEmpty(payload.getUserName())) {
				if (isNewUser(payload.getUserName(), exceptionResponse) && payload.getNewPassword() == null) {
					userSignInPayload = new UserSignInPayload();
					userSignInPayload.setUserName(payload.getUserName());
					userSignInPayload.setIsNewUser(true);

					if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
							&& exceptionResponse.getStatusCode() != null)
						return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

					return sendSuccessResponse(userSignInPayload);
				}

				if (exceptionResponse.getException() != null
						&& exceptionResponse.getException() instanceof UserNotFoundException) {
					return sendMsgResponse(exceptionResponse.getException().getMessage(),
							exceptionResponse.getStatusCode());
				}

				authenticationResult = userService.userSignIn(payload, exceptionResponse);

				if (null != authenticationResult) {
					if (!StringUtils.isEmpty(authenticationResult.getAccessToken())) {
						UserPayload userPayload = userService.getLoggedInUser(authenticationResult.getAccessToken(),
								exceptionResponse);
						userSignInResPayload = new UserSignInResponsePayload();
						userSignInResPayload.setAuthResult(authenticationResult);
						userSignInResPayload.setUserDetails(userPayload);
					}
				}

				if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
						&& exceptionResponse.getStatusCode() != null)
					return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
			} else {
				return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
			}
		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}
		return sendSuccessResponse(userSignInResPayload);
	}

	@RequestMapping(value = "info", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getUserInfo(UserPayload userPayload) throws UserException {
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
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "update", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
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
		return sendSuccessResponse(payload);
	}

	@RequestMapping(value = "updateAll", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> updateUsersInfo(@Valid @RequestBody List<UserPayload> userPayloadList) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		UserPayload payload = null;
		List<UserPayload> payloadList = new ArrayList<UserPayload>();

		if (null != userPayloadList) {
			for (UserPayload userPayload : userPayloadList) {
				if (!StringUtils.isEmpty(userPayload.getEmail())) {
					userService.updateUserInfo(userPayload, exceptionResponse);
					payload = userService.getUserInfo(userPayload.getEmail(), exceptionResponse);
					payloadList.add(payload);

					if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
							&& exceptionResponse.getStatusCode() != null)
						return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());
				} else {
					return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
				}
			}
		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}
		return sendSuccessResponse(payloadList);
	}

	public Boolean isNewUser(String userName, ExceptionResponse exceptionResponse) throws UserException {
		String userStatus = userService.getUserStatus(userName, exceptionResponse);
		if (!StringUtils.isEmpty(userStatus)) {
			if (OrganizationConstants.AWS_USER_STATUS_FORCE_CHANGE_PASSWORD.equals(userStatus)) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "')")
	public ResponseEntity<?> getUserList() {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		List<UserPayload> payloadList = new ArrayList<UserPayload>();

		payloadList = userService.getUserList(exceptionResponse);

		if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage())) && exceptionResponse.getStatusCode() != null)
			return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

		// Sort user list by name in ascending order
		if (!payloadList.isEmpty())
			Collections.sort(payloadList, new UserComparator());

		return sendSuccessResponse(payloadList);
	}

	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
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
		return sendSuccessResponse("org.user.password.success.reset");
	}

	@RequestMapping(value = "confirmResetPassword", method = RequestMethod.POST)
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
		return sendSuccessResponse("org.user.password.success.reset");
	}

	@RequestMapping(value = "resendCode", method = RequestMethod.POST)
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
		return sendSuccessResponse("org.user.code.success");
	}

	@RequestMapping(value = "changePassword", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "') or hasAuthority('" + UserConstants.ROLE_DATASEEDER
			+ "') or hasAuthority('" + UserConstants.ROLE_READER + "')")
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
		return sendSuccessResponse("org.user.password.success.changed");
	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
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
		return sendSuccessResponse("org.user.delete.success");
	}

	@RequestMapping(value = "changeUserStatus", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + UserConstants.ROLE_ADMIN + "')")
	public ResponseEntity<?> changeUserStatus(@Valid @RequestBody UserStatusPayload userStatuspayload)
			throws UserException {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		if (null != userStatuspayload && null != userStatuspayload.getUserNames()) {
			for (String userName : userStatuspayload.getUserNames()) {
				if (StringUtils.isEmpty(userName)) {
					return sendErrorResponse("org.user.error.name.null", HttpStatus.BAD_REQUEST);
				}
			}

			if (userStatuspayload.getIsActive()) {
				userService.enableUser(userStatuspayload.getUserNames(), exceptionResponse);
			} else {
				userService.disableUser(userStatuspayload.getUserNames(), exceptionResponse);
			}

			if (!(StringUtils.isEmpty(exceptionResponse.getErrorMessage()))
					&& exceptionResponse.getStatusCode() != null)
				return sendMsgResponse(exceptionResponse.getErrorMessage(), exceptionResponse.getStatusCode());

		} else {
			return sendErrorResponse("org.user.error.payload_null", HttpStatus.BAD_REQUEST);
		}
		return sendSuccessResponse("org.user.enable.success");
	}

	@RequestMapping(value = "/actuator/health", method = RequestMethod.GET)
	public ResponseEntity<?> getActuatorHealthStatus() {
		return sendSuccessResponse("actuator.health.status", HttpStatus.OK);
	}

}
