/**
 * The Class BaseController is a Base Class for all the Controller Classes
 */
package com.winwin.winwin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	/**
	 * send success response with Object
	 * 
	 * @param dto
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<Object>> sendSuccessResponse(Object dto) {
		return new ResponseEntity<ResponseWrapper<Object>>(new ResponseWrapper<Object>(dto), HttpStatus.OK);
	}

	/**
	 * send success response with custom successMsg and Object
	 * 
	 * @param dto
	 * @param successMsg
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<Object>> sendSuccessResponse(Object dto, String successMsg) {
		LOGGER.info(customMessageSource.getMessage(successMsg));
		return new ResponseEntity<ResponseWrapper<Object>>(new ResponseWrapper<Object>(dto), HttpStatus.OK);
	}

	/**
	 * send success response with custom successMsg and Array of Object[]
	 * 
	 * @param dto
	 * @param successMsg
	 * @param args
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<Object>> sendSuccessResponse(Object dto, String successMsg,
			Object[] args) {
		LOGGER.info(customMessageSource.getMessage(successMsg, args));
		return new ResponseEntity<ResponseWrapper<Object>>(new ResponseWrapper<Object>(dto), HttpStatus.OK);
	}

	/**
	 * send success response with HttpStatus and Object
	 * 
	 * @param dto
	 * @param status
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<Object>> sendSuccessResponse(Object dto, HttpStatus status) {
		return new ResponseEntity<ResponseWrapper<Object>>(new ResponseWrapper<Object>(dto), status);
	}

	/**
	 * send success response with custom responseMsg
	 * 
	 * @param successMsg
	 * @return
	 */
	protected ResponseEntity<ResponseWrapper<String>> sendSuccessResponse(String successMsg) {
		return sendSuccessResponse(successMsg, HttpStatus.OK);
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
