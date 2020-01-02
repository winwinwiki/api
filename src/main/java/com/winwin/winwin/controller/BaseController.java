/**
 * The Class BaseController is a Base Class for all the Controller Classes
 */
package com.winwin.winwin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.stereotype.Component;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.entity.ResponseWrapper;

/**
 * @author ArvindKhatik
 * @version 1.0
 * 
 */
@Component
public class BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

	@Autowired
	protected CustomMessageSource customMessageSource;

	private BodyBuilder buildSuccessBody() {
		return ResponseEntity.ok();

	}

	/**
	 * send success response with Object
	 * 
	 * @param dto
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<Object>> sendSuccessResponse(Object dto) {
		return buildSuccessBody().body(new ResponseWrapper<Object>(dto));
	}

	/**
	 * send success response with custom responseMsg
	 * 
	 * @param successMsg
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendSuccessResponse(String successMsg) {
		return buildSuccessBody().body(new ResponseWrapper<String>(customMessageSource.getMessage(successMsg)));
	}

	/**
	 * send success response with custom responseMsg and HttpStatus
	 * 
	 * @param successMsg
	 * @param httpStatus
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendSuccessResponse(String successMsg, HttpStatus httpStatus) {
		LOGGER.info(customMessageSource.getMessage(successMsg));
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(successMsg)), httpStatus);
	}

	/**
	 * send error response with custom responseMsg and HttpStatus
	 * 
	 * @param responseMsg
	 * @param httpStatus
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendErrorResponse(String responseMsg, HttpStatus httpStatus) {
		LOGGER.error(customMessageSource.getMessage(responseMsg));
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg)), httpStatus);
	}

	/**
	 * send error response with custom responseMsg
	 * 
	 * @param successMsg
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendErrorResponse(String successMsg) {
		return sendSuccessResponse(successMsg, HttpStatus.BAD_REQUEST);
	}

	/**
	 * send error response with custom responseMsg, HttpStatus and Array of
	 * Object[]
	 * 
	 * @param responseMsg
	 * @param args
	 * @param httpStatus
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendErrorResponse(String responseMsg, Object[] args,
			HttpStatus httpStatus) {
		LOGGER.error(customMessageSource.getMessage(responseMsg, args));
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg, args)), httpStatus);
	}

	/**
	 * send exception response with custom responseMsg, HttpStatus, Exception
	 * and Array of Object[]
	 * 
	 * @param exception
	 * @param responseMsg
	 * @param args
	 * @param httpStatus
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendExceptionResponse(Exception exception, String responseMsg,
			Object[] args, HttpStatus httpStatus) {
		LOGGER.error(customMessageSource.getMessage(responseMsg, args));
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg, args)), httpStatus);
	}

	/**
	 * send exception response with custom responseMsg, HttpStatus and Exception
	 * 
	 * @param exception
	 * @param responseMsg
	 * @param httpStatus
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendExceptionResponse(Exception exception, String responseMsg,
			HttpStatus httpStatus) {
		LOGGER.error(customMessageSource.getMessage(responseMsg), exception);
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg)), httpStatus);
	}

	/**
	 * send message response with custom responseMsg, Exception and HttpStatus
	 * 
	 * @param exception
	 * @param responseMsg
	 * @param httpStatus
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendMsgResponse(Exception exception, String responseMsg,
			HttpStatus httpStatus) {
		LOGGER.error(responseMsg, exception);
		return new ResponseEntity<ResponseWrapper<String>>(new ResponseWrapper<String>(responseMsg), httpStatus);
	}

	/**
	 * send message response with custom responseMsg and HttpStatus
	 * 
	 * @param responseMsg
	 * @param httpStatus
	 * @return
	 */
	public ResponseEntity<ResponseWrapper<String>> sendMsgResponse(String responseMsg, HttpStatus httpStatus) {
		return new ResponseEntity<ResponseWrapper<String>>(new ResponseWrapper<String>(responseMsg), httpStatus);
	}

	/**
	 * send exception response with custom responseMsg and Exception
	 * 
	 * @param exception
	 * @param responseMsg
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendExceptionResponse(Exception exception, String responseMsg) {
		LOGGER.error(customMessageSource.getMessage(responseMsg), exception);
		return new ResponseEntity<ResponseWrapper<String>>(
				new ResponseWrapper<String>(customMessageSource.getMessage(responseMsg)),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
