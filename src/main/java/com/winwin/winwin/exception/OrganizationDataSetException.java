/**
 * 
 */
package com.winwin.winwin.exception;

/**
 * @author ArvindKhatik
 *
 */
public class OrganizationDataSetException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
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
