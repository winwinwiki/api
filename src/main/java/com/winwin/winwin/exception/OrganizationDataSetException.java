/**
 * 
 */
package com.winwin.winwin.exception;

/**
 * @author ArvindK
 *
 */
@SuppressWarnings("serial")
public class OrganizationDataSetException extends Exception {
	private String message = null;

	public OrganizationDataSetException() {
		super();
	}

	public OrganizationDataSetException(String message) {
		super(message);
		this.message = message;
	}

	public OrganizationDataSetException(Throwable cause) {
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
