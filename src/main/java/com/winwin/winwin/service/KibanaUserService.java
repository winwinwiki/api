package com.winwin.winwin.service;

import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.KibanaUserResponsePayload;
import com.winwin.winwin.payload.UserSignInPayload;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
public interface KibanaUserService {

	public void createInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response) throws Exception;

	public void deleteInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response) throws Exception;

	public void changePasswordForInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response)
			throws Exception;

	public KibanaUserResponsePayload getInternalKibanaUserDetails(String userName) throws Exception;

	public String getKibanaUserCognitoRole(String userName) throws Exception;

}