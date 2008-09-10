package com.dgrid.errors;


public class TransportException extends DGridException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2681369008571050070L;

	public TransportException(String msg) {
		super(msg);
	}

	public TransportException(Throwable root) {
		super(root);
	}
}
