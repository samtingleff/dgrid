package com.dgrid.errors;

public class EC2Exception extends AWSException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3562964920375602538L;

	public EC2Exception(Throwable root) {
		super(root);
	}

}
