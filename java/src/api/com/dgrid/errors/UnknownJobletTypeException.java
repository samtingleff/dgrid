package com.dgrid.errors;


public class UnknownJobletTypeException extends DGridException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5036149032204682495L;

	public UnknownJobletTypeException(Throwable root) {
		super(root);
	}

	public UnknownJobletTypeException(String message) {
		super(message);
	}

	public UnknownJobletTypeException() {
		super();
	}
}
