/**
 * 
 */
package com.winwin.winwin.exception;

/**
 * @author ArvindKhatik
 *
 */
public class DataSetException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private String message = null;

	public DataSetException() {
		super();
	}

	public DataSetException(String message) {
		super(message);
		this.message = message;
	}

	public DataSetException(Throwable cause) {
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
