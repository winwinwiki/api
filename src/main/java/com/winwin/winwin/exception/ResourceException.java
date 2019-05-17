package com.winwin.winwin.exception;

/**
 * @author ArvindKhatik
 *
 */

public class ResourceException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private String message = null;

	public ResourceException() {
		super();
	}

	public ResourceException(String message) {
		super(message);
		this.message = message;
	}

	public ResourceException(Throwable cause) {
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
