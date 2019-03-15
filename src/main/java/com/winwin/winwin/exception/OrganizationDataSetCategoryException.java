package com.winwin.winwin.exception;

/**
 * @author ArvindK
 *
 */
@SuppressWarnings("serial")
public class OrganizationDataSetCategoryException extends Exception {
	private String message = null;

	public OrganizationDataSetCategoryException() {
		super();
	}

	public OrganizationDataSetCategoryException(String message) {
		super(message);
		this.message = message;
	}

	public OrganizationDataSetCategoryException(Throwable cause) {
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
