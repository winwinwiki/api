package com.winwin.winwin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionController {
	@ExceptionHandler(value = OrganizationException.class)
	public ResponseEntity<Object> exception(OrganizationException exception) {
	      return new ResponseEntity<>("Some exception Occured while creating org", HttpStatus.NOT_FOUND);
	   }
}
