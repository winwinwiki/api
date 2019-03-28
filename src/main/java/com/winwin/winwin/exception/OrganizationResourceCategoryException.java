package com.winwin.winwin.exception;

/**
 * @author ArvindKhatik
 *
 */
@SuppressWarnings("serial")
public class OrganizationResourceCategoryException extends Exception {
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

