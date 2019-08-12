package com.winwin.winwin.service;

import java.util.List;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.UserException;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.payload.UserSignInPayload;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
public interface UserService {
	public void createUser(UserPayload payload, ExceptionResponse response) throws UserException;

	public void createKibanaUser(UserPayload payload, ExceptionResponse response) throws UserException;

	public UserPayload getUserInfo(String userName, ExceptionResponse response);

	void updateUserInfo(UserPayload payload, ExceptionResponse response);

	public List<UserPayload> getUserList(ExceptionResponse response);

	public String getUserStatus(String userName, ExceptionResponse response);

	public void resetUserPassword(UserPayload payload, ExceptionResponse response);

	public void resendConfirmationCode(UserPayload payload, ExceptionResponse response);

	void confirmResetPassword(UserSignInPayload payload, ExceptionResponse response);

	public void changePassword(UserSignInPayload payload, ExceptionResponse response);

	public AuthenticationResultType userSignIn(UserSignInPayload payload, ExceptionResponse response);

	void deleteUser(UserPayload payload, ExceptionResponse response);

	public UserPayload getLoggedInUser(String accessToken, ExceptionResponse response);

	UserPayload getCurrentUserDetails();

	void resendUserInvitation(UserPayload payload, ExceptionResponse response) throws UserException;

}
