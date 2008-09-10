package com.dgrid.errors;

public class MailException extends DGridException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1107025858152537437L;

	public MailException(String msg) {
		super(msg);
	}

	public MailException(Throwable root) {
		super(root);
	}
}
