package com.winwin.winwin.exception;

/**
 * @author ArvindKhatik
 *
 */

public class OrganizationResourceCategoryException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message = null;

	public OrganizationResourceCategoryException() {
		super();
	}

	public OrganizationResourceCategoryException(String message) {
		super(message);
		this.message = message;
	}

	public OrganizationResourceCategoryException(Throwable cause) {
		super(cause);
	}

	@Override
	public String toString() {
		return message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}

