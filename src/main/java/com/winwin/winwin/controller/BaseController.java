/**
 * 
 */
package com.winwin.winwin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.entity.ResponseWrapper;

/**
 * @author ArvindKhatik
 * 
 */
@Component
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

	@Autowired
	protected CustomMessageSource customMessageSource;

	protected ResponseEntity<ResponseWrapper<Object>> sendSuccessResponse(Object dto) {
		return new ResponseEntity<ResponseWrapper<Object>>(new ResponseWrapper<Object>(dto), HttpStatus.OK);
	}

	protected ResponseEntity<ResponseWrapper<Object>> sendSuccessResponse(Object dto, String successMsg) {
		LOGGER.info(customMessageSource.getMessage(successMsg));
		return new ResponseEntity<ResponseWrapper<Object>>(new ResponseWrapper<Object>(dto), HttpStatus.OK);
	}

	protected ResponseEntity<ResponseWrapper<Object>> sendSuccessResponse(Object dto, String successMsg,
			Object[] args) {
		LOGGER.info(customMessageSource.getMessage(successMsg, args));
		return new ResponseEntity<ResponseWrapper<Object>>(new ResponseWrapper<Object>(dto), HttpStatus.OK);
	}

	protected ResponseEntity<ResponseWrapper<Object>> sendSuccessResponse(Object dto, HttpStatus status) {
		return new ResponseEntity<ResponseWrapper<Object>>(new ResponseWrapper<Object>(dto), status);
	}

	protected ResponseEntity<ResponseWrapper<String>> sendSuccessResponse(String successMsg) {
		return sendSuccessResponse(successMsg, HttpStatus.OK);
	}

	protected ResponseEntity<ResponseWrapper<String>> sendSuccessResponse(String successMsg, HttpStatus httpStatus) {
		LOGGER.info(customMessageSource.getMessage(successMsg));
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(successMsg)), httpStatus);
	}

	protected ResponseEntity<ResponseWrapper<String>> sendErrorResponse(String responseMsg, HttpStatus httpStatus) {
		LOGGER.error(customMessageSource.getMessage(responseMsg));
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg)), httpStatus);
	}

	protected ResponseEntity<ResponseWrapper<String>> sendErrorResponse(String successMsg) {
		return sendSuccessResponse(successMsg, HttpStatus.BAD_REQUEST);
	}

	protected ResponseEntity<ResponseWrapper<String>> sendErrorResponse(String responseMsg, Object[] args,
			HttpStatus httpStatus) {
		LOGGER.error(customMessageSource.getMessage(responseMsg, args));
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg, args)), httpStatus);
	}

	protected ResponseEntity<ResponseWrapper<String>> sendExceptionResponse(Exception exception, String responseMsg,
			Object[] args, HttpStatus httpStatus) {
		LOGGER.error(customMessageSource.getMessage(responseMsg, args));
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg, args)), httpStatus);
	}

	protected ResponseEntity<ResponseWrapper<String>> sendExceptionResponse(Exception exception, String responseMsg,
			HttpStatus httpStatus) {
		LOGGER.error(customMessageSource.getMessage(responseMsg), exception);
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg)), httpStatus);
	}

	protected ResponseEntity<ResponseWrapper<String>> sendMsgResponse(Exception exception, String responseMsg,
			HttpStatus httpStatus) {
		LOGGER.error(responseMsg, exception);
		return new ResponseEntity<ResponseWrapper<String>>(new ResponseWrapper<String>(responseMsg), httpStatus);
	}

	public ResponseEntity<ResponseWrapper<String>> sendMsgResponse(String responseMsg, HttpStatus httpStatus) {
		return new ResponseEntity<ResponseWrapper<String>>(new ResponseWrapper<String>(responseMsg), httpStatus);
	}

	protected ResponseEntity<ResponseWrapper<String>> sendExceptionResponse(Exception exception, String responseMsg) {
		LOGGER.error(customMessageSource.getMessage(responseMsg), exception);
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg)),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
