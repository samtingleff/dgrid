package com.dgrid.errors;

public class SQSException extends AWSException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5930561613303228313L;

	public SQSException(Throwable root) {
		super(root);
	}

}
