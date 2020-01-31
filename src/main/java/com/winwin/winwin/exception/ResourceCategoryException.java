package com.winwin.winwin.exception;

/**
 * @author ArvindKhatik
 *
 */

public class ResourceCategoryException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message = null;

	public ResourceCategoryException() {
		super();
	}

	public ResourceCategoryException(String message) {
		super(message);
		this.message = message;
	}

	public ResourceCategoryException(Throwable cause) {
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

