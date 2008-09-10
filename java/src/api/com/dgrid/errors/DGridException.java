package com.dgrid.errors;

public class DGridException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1106730946273363940L;

	public DGridException(Throwable root) {
		super(root);
	}

	public DGridException(String message) {
		super(message);
	}

	public DGridException() {
		super();
	}
}
