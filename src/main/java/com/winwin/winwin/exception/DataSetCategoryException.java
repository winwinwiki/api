package com.winwin.winwin.exception;

/**
 * @author ArvindKhatik
 *
 */
public class DataSetCategoryException extends Exception {

	private static final long serialVersionUID = 1L;
	private String message = null;

	public DataSetCategoryException() {
		super();
	}

	public DataSetCategoryException(String message) {
		super(message);
		this.message = message;
	}

	public DataSetCategoryException(Throwable cause) {
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
