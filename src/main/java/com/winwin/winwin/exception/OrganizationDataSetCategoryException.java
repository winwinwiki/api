package com.winwin.winwin.exception;

/**
 * @author ArvindKhatik
 *
 */
public class OrganizationDataSetCategoryException extends Exception {

	private static final long serialVersionUID = 1L;
	
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
