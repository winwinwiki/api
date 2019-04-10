package com.winwin.winwin.exception;

/**
 * @author ArvindKhatik
 *
 */

public class OrganizationResourceException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private String message = null;

	public OrganizationResourceException() {
		super();
	}

	public OrganizationResourceException(String message) {
		super(message);
		this.message = message;
	}

	public OrganizationResourceException(Throwable cause) {
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
