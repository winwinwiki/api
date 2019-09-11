package com.winwin.winwin.service;

import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.UserSignInPayload;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
public interface KibanaUserService {

	public void createInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response) throws Exception;

	public void deleteInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response) throws Exception;

	public String getInternalKibanaUserRole(String userName) throws Exception;

}