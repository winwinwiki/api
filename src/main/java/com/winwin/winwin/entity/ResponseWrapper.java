package com.winwin.winwin.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author arvindK
 *
 */
public class ResponseWrapper<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private T response;

	@JsonCreator
	public ResponseWrapper(T obj) {
		this.response = obj;
	}

	public ResponseWrapper() {

	}

	public T getResponse() {
		return response;
	}

	public void setResponse(T response) {
		this.response = response;
	}
}
