package com.winwin.winwin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionController {
	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<Object> handleAllExceptions(OrganizationException exception) {
		return new ResponseEntity<>("Some exception Occured while creating org", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(OrganizationException.class)
	public final ResponseEntity<Object> handleUserNotFoundException(OrganizationException ex) {
		return new ResponseEntity<>("operation is not performed because the id is not found", HttpStatus.NOT_FOUND);
	}
}
