package com.winwin.winwin.service;

import java.util.List;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.winwin.winwin.exception.UserException;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.payload.UserSignInPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface IUserService {
	public void createUser(UserPayload payload) throws UserException;

	public UserPayload getUserInfo(String userName) throws UserException;

	void updateUserInfo(UserPayload payload) throws UserException;

	public List<UserPayload> getUserList() throws UserException;

	public String getUserStatus(String userName) throws UserException;

	public void resetUserPassword(UserPayload payload) throws UserException;

	public void resendConfirmationCode(UserPayload payload) throws UserException;

	void confirmResetPassword(UserSignInPayload payload) throws UserException;

	public void changePassword(UserSignInPayload payload) throws UserException;

	public AuthenticationResultType userSignIn(UserSignInPayload payload) throws UserException;

	void deleteUser(UserPayload payload) throws UserException;

}
