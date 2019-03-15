package com.winwin.winwin.exception;

/**
 * @author ArvindK
 *
 */
@SuppressWarnings("serial")
public class OrganizationResourceException extends Exception {
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
