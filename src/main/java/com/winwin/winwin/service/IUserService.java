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
	
	public List<UserPayload> getUserList() throws UserException;
	
	public String getUserStatus(String userName) throws UserException;

	public AuthenticationResultType userSignIn(UserSignInPayload payload) throws UserException;

}
